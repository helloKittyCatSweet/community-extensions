package com.logicaldoc.ocr;

import javax.annotation.Resource;

import org.slf4j.LoggerFactory;

import com.logicaldoc.core.HibernatePersistentObjectDAO;
import com.logicaldoc.core.History;
import com.logicaldoc.core.PersistenceException;
import com.logicaldoc.core.RunLevel;
import com.logicaldoc.core.communication.EventCollector;
import com.logicaldoc.core.document.Document;
import com.logicaldoc.core.folder.FolderDAO;
import com.logicaldoc.core.security.user.UserDAO;

public class HibernateOCRHistoryDAO extends HibernatePersistentObjectDAO<OCRHistory> implements OCRHistoryDAO {

	@Resource(name = "FolderDAO")
	private FolderDAO folderDAO;

	@Resource(name = "UserDAO")
	private UserDAO userDAO;

	private HibernateOCRHistoryDAO() {
		super(OCRHistory.class);
		super.log = LoggerFactory.getLogger(HibernateOCRHistoryDAO.class);
	}

	@Override
	public void cleanOldHistories(int ttl) throws PersistenceException {
			log.info("cleanOldHistories rows updated: {}", cleanOldRecords(ttl, "ld_ocr_history"));
	}

	@Override
	public void store(OCRHistory history) throws PersistenceException {
		// Write only if the history is enabled
		if (RunLevel.current().aspectEnabled(History.ASPECT)) {
			if (history.getUserId() == null) {
				history.setUser(userDAO.findByUsername("_ocr"));
			}

			super.store(history);
			EventCollector.get().newEvent(history);
		}
	}

	@Override
	public void store(OCRHistory history, Document document) throws PersistenceException {
		if (document != null) {
			history.setTenantId(document.getTenantId());
			history.setFilename(document.getFileName());
			history.setFileSize(document.getFileSize());
			history.setDocId(document.getId());
			history.setColor(document.getColor());
			if (document.getFolder() != null) {
				history.setFolderId(document.getFolder().getId());
				history.setPath(folderDAO.computePathExtended(document.getFolder().getId()));
			}
		}
		
		store(history);
	}

	public void setFolderDAO(FolderDAO folderDAO) {
		this.folderDAO = folderDAO;
	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}
}
