package com.logicaldoc.parser;

import java.io.File;

import org.junit.Test;

import com.logicaldoc.core.parser.ParseException;
import com.logicaldoc.core.security.Tenant;

import junit.framework.Assert;

/**
 * Basic test case for <code>OpenXMLParser</code>
 * 
 * @author Marco Meschieri
 * @version $Id:$
 * @since 1.0.0
 */
public class OpenXMLParserTest {

	@Test
	public void testParse() throws ParseException {
		OpenXMLParser parser = new OpenXMLParser();
		String content = parser.parse(getFile("/template.dotx"), "template.dotx", null, null, Tenant.DEFAULT_NAME);
		Assert.assertTrue(content.contains("Any notices"));
		Assert.assertEquals(7, parser.countPages(getFile("/template.dotx"), "template.dotx"));

		parser = new OpenXMLParser();
		content = parser.parse(getFile("/Prova_Corto.docx"), "Prova_Corto.docx", null, null, Tenant.DEFAULT_NAME);
		Assert.assertTrue(content.contains("grafico del profilo"));
		Assert.assertFalse(content.contains("pippo"));

		content = parser.parse(getFile("/Livellazione_3.xlsx"), "Livellazione_3.xlsx", null, null, Tenant.DEFAULT_NAME);
		Assert.assertTrue(content.contains("LIVELLAZIONE LUNGO LA LINEA"));
		Assert.assertFalse(content.contains("pippo"));

		content = parser.parse(getFile("/Distanze_Indirette.pptx"), "Distanze_Indirette.pptx", null, null,
				Tenant.DEFAULT_NAME);
		Assert.assertTrue(content.contains("devono essere collimati dal goniometro"));
		Assert.assertFalse(content.contains("pippo"));

		content = parser.parse(getFile("/bad_doc.docx"), "bad_doc.docx", null, null, Tenant.DEFAULT_NAME);
		Assert.assertEquals("", content.toString());

		content = parser.parse(getFile("/Prova_Corto_password.docx"), "Prova_Corto_password.docx", null, null,
				Tenant.DEFAULT_NAME);
		Assert.assertEquals("", content);

		parser = new OpenXMLParser();
		content = parser.parse(getFile("/Lab4385_TPUC_Aug2016.docx"), "Lab4385_TPUC_Aug2016.docx", null, null,
				Tenant.DEFAULT_NAME);

		Assert.assertTrue(content.contains("Roche"));
		Assert.assertTrue(content.contains("Diagnostics"));
		Assert.assertTrue(content.contains("Nishikaze"));
		Assert.assertFalse(content.contains("pippo"));

		parser = new OpenXMLParser();
		content = parser.parse(getFile("/example.vsd"), "example.vsd", null, null, Tenant.DEFAULT_NAME);
		Assert.assertTrue(content.contains("Provide example"));
	}

	private static File getFile(String resource) {
		String path = OpenXMLParserTest.class.getResource(resource).toString();
		path = path.substring(5);
		path = path.replace("%20", " ");
		return new File(path);
	}
}