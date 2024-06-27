package com.logicaldoc.conversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaldoc.core.PersistenceException;
import com.logicaldoc.core.conversion.FormatConverterManager;
import com.logicaldoc.core.document.Document;
import com.logicaldoc.core.document.DocumentDAO;
import com.logicaldoc.core.document.Version;
import com.logicaldoc.core.document.VersionDAO;
import com.logicaldoc.core.security.Session;
import com.logicaldoc.core.security.SessionListener;
import com.logicaldoc.core.security.SessionManager;
import com.logicaldoc.core.security.user.Group;
import com.logicaldoc.core.store.Storer;
import com.logicaldoc.core.util.GhostUtil;
import com.logicaldoc.util.Context;
import com.logicaldoc.util.io.FileUtil;
import com.logicaldoc.web.util.ServletUtil;
//import com.logicalobjects.jlicense.license.LicenseManager;

/**
 * This servlet produces a set of JPGs from a document as a list of jpg files
 * inside io.tmpdir/jpgprev/sid. If the export was already performed it simply
 * return it.
 * 
 * This servlet requires the following parameters:
 * <ul>
 * <li>docId</li>
 * <li>version</li>
 * </ul>
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 8.3.1
 */
public class ConvertJpg extends HttpServlet implements SessionListener {

	private static final String VERSION = "version";

	private static final String FILE_VERSION = "fileVersion";

	private static final String DOCUMENT_ID = "docId";

	private static final long serialVersionUID = 1L;

	protected static Logger log = LoggerFactory.getLogger(ConvertJpg.class);

	/**
	 * Constructor of the object.
	 */
	public ConvertJpg() {
		super();
	}

	private static File getJpgDirRoot(String sid) {
		return new File(System.getProperty("java.io.tmpdir") + "/convertjpg/" + sid);

	}

	/**
	 * Prepares the temporary folder to store the pages of the current document
	 */
	private static File getJpgDir(String sid, long docId) {
		return new File(getJpgDirRoot(sid).getAbsolutePath() + File.separator + docId);
	}

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			Session session = ServletUtil.validateSession(request);

            /*
			if (!LicenseManager.getInstance().isEnabled("Feature_8")) {
				log.error("Feature pdf conversion not enabled");
				response.getOutputStream().println("Feature not enabled");
				return;
			}*/

			SessionManager.get().addListener(this);

			int page = getPage(request);

			Document document = getDocument(request, session);

			String fileVersion = getFileVersion(request, document);

			File tempDir = getJpgDir(session.getSid(), document.getId());
			if (!tempDir.exists()) {
				/*
				 * No temporary dir, so we have to print the pages.
				 */
				tempDir.mkdirs();
				tempDir.mkdir();

				Storer storer = (Storer) Context.get().getBean(Storer.class);

				String resourceName = storer.getResourceName(document, fileVersion, null);
				if (!"pdf".equalsIgnoreCase(FileUtil.getExtension(document.getFileName()))) {
					FormatConverterManager manager = (FormatConverterManager) Context.get()
							.getBean(FormatConverterManager.class);
					manager.convertToPdf(document, fileVersion, session.getSid());
					resourceName = storer.getResourceName(document, fileVersion,
							FormatConverterManager.PDF_CONVERSION_SUFFIX);
				}

				if (storer.exists(document.getId(), resourceName)) {
					File pdfConversion = FileUtil.createTempFile("jpgprev", ".pdf");
					try {
						storer.writeToFile(document.getId(), resourceName, pdfConversion);
						GhostUtil.print(pdfConversion, new File(tempDir.getAbsolutePath() + "/page.jpg"), null);
					} finally {
						FileUtil.strongDelete(pdfConversion);
					}
				}
			}

			// Get ordered list of page files
			File[] children = tempDir.listFiles();
			List<File> pages = new ArrayList<>();
			pages.addAll(Arrays.asList(children));
			pages.sort((f1, f2) -> f1.getName().compareTo(f2.getName()));

			File pageFile = null;
			if (page <= pages.size())
				pageFile = pages.get(page - 1);

			if (fileVersion.equals(document.getFileVersion()) && document.getPreviewPages() != pages.size()) {
				DocumentDAO docDao = (DocumentDAO) Context.get().getBean(DocumentDAO.class);
				docDao.initialize(document);
				document.setPreviewPages(pages.size());
				docDao.store(document);
			}

			if (pageFile == null) {
				throw new IOException(String.format("Unable to extract pages of document %s", document));
			} else {
				ServletUtil.downloadFile(request, response, pageFile, pageFile.getName());
			}
		} catch (Exception r) {
			handleError(request, response, r);
		}
	}

	private void handleError(HttpServletRequest request, HttpServletResponse response, Throwable error) {
		log.error(error.getMessage(), error);
		try {
			ServletUtil.setContentDisposition(request, response, "notavailable.jpg");
			try (InputStream is = ConvertPdf.class.getResourceAsStream("/jpg/notavailable.jpg");
					OutputStream os = response.getOutputStream();) {
				IOUtils.copy(is, os);
			}
		} catch (Exception t) {
			ServletUtil.sendError(response, t.getMessage());
		}
	}

	private Document getDocument(HttpServletRequest request, Session session)
			throws PersistenceException, FileNotFoundException {
		DocumentDAO docDao = (DocumentDAO) Context.get().getBean(DocumentDAO.class);
		Document document = docDao.findDocument(Long.parseLong(request.getParameter(DOCUMENT_ID)));
		if (document != null && !session.getUser().isMemberOf(Group.GROUP_ADMIN)
				&& !session.getUser().isMemberOf("publisher") && !document.isPublishing())
			throw new FileNotFoundException("Document not published");
		return document;
	}

	private String getFileVersion(HttpServletRequest request, Document document) throws PersistenceException {
		VersionDAO versionDao = (VersionDAO) Context.get().getBean(VersionDAO.class);
		String fileVersion = document.getFileVersion();
		if (StringUtils.isNotEmpty(request.getParameter(FILE_VERSION))) {
			fileVersion = request.getParameter(FILE_VERSION);
		} else {
			String ver = document.getVersion();
			if (StringUtils.isNotEmpty(request.getParameter(VERSION)))
				ver = request.getParameter(VERSION);
			Version version = versionDao.findByVersion(document.getId(), ver);
			if (version != null)
				fileVersion = version.getFileVersion();
		}
		return fileVersion;
	}

	private int getPage(HttpServletRequest request) {
		int page = 1;
		try {
			page = Integer.parseInt(request.getParameter("page"));
		} catch (Exception t) {
			// Nothing to do
		}
		return page;
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			doGet(request, response);
		} catch (Exception t) {
			// Nothing to do
		}
	}

	@Override
	public void onSessionCreated(Session session) {
		// Nothing to do
	}

	public static void deleteJpgDir(String sid, long docId) {
		File tempDir = getJpgDir(sid, docId);
		if (tempDir.exists())
			try {
				FileUtils.deleteDirectory(tempDir);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
	}

	@Override
	public void onSessionClosed(Object sid) {
		try {
			log.warn("Clean the temporary jpg folder for session {}", sid);
			FileUtils.forceDelete(getJpgDirRoot(sid.toString()));
		} catch (Exception e) {
			log.warn("Unable to clean the temporary jpg folder for session {}", sid);
		}

	}
}