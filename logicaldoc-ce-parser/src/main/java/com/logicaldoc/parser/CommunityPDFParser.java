package com.logicaldoc.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaldoc.core.document.Document;
import com.logicaldoc.core.parser.ParseException;
import com.logicaldoc.core.parser.ParseParameters;
import com.logicaldoc.ocr.OCR;
import com.logicaldoc.ocr.OCRHistory;
import com.logicaldoc.util.io.FileUtil;

/**
 * Extension of standard PDF parser that also uses OCR to read embedded raster
 * images.
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 2.0.0
 */
public class CommunityPDFParser extends com.logicaldoc.core.parser.PDFParser {

	protected static Logger log = LoggerFactory.getLogger(com.logicaldoc.parser.CommunityPDFParser.class);

	@Override
	public void internalParse(InputStream input, ParseParameters parameters, StringBuilder output)
			throws ParseException {

		File tmpFile = null;
		try {
			tmpFile = FileUtil.createTempFile("pdfparser", "-");
			FileUtil.writeFile(input, tmpFile.getAbsolutePath());

			try (InputStream is = new BufferedInputStream(new FileInputStream(tmpFile), 2048);) {
				super.internalParse(is, parameters, output);
			}

			if (!OCRUtil.isOcrizable(parameters.getFileName(), tmpFile.length(), parameters.getTenant()))
				return;

			/*
			 * Check if we have to run the OCR
			 */
			double fileSize = tmpFile.length();
			double contentSize = output.toString().getBytes().length;
			double textPercentageOnFileSize = contentSize * 100D / fileSize;
			float threshold = OCRUtil.getTextThreshold(parameters.getTenant());

			if (textPercentageOnFileSize < threshold) {
				String ocrExtraction = ocr(tmpFile, parameters.getLocale(), parameters.getTenant(),
						parameters.getDocument());
				output.append(ocrExtraction);
			}
		} catch (ParseException pe) {
			throw pe;
		} catch (Exception t) {
			throw new ParseException(t.getMessage(), t);
		} finally {
			FileUtil.strongDelete(tmpFile);
		}
	}

	private String ocr(File file, Locale locale, String tenant, Document document) throws IOException {
		OCR ocr = OCRUtil.newOCR();

		// Gets the necessary ISO3 (ISO 639-2) language code
		String lang = null;
		try {
			if (locale != null) {
				String tmplang = locale.getISO3Language();
				if (StringUtils.isNotEmpty(tmplang))
					lang = tmplang;
			}
		} catch (MissingResourceException e) {
			// Nothing to do
		}

		OCRHistory transaction = new OCRHistory();
		transaction.setDocument(document);

		StringBuilder output = new StringBuilder();
		ocr.extractPDFText(file, lang, tenant, output, transaction);
		return output.toString().trim();
	}
}