package com.logicaldoc.conversion;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.office.LocalOfficeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A place holder for the Jodconverter office manager
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 8.8.5
 */
public class LibreOfficeManager {

	protected static Logger log = LoggerFactory.getLogger(LibreOfficeManager.class);

	protected static LocalOfficeManager officeManager;

	private LibreOfficeManager() {
		// Nothing to do
	}

	static void start(int[] ports, long taskTimeout, int tasks, String officeHome) throws OfficeException {
		// If the case create and launch the OfficeManager
		if (officeManager == null) {
			officeManager = LocalOfficeManager.builder().maxTasksPerProcess(tasks).officeHome(officeHome)
					.portNumbers(ports).taskExecutionTimeout(taskTimeout).install().build();
			log.info("Installed LibreOffice/OpenOffice conversion mechanism");

			officeManager.start();
			log.info("Started LibreOffice/OpenOffice conversion daemon");
		}
	}

	static void stop() {
		try {
			if (officeManager != null) {
				log.info("Killing LibreOffice/OpenOffice daemon");
				OfficeUtils.stopQuietly(officeManager);
				officeManager = null;
			}
		} catch (Exception e) {
			// Nothing to do
		}
	}
}
