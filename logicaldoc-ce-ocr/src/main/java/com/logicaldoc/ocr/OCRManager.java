package com.logicaldoc.ocr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.java.plugin.registry.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaldoc.util.config.ContextProperties;
import com.logicaldoc.util.plugin.PluginRegistry;

/**
 * Manager class used to handle different OCR engines taken by the <b>OCR</b>
 * extension point.
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 7.7.3
 */
public class OCRManager {

	protected static Logger log = LoggerFactory.getLogger(OCRManager.class);

	// All the available engines, key is the engine's simple class name
	private Map<String, OCR> engines = new HashMap<>();

	@Resource(name = "ContextProperties")
	private ContextProperties config;

	public void setConfig(ContextProperties config) {
		this.config = config;
	}

	/**
	 * Initializes the converters map
	 */
	private void initEngines() {
		// Acquire the 'OCR' extensions of the core plugin
		PluginRegistry registry = PluginRegistry.getInstance();
		Collection<Extension> exts = registry.getExtensions("logicaldoc-ce-ocr", "OCR");
		for (Extension ext : exts) {
			String className = ext.getParameter("class").valueAsString();

			try {
				Class<?> clazz = Class.forName(className);
				// Try to instantiate the builder
				Object engine = clazz.getDeclaredConstructor().newInstance();
				if (!(engine instanceof OCR))
					throw new ClassNotFoundException(
							String.format("The specified engine %s doesn't extend the OCR abstract class", className));

				OCR ocrEngine = (OCR) engine;
				for (String name : ocrEngine.getParameterNames())
					ocrEngine.getParameters().put(name, null);
				engines.put(ocrEngine.getClass().getSimpleName(), ocrEngine);
				log.info("Registered OCR engine {}", className);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public Map<String, OCR> getEngines() {
		if (engines.isEmpty())
			initEngines();
		return engines;
	}

	public OCR getCurrentEngine() {
		return getEngine(config.getProperty("ocr.engine"));
	}

	public OCR getEngine(String name) {
		getEngines();
		return engines.get(name);
	}
}