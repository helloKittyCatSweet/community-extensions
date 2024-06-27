package com.logicaldoc.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.sanselan.ImageReadException;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Test case for Image parsers
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 4.0
 */
public class ImageParserTest {

	@Before
	public void setUp() throws FileNotFoundException, IOException, SQLException {
//		com.logicalobjects.jlicense.license.LicenseManager.getInstance(System.getProperty("user.home")
//				+ "/logicaldoc-dev.lic", "");
	}

	@Test
	public void testMetadata() throws ImageReadException, IOException {
		ImageParser parser = new ImageParser();
		StringBuilder sb=new StringBuilder();
		parser.extractMetadata(getFile("/test.jpg"), sb);

		Assert.assertTrue(sb.toString().contains("tag1"));
		Assert.assertTrue(sb.toString().contains("Marco Meschieri"));
		
		parser = new ImageParser();
		parser.extractMetadata(getFile("/test.tif"), sb);

		Assert.assertTrue(sb.toString().contains("Descrizione"));
		Assert.assertTrue(sb.toString().contains("Meschieri"));
	}

	private File getFile(String resource) {
		String path = this.getClass().getResource(resource).toString();
		path = path.substring(5);
		path = path.replace("%20", " ");
		return new File(path);
	}
}