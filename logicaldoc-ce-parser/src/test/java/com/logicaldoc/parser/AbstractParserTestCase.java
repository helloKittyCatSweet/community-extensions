package com.logicaldoc.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.Before;

import com.logicaldoc.util.junit.AbstractTestCase;

/**
 * Base class for Parser tests
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 8.8.3
 */
public abstract class AbstractParserTestCase extends AbstractTestCase {

	@Before
	@Override
	public void setUp() throws FileNotFoundException, IOException, SQLException {
		//com.logicalobjects.jlicense.license.LicenseManager.getInstance(System.getProperty("user.home") + "/logicaldoc-dev.lic", "");		
		super.setUp();
	}

	@Override
	protected String[] getSqlScripts() {
		return new String[] { "/sql/logicaldoc-core.sql", "/sql/logicaldoc-ocr.sql" };
	}
}