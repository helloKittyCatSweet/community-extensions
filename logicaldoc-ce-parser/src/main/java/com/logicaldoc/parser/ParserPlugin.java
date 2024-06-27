package com.logicaldoc.parser;

import java.io.IOException;

import com.logicaldoc.util.config.ContextProperties;
import com.logicaldoc.util.plugin.LogicalDOCPlugin;
import com.logicaldoc.util.plugin.PluginException;

/**
 * Provides some initializations needed by parsers
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 3.5
 */
public class ParserPlugin extends LogicalDOCPlugin {

	@Override
	public void install() throws PluginException {
		super.install();

		// Add some OCR settings
		try {
			ContextProperties pbean = new ContextProperties();

			pbean.setProperty("default.ocr.text.threshold", "0.5");
			pbean.setProperty("default.ocr.resolution.threshold", "600");
			pbean.setProperty("default.ocr.includes", "");
			pbean.setProperty("default.ocr.excludes", "");
			pbean.setProperty("ocr.enabled", "true");
			pbean.setProperty("ocr.timeout", "60");
			pbean.setProperty("ocr.engine", "Tesseract");
			pbean.setProperty("barcode.threshold", "100");

			pbean.write();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
}