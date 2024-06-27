package com.logicaldoc.ocr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.logicaldoc.core.security.Session;
import com.logicaldoc.core.security.menu.Menu;
import com.logicaldoc.gui.common.client.ServerException;
import com.logicaldoc.gui.common.client.beans.GUIParameter;
import com.logicaldoc.gui.frontend.client.services.OCRService;
import com.logicaldoc.util.Context;
import com.logicaldoc.util.config.ContextProperties;
import com.logicaldoc.web.service.AbstractRemoteService;

/**
 * Implementation of the SettingService
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 7.7.4
 */
public class OCRServiceImpl extends AbstractRemoteService implements OCRService {

	private static final long serialVersionUID = 1L;

	@Override
	public GUIParameter[] loadSettings() throws ServerException {
		Session session = checkMenu(getThreadLocalRequest(), Menu.SESSIONS);

		ContextProperties conf = Context.get().getProperties();

		List<GUIParameter> params = new ArrayList<>();

		// Load tenant-specific settings
		params.add(new GUIParameter("ocr.enabled", conf.getProperty("ocr.enabled")));
		params.add(new GUIParameter(session.getTenantName() + ".ocr.resolution.threshold",
				conf.getProperty(session.getTenantName() + ".ocr.resolution.threshold")));
		params.add(new GUIParameter(session.getTenantName() + ".ocr.text.threshold",
				conf.getProperty(session.getTenantName() + ".ocr.text.threshold")));
		params.add(new GUIParameter(session.getTenantName() + ".ocr.includes",
				conf.getProperty(session.getTenantName() + ".ocr.includes")));
		params.add(new GUIParameter(session.getTenantName() + ".ocr.excludes",
				conf.getProperty(session.getTenantName() + ".ocr.excludes")));
		params.add(new GUIParameter(session.getTenantName() + ".ocr.erroronempty",
				conf.getProperty(session.getTenantName() + ".ocr.erroronempty")));

		// Load general settings
		params.add(new GUIParameter("ocr.timeout", conf.getProperty("ocr.timeout")));
		params.add(new GUIParameter("ocr.timeout.batch", conf.getProperty("ocr.timeout.batch")));
		params.add(new GUIParameter("ocr.rendres", conf.getProperty("ocr.rendres")));
		params.add(new GUIParameter("ocr.batch", conf.getProperty("ocr.batch")));
		params.add(new GUIParameter("ocr.engine", conf.getProperty("ocr.engine")));
		params.add(new GUIParameter("ocr.maxsize", conf.getProperty("ocr.maxsize")));
		params.add(new GUIParameter("ocr.cropImage", conf.getProperty("ocr.cropImage")));
		params.add(new GUIParameter("ocr.threads", conf.getProperty("ocr.threads")));
		params.add(new GUIParameter("ocr.threads.wait", conf.getProperty("ocr.threads.wait")));
		params.add(new GUIParameter("ocr.events.record", conf.getProperty("ocr.events.record")));
		params.add(new GUIParameter("ocr.events.maxtext", conf.getProperty("ocr.events.maxtext")));

		// Load engine settings
		OCRManager manager = (OCRManager) Context.get().getBean(OCRManager.class);
		Map<String, OCR> engines = manager.getEngines();
		for (Map.Entry<String, OCR> entry : engines.entrySet()) {
			OCR ocr = entry.getValue();
			if (ocr.isAvailable()) {
				params.add(new GUIParameter("ocr.engine." + entry.getKey(), entry.getKey()));
				List<String> ocrParameters = ocr.getParameterNames();
				for (String param : ocrParameters) {
					String fullName = "ocr." + entry.getKey() + "." + param;
					params.add(new GUIParameter(fullName, conf.getProperty(fullName)));
				}
			}
		}

		return params.toArray(new GUIParameter[0]);
	}
}