package com.logicaldoc.conversion;

import com.logicaldoc.util.plugin.LogicalDOCPlugin;
import com.logicaldoc.util.plugin.PluginException;

/**
 * Conversion plugin class-
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 7.5
 */
public class ConversionPlugin extends LogicalDOCPlugin {
	@Override
	public void install() throws PluginException {
		addServlet("ConvertPdf", ConvertPdf.class.getName(), "/convertpdf/*");
		addServlet("Convert", Convert.class.getName(), "/converto/*");
		addServlet("ConvertJpg", ConvertJpg.class.getName(), "/convertjpg");

		setRestartRequired();
	}
}