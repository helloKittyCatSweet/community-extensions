package com.logicaldoc.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaldoc.ocr.OCR;
import com.logicaldoc.ocr.OCRManager;
import com.logicaldoc.ocr.Tesseract;
import com.logicaldoc.util.Context;
import com.logicaldoc.util.config.ContextProperties;
import com.logicaldoc.util.io.FileUtil;

/**
 * Utility class for OCR interaction.
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 6.0
 */
public class OCRUtil {

	private static boolean isTesting = false;

	protected static Logger log = LoggerFactory.getLogger(OCRUtil.class);

	/**
	 * Checks if the OCR is enabled and if the license feature is enabled
	 * 
	 * @return the enabled status
	 */
	public static boolean isEnabled() {
		ContextProperties config = Context.get().getProperties();
		return (config.getBoolean("ocr.enabled"));
	}

	/**
	 * Checks if the given filename match the OCR's include/exclude policies and
	 * the file size is less than the configured max. file size and of course if
	 * the OCR is enabled
	 * 
	 * @param filename name of the file
	 * @param fileSize size of the file in bytes
	 * @param tenant name of the tenant
	 * 
	 * @return if the OCR can process the given filename
	 */
	public static boolean isOcrizable(String filename, Long fileSize, String tenant) {
		ContextProperties config = Context.get().getProperties();
		if (!isEnabled()) {
			log.warn("Skip OCR: OCR is disabled");
			return false;
		}

		if (!FileUtil.matches(filename, config.getProperty(tenant + ".ocr.includes"),
				config.getProperty(tenant + ".ocr.excludes"))) {
			log.warn("Skip OCR: file name {} does not matches the inclusion/exclusion filters", filename);
			return false;
		}

		if (fileSize != null && fileSize.longValue() > (config.getLong("ocr.maxsize", 100) * 1024 * 1024)) {
			log.warn("Skip OCR: size of file {} over the maximum of {} MB", filename,
					config.getLong("ocr.maxsize", 100));
			return false;
		}

		return true;
	}

	public static float getTextThreshold(String tenant) {
		return Context.get().getProperties().getFloat(tenant + ".ocr.text.threshold", 0.01F);
	}

	public static OCR newOCR() {
		OCR ocr = ((OCRManager) Context.get().getBean(OCRManager.class)).getCurrentEngine();
		if (ocr == null && isTesting)
			ocr = new Tesseract();
		return ocr;
	}

	private OCRUtil() {
		// Do nothing
	}
}