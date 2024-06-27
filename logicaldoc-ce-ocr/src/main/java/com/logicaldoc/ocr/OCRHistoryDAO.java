package com.logicaldoc.ocr;

import com.logicaldoc.core.PersistenceException;
import com.logicaldoc.core.PersistentObjectDAO;
import com.logicaldoc.core.document.Document;

public interface OCRHistoryDAO extends PersistentObjectDAO<OCRHistory> {

	/**
	 * Stores a new history
	 * 
	 * @param history the history to save
	 * @param document the document the history points to
	 * 
	 * @throws PersistenceException An error in the database
	 */
	public void store(OCRHistory history, Document document) throws PersistenceException;

	/**
	 * This method deletes all the OCR history entries oldest than the given
	 * days from now. If <code>ttl</code> is 0 or -1, the cancellation is not
	 * made.
	 * 
	 * @param ttl The maximum number of days over which the history is
	 *        considered old
	 *        
	 * @throws PersistenceException Error in the database
	 */
	public void cleanOldHistories(int ttl) throws PersistenceException;
}