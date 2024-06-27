package com.logicaldoc.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;

import com.logicaldoc.core.parser.ParseException;
import com.logicaldoc.core.security.Tenant;
import com.logicaldoc.ocr.OCRManager;
import com.logicaldoc.ocr.Tesseract;
import com.logicaldoc.util.Context;

import junit.framework.Assert;

public class EnterprisePDFParserTest extends AbstractParserTestCase {

	@Override
	public void setUp() throws FileNotFoundException, IOException, SQLException {
		super.setUp();

		OCRManager manager = (OCRManager) Context.get().getBean(OCRManager.class);
		manager.getEngines().put("Tesseract", new Tesseract());
	}

	@Test
	public void testParseFileLocale() throws ParseException {
		CommunityPDFParser parser = new CommunityPDFParser();
		String content = parser.parse(getFile("/bando_300dpi_bw.pdf"), "bando_300dpi_bw.pdf", null, null,
				Tenant.DEFAULT_NAME);
		Assert.assertTrue(content.contains("contributo concesso"));
		Assert.assertFalse(content.contains("pippo"));
	}

	private File getFile(String resource) {
		String path = this.getClass().getResource(resource).toString();
		path = path.substring(5);
		path = path.replace("%20", " ");
		return new File(path);
	}
}