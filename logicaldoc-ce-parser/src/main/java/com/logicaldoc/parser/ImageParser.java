package com.logicaldoc.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.MissingResourceException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaldoc.core.document.Document;
import com.logicaldoc.core.parser.AbstractParser;
import com.logicaldoc.core.parser.ParseException;
import com.logicaldoc.core.parser.ParseParameters;
import com.logicaldoc.ocr.OCR;
import com.logicaldoc.ocr.OCRHistory;
import com.logicaldoc.util.Context;
import com.logicaldoc.util.io.FileUtil;
import com.logicaldoc.util.io.IOUtil;
import com.twelvemonkeys.contrib.tiff.TIFFUtilities;

/**
 * This parser reads images (supported formats are: jpg, jfif, bmp, jpeg, wbmp,
 * png, gif) and extracts text using OCR.
 * 
 * @author Alessandro Gasparini - LogicalDOC
 * @since 3.5
 */
public class ImageParser extends AbstractParser {

	protected static Logger log = LoggerFactory.getLogger(ImageParser.class);

	@Override
	public String parse(File file, String filename, String encoding, Locale locale, String tenant, Document document,
			String fileVersion) throws ParseException {
		StringBuilder output = new StringBuilder();

		extractMetadata(file, output);

		if (!OCRUtil.isOcrizable(filename, file.length(), tenant))
			return output.toString();

		OCR ocr = OCRUtil.newOCR();
		try {
			// Gets the necessary ISO3 (ISO 639-2) language code
			String lang = getISO3Language(locale);
			output.append("\n");

			OCRHistory transaction = new OCRHistory();
			transaction.setDocument(document);

			StringBuilder ocrExtraction = new StringBuilder();
			ocr.extractText(file, lang != null ? lang.toLowerCase() : "en", tenant, ocrExtraction, transaction);

			String ocrExtractedText = ocrExtraction.toString().trim();
			if (StringUtils.isEmpty(ocrExtractedText)) {
				log.warn("The OCR was unable to extract any text from document {}", document.getId());
				if (Context.get().getProperties().getBoolean(tenant + ".ocr.erroronempty", false))
					throw new ParseException("Cannot extract texts with OCR");
			}
			output.append(ocrExtractedText);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (Exception th) {
			log.warn(th.getMessage(), th);
		}

		return output.toString();
	}

	private String getISO3Language(Locale locale) {
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
		return lang;
	}

	@Override
	public void internalParse(InputStream input, ParseParameters parameters, StringBuilder output)
			throws ParseException {

		File tempFile = null;

		try {
			tempFile = FileUtil.createTempFile("image", "." + FileUtil.getExtension(parameters.getFileName()));

			try (OutputStream out = new FileOutputStream(tempFile)) {
				byte[] buf = new byte[1024];
				int len;
				while ((len = input.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			}
			output.append(parse(tempFile, parameters.getFileName(), parameters.getEncoding(), parameters.getLocale(),
					parameters.getTenant(), parameters.getDocument(), parameters.getFileVersion()));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			IOUtil.close(input);
			if (tempFile != null && tempFile.exists())
				FileUtils.deleteQuietly(tempFile);
		}
	}

	/**
	 * Extracts all image metadata placing them into the content
	 * 
	 * @param file the image file
	 * @param output the buffer that hosts the extracted metadata
	 */
	public void extractMetadata(File file, StringBuilder output) {
		IImageMetadata metadata = null;
		try {
			metadata = Sanselan.getMetadata(file);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		if (metadata == null)
			return;

		StringBuilder sb = new StringBuilder();

		@SuppressWarnings("rawtypes")
		ArrayList items = metadata.getItems();
		for (int i = 0; i < items.size(); i++) {
			String item = items.get(i).toString();
			sb.append("\n");
			sb.append(item);
		}

		output.append(sb.toString());
	}

	@Override
	public int countPages(File input, String filename) {
		try {
			if (filename != null
					&& (filename.toLowerCase().endsWith(".tiff") || filename.toLowerCase().endsWith(".tif"))) {
				try (ImageInputStream is = ImageIO.createImageInputStream(input)) {
					return TIFFUtilities.getPages(is).size();
				}
			}
		} catch (Exception e) {
			// Do nothing
		}
		return 1;
	}

	@Override
	public int countPages(InputStream input, String filename) {
		if (filename != null && (filename.toLowerCase().endsWith(".tiff") || filename.toLowerCase().endsWith(".tif"))) {
			File tifFile = null;
			try {
				tifFile = FileUtil.createTempFile("countpages", ".tif");
				FileUtil.writeFile(input, tifFile.getAbsolutePath());
				return countPages(tifFile, filename);
			} catch (Exception e) {
				log.error(e.getMessage());
			} finally {
				if (tifFile != null)
					FileUtil.strongDelete(tifFile);
			}
		}
		return 1;
	}
}