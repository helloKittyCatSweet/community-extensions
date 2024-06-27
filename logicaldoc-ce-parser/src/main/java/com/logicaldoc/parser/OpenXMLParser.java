package com.logicaldoc.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.ooxml.extractor.ExtractorFactory;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaldoc.core.conversion.FormatConverterManager;
import com.logicaldoc.core.parser.AbstractParser;
import com.logicaldoc.core.parser.ParseParameters;
import com.logicaldoc.core.parser.Parser;
import com.logicaldoc.core.parser.ParserFactory;
import com.logicaldoc.core.store.Storer;
import com.logicaldoc.util.Context;
import com.logicaldoc.util.io.FileUtil;

/**
 * This parser is able to extract text from Office documents
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 1.0.0
 */
public class OpenXMLParser extends AbstractParser {

	private static final int ONE_MB = 1024 * 1024;

	protected static Logger log = LoggerFactory.getLogger(OpenXMLParser.class);

	@Override
	public void internalParse(InputStream input, ParseParameters parameters, StringBuilder output) {
		try {
			if (parameters.getFileName().toLowerCase().endsWith(".xlsx")
					|| parameters.getFileName().toLowerCase().endsWith(".xlsm")) {
				File tmpFile = null;
				try {
					tmpFile = FileUtil.createTempFile("parse",
							"." + FileUtil.getExtension(parameters.getFileName().toLowerCase()));
					FileUtil.writeFile(input, tmpFile.getAbsolutePath());
					if (tmpFile.length() < ONE_MB) {
						// In a customer with 100MB XLSX the system gets freezed
						// with CPU deadlocks, so use POI just for small files
						try (POITextExtractor extractor = ExtractorFactory.createExtractor(tmpFile)) {
							output.append(extractor.getText());
						}
					} else {
						// For big excels, better to convert to PDF
						FormatConverterManager manager = (FormatConverterManager) Context.get()
								.getBean(FormatConverterManager.class);
						manager.convertToPdf(parameters.getDocument(), parameters.getFileVersion());
						File tmpPdf = null;
						try {
							tmpPdf = FileUtil.createTempFile("parse", ".pdf");

							Storer storer = (Storer) Context.get().getBean(Storer.class);
							String resource = storer.getResourceName(parameters.getDocument(),
									parameters.getFileVersion(), FormatConverterManager.PDF_CONVERSION_SUFFIX);
							storer.writeToFile(parameters.getDocument().getId(), resource, tmpPdf);

							Parser parser = ParserFactory.getParser(tmpPdf.getName());
							output.append(parser.parse(tmpPdf, parameters.getFileName(), null, parameters.getLocale(),
									parameters.getTenant()));
						} finally {
							FileUtil.strongDelete(tmpPdf);
						}
					}
				} catch (Exception t) {
					log.error(t.getMessage(), t);
				} finally {
					FileUtil.strongDelete(tmpFile);
				}
			} else {
				try (POITextExtractor extractor = ExtractorFactory.createExtractor(input)) {
					output.append(extractor.getText());
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				// Nothing to do
			}
		}
	}

	@Override
	public int countPages(InputStream input, String filename) {
		int pages = 1;
		if (isWord(filename)) {
			pages = countWordPages(input);
		} else if (isExcel(filename)) {
			pages = countExcelPages(input, filename);
		} else if (filename.toLowerCase().endsWith(".pptx")) {
			pages = countPowerpointPages(input);
		}
		return pages;
	}

	private int countPowerpointPages(InputStream input) {
		int pages = 1;
		try (XMLSlideShow xslideShow = new XMLSlideShow(input)) {
			pages = xslideShow.getSlides().size();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return pages;
	}

	private int countExcelPages(InputStream input, String filename) {
		int pages = 1;
		File tmpFile = null;
		try {
			tmpFile = FileUtil.createTempFile("countpages", "." + FileUtil.getExtension(filename.toLowerCase()));
			FileUtil.writeFile(input, tmpFile.getAbsolutePath());

			if (tmpFile.length() < ONE_MB) {
				// In a customer with 100MB XLSX the system gets freezed
				// with CPU deadlocks, so use POI just for small files
				pages = countExcepPageswithPOI(input);
			}
		} catch (Exception t) {
			log.error(t.getMessage(), t);
		} finally {
			FileUtil.strongDelete(tmpFile);
		}
		return pages;
	}

	private int countExcepPageswithPOI(InputStream input) {
		int pages=1;
		try (XSSFWorkbook xwb = new XSSFWorkbook(input)) {
			Integer sheetNums = xwb.getNumberOfSheets();
			if (sheetNums > 0) {
				pages = xwb.getSheetAt(0).getRowBreaks().length + 1;
			}
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
		return pages;
	}

	private int countWordPages(InputStream input) {
		int pages = 1;
		try (XWPFDocument docx = new XWPFDocument(input)) {
			pages = docx.getProperties().getExtendedProperties().getUnderlyingProperties().getPages();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return pages;
	}

	private boolean isExcel(String filename) {
		return filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xlsm");
	}

	private boolean isWord(String filename) {
		return filename == null || filename.toLowerCase().endsWith(".docx") || filename.toLowerCase().endsWith(".dotm")
				|| filename.toLowerCase().endsWith(".dotx");
	}
}