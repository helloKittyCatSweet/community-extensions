package com.logicaldoc.ocr;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import com.logicaldoc.core.dbinit.PluginDbInit;
import com.logicaldoc.util.config.ContextProperties;
import com.logicaldoc.util.plugin.LogicalDOCPlugin;
import com.logicaldoc.util.plugin.PluginException;

/**
 * OCR module plugin class.
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 6.0
 */
public class OCRPlugin extends LogicalDOCPlugin {

	@Override
	public void install() throws PluginException {
		super.install();

		ContextProperties config = null; 
		try {
			config = new ContextProperties();
			config.setProperty("history.ocr.ttl", "90");
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		
		initDB(config);		

		addServlet("OCRService", OCRServiceImpl.class.getName(), "/frontend/ocr");
		addServlet("OCRHistoriesData", OCRHistoriesDataServlet.class.getName(), "/data/ocrhistories.xml");

		setRestartRequired();
	}

	private void initDB(ContextProperties config) {
		try {
			/*
			 * Initialize the database
			 */
			PluginDbInit dbInit = new PluginDbInit();
			dbInit.setDbms(config.getProperty("jdbc.dbms"));
			dbInit.setDriver(config.getProperty("jdbc.driver"));
			dbInit.setUrl(config.getProperty("jdbc.url"));
			dbInit.setUsername(config.getProperty("jdbc.username"));
			dbInit.setPassword(config.getProperty("jdbc.password"));

			if (dbInit.testConnection()) {
				// connection success
				log.error("OCR Start plugin inistalization");
				dbInit.init(Set.of("logicaldoc-ce-ocr"));
				log.error("OCR Plugin correctlry initialized");
			} else {
				// connection failure
				log.info("connection failure");
			}
		} catch (SQLException e) {			
			e.printStackTrace();
			log.error(e.getMessage(), e);
		} catch (Throwable tw) {
			log.error(tw.getMessage(), tw);
		}
	}
	
}