package com.logicaldoc.ocr;

import com.logicaldoc.util.junit.AbstractTestCase;

/**
 * Abstract test case for the OCR module. This class initialises a test database
 * and prepares the spring test context.
 * <p>
 * 
 * @author Matteo Caruso - LogicalDOC
 * @since 6.0
 */
public abstract class AbstractOCRTestCase extends AbstractTestCase {

	@Override
	protected String[] getSqlScripts() {
		return new String[] { "/sql/logicaldoc-core.sql", "/sql/logicaldoc-ocr.sql" };
	}
}