package com.logicaldoc.ocr;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.logicaldoc.core.PersistenceException;
import com.logicaldoc.core.document.Document;
import com.logicaldoc.core.document.DocumentDAO;
import com.logicaldoc.core.security.Tenant;
import com.logicaldoc.util.Context;

public class HibernateOCRHistoryDAOTest extends AbstractOCRTestCase {

	private OCRHistoryDAO testSubject;

	@Before
	public void setUp() throws FileNotFoundException, IOException, SQLException {
		super.setUp();
		testSubject = (OCRHistoryDAO) Context.get().getBean(OCRHistoryDAO.class);
	}

	@Test
	public void testStore() throws PersistenceException {
		List<OCRHistory> histories = testSubject.findAll(Tenant.DEFAULT_ID);
		assertEquals(0, histories.size());

		DocumentDAO docDao = (DocumentDAO) Context.get().getBean(DocumentDAO.class);
		Document doc = docDao.findById(1L);

		OCRHistory history = new OCRHistory();
		history.setDate(new Date(0L));
		history.setEvent(OCRHistory.EVENT_OCR_SUCCESS);

		testSubject.store(history, doc);

		histories = testSubject.findAll(Tenant.DEFAULT_ID);
		assertEquals(1, histories.size());

		history = new OCRHistory();
		history.setEvent(OCRHistory.EVENT_OCR_FAILURE);
		testSubject.store(history, doc);

		histories = testSubject.findAll(Tenant.DEFAULT_ID);
		assertEquals(2, histories.size());
	}

	@Test
	public void testCleanOldHistories() throws PersistenceException {
		testStore();

		testSubject.jdbcUpdate("update ld_ocr_history set ld_lastmodified = ld_date");

		List<OCRHistory> histories = testSubject.findAll(Tenant.DEFAULT_ID);
		assertEquals(2, histories.size());

		testSubject.cleanOldHistories(1);
		histories = testSubject.findAll(Tenant.DEFAULT_ID);
		assertEquals(1, histories.size());
	}
}