package com.logicaldoc.conversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaldoc.core.PersistenceException;
import com.logicaldoc.core.conversion.FormatConverterManager;
import com.logicaldoc.core.document.Document;
import com.logicaldoc.core.document.DocumentDAO;
import com.logicaldoc.core.document.DocumentEvent;
import com.logicaldoc.core.document.DocumentHistory;
import com.logicaldoc.core.document.Version;
import com.logicaldoc.core.document.VersionDAO;
import com.logicaldoc.core.security.Session;
import com.logicaldoc.core.security.authorization.PermissionException;
import com.logicaldoc.core.security.user.Group;
import com.logicaldoc.util.Context;
import com.logicaldoc.util.io.FileUtil;
import com.logicaldoc.web.util.ServletUtil;
//import com.logicalobjects.jlicense.license.LicenseManager;

/**
 * This servlet converts a document's file in a different format
 * 
 * This servlet requires the following parameters:
 * <ul>
 * <li>docId</li>
 * <li>fileVersion</li>
 * <li>version</li>
 * <li>format</li>
 * </ul>
 * <br>
 *
 * @author Marco Meschieri - LogicalDOC
 * @since 7.6.4
 */
public class Convert extends HttpServlet {

	private static final String VERSION = "version";

	private static final String FILE_VERSION = "fileVersion";

	private static final String DOCUMENT_ID = "docId";

	private static final String FORMAT = "format";

	private static final long serialVersionUID = 1L;

	protected static Logger log = LoggerFactory.getLogger(Convert.class);

	/**
	 * Constructor of the object.
	 */
	public Convert() {
		super();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		File out = null;

		try {
			Session session = ServletUtil.validateSession(request);

			String format = request.getParameter(FORMAT);
			if (StringUtils.isEmpty(format)) {
				log.error("Format not specified");
				response.getOutputStream().println("Format not specified");
				return;
			}

            /*
			if ("pdf".equals(format) && !LicenseManager.getInstance().isEnabled("Feature_8")) {
				log.error("Feature pdf conversion not enabled");
				response.getOutputStream().println("Feature not enabled");
				return;
			} */

            /* 
			if (!"pdf".equals(format) && !LicenseManager.getInstance().isEnabled("Feature_45")) {
				log.error("Feature format conversion not enabled");
				response.getOutputStream().println("Feature not enabled");
				return;
			} */

			String docIdParam = request.getParameter(DOCUMENT_ID);
			if (StringUtils.isEmpty(docIdParam)) {
				log.error("Document not specified");
				response.getOutputStream().println("Document not specified");
				return;
			}

			DocumentDAO docDao = (DocumentDAO) Context.get().getBean(DocumentDAO.class);
			VersionDAO versionDao = (VersionDAO) Context.get().getBean(VersionDAO.class);
			long docId = Long.parseLong(docIdParam);

			Document originalDoc = docDao.findById(docId);
			Document document = docDao.findDocument(docId);
			if (document == null) {
				log.error("Document not dound");
				response.getOutputStream().println("Document not found");
				return;
			}

			validate(docId, originalDoc, document, session);

			String fileName = document.getFileName();
			String fileVersion = document.getFileVersion();
			if (StringUtils.isNotEmpty(request.getParameter(FILE_VERSION))) {
				fileVersion = request.getParameter(FILE_VERSION);
				Version version = versionDao.findByVersion(document.getId(), fileVersion);
				if (version != null)
					fileName = version.getFileName();
			} else {
				String ver = document.getVersion();
				if (StringUtils.isNotEmpty(request.getParameter(VERSION)))
					ver = request.getParameter(VERSION);
				Version version = versionDao.findByVersion(document.getId(), ver);
				fileVersion = version.getFileVersion();
			}

			FormatConverterManager manager = (FormatConverterManager) Context.get()
					.getBean(FormatConverterManager.class);
			DocumentHistory history = new DocumentHistory();
			history.setSession(session);

			// Create the converted file
			out = FileUtil.createTempFile("conv", "." + format);
			manager.convertToFile(document, fileVersion, out, history);

			/*
			 * For security checks, also gridRecord a download event
			 */
			DocumentHistory downloadHistory = new DocumentHistory(history);
			downloadHistory.setEvent(DocumentEvent.DOWNLOADED.toString());
			downloadHistory.setComment("downloaded the " + FileUtil.getExtension(out.getName()) + " conversion");
			docDao.saveDocumentHistory(document, downloadHistory);

			// Download the conversion
			ServletUtil.downloadFile(request, response, out, FileUtil.getBaseName(fileName) + "-conv." + format);
		} catch (Exception r) {
			handleEror(request, response, r);
		} finally {
			FileUtil.strongDelete(out);
		}
	}

	private void validate(long docId, Document originalDoc, Document document, Session session)
			throws FileNotFoundException, PersistenceException, PermissionException {

		DocumentDAO dao = (DocumentDAO) Context.get().getBean(DocumentDAO.class);

		if (document != null && !session.getUser().isMemberOf(Group.GROUP_ADMIN)
				&& !session.getUser().isMemberOf("publisher") && !document.isPublishing())
			throw new FileNotFoundException("Document not published");

		if (!dao.isReadEnabled(docId, session.getUserId()))
			throw new PermissionException(
					String.format("User %s cannot access the document %s", session.getUsername(), originalDoc));

		if (!dao.isDownloadEnabled(docId, session.getUserId()))
			throw new PermissionException(
					String.format("User %s cannot download the document %s", session.getUsername(), originalDoc));
	}

	private void handleEror(HttpServletRequest request, HttpServletResponse response, Throwable error) {
		log.error(error.getMessage(), error);

		try {
			ServletUtil.setContentDisposition(request, response, "notavailable.pdf");
			int letter = 0;

			try (OutputStream os = response.getOutputStream();
					InputStream is = Convert.class.getResourceAsStream("/pdf/notavailable.pdf");) {
				while ((letter = is.read()) != -1)
					os.write(letter);
			}
		} catch (Exception t) {
			log.warn(t.getMessage(), t);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			doGet(request, response);
		} catch (Exception t) {
			// Nothing to do
		}
	}
}