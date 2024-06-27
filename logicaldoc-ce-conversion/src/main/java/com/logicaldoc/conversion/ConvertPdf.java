package com.logicaldoc.conversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
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
import com.logicaldoc.core.security.user.User;
import com.logicaldoc.core.store.Storer;
import com.logicaldoc.util.Context;
import com.logicaldoc.util.io.FileUtil;
import com.logicaldoc.util.time.TimeDiff;
import com.logicaldoc.util.time.TimeDiff.TimeField;
import com.logicaldoc.web.util.ServletUtil;
//import com.logicalobjects.jlicense.license.LicenseManager;

/**
 * This servlet produces a Pdf from a document and stores it as a document's
 * resource. If the export was already performed it simply return it.
 * 
 * This servlet requires the following parameters:
 * <ul>
 * <li>docId</li>
 * <li>version</li>
 * </ul>
 * 
 * <br>
 * 
 * The produced conversion is stored as &lt;fileversion&gt;-conversion.pdf
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 4.5
 */
public class ConvertPdf extends HttpServlet {

	private static final String MERGE = "merge";

	private static final String VERSION = "version";

	private static final String FILE_VERSION = "fileVersion";

	private static final String DOCUMENT_ID = "docId";

	private static final long serialVersionUID = 1L;

	protected static Logger log = LoggerFactory.getLogger(ConvertPdf.class);

	/**
	 * Constructor of the object.
	 */
	public ConvertPdf() {
		super();
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
		// A temporary folder
		File tempDir = null;

		// A temporary file that will store the PDF to download
		File downloadedPdf = null;

        /*
		if (!LicenseManager.getInstance().isEnabled("Feature_8")) {
			log.error("Feature pdf conversion not enabled");
			try {
				response.getOutputStream().println("Feature not enabled");
			} catch (IOException e) {
				// Do nothing
			}
			return;
		}
		*/

		try {
			Session session = ServletUtil.validateSession(request);

			DocumentDAO docDao = (DocumentDAO) Context.get().getBean(DocumentDAO.class);
			VersionDAO versionDao = (VersionDAO) Context.get().getBean(VersionDAO.class);

			String downloadedPdfName = null;

			List<String> docIds = getDocIds(request);

			if (docIds.size() == 1) {
				long docId = Long.parseLong(docIds.get(0));
				Document originalDoc = docDao.findById(docId);
				Document document = docDao.findDocument(docId);

				validateDocument(docId, originalDoc, document, session);

				String fileName = document.getFileName();
				String fileVersion = document.getFileVersion();
				if (StringUtils.isNotEmpty(request.getParameter(FILE_VERSION))) {
					fileVersion = request.getParameter(FILE_VERSION);
					Version version = versionDao.findByVersion(document.getId(), fileVersion);
					if (version != null)
						fileName = version.getFileName();
				} else {
					String ver = getVersion(request, document);
					Version version = versionDao.findByVersion(document.getId(), ver);
					fileVersion = version.getFileVersion();
				}
				FormatConverterManager manager = (FormatConverterManager) Context.get()
						.getBean(FormatConverterManager.class);
				manager.convertToPdf(document, fileVersion, session.getSid());

				String suffix = getSuffix(fileName);

				boolean isPreview = "preview".equals(request.getParameter("control"));

				// Check the download permission
				boolean isDownloadAllowed = docDao.isDownloadEnabled(docId, session.getUserId());

				securityChecks(originalDoc, session, isPreview, isDownloadAllowed);

				checkHash(request, session, document, isPreview, isDownloadAllowed);

				// Remove the oldest preview attributes anyways
				removeSessionAttributes(request);

				saveHistory(originalDoc, document, fileVersion, isPreview, session);

				downloadedPdf = FileUtil.createTempFile("pdfconv", ".pdf");
				downloadedPdfName = fileName.toLowerCase().endsWith(".pdf") ? fileName : (fileName + ".pdf");
				Storer storer = (Storer) Context.get().getBean(Storer.class);
				String resource = storer.getResourceName(document, fileVersion, suffix);
				storer.writeToFile(document.getId(), resource, downloadedPdf);
			} else {
				// Copy the PDF conversions into a single temporary folder
				tempDir = preparePdfs(session.getUser(), docIds);

				File[] pdfs = getSortedPdfs(tempDir);

				// Merge all the PDFs
				downloadedPdf = mergePdf(pdfs);

				// As filename we can use the name of the folder
				Document doc = docDao.findById(Long.parseLong(docIds.get(0)));
				downloadedPdfName = doc.getFolder().getName() + ".pdf";

				// Add an history entry to track the export of the document
				saveDocumentHistories(session, docIds);
			}

			// Download the PDF
			ServletUtil.downloadFile(request, response, downloadedPdf, downloadedPdfName);
		} catch (Exception r) {
			handleError(request, response, r);
		} finally {
			FileUtil.strongDelete(tempDir);
			FileUtil.strongDelete(downloadedPdf);
		}
	}

	private void checkHash(HttpServletRequest request, Session session, Document document, boolean isPreview,
			boolean isDownloadAllowed) throws PermissionException {
		if (isPreview && !isDownloadAllowed) {
			/*
			 * We are in a preview but the user does not have the download
			 * permission
			 */

			/*
			 * In order to check if this is a real preview request, we check if
			 * the hash passes in the previewcheck parameters matches the hash
			 * of the document calculated again here.
			 */
			int docHash = 0;
			if (document.getDigest() != null)
				docHash = (document.getDigest() + document.getFileName()).hashCode();
			else
				docHash = document.getFileName().hashCode();

			int previewCheck = 0;
			try {
				previewCheck = Integer.parseInt(request.getParameter("previewcheck"));
			} catch (Exception t) {
				// Nothing to do
			}

			if (docHash == 0 || previewCheck == 0 || docHash != previewCheck)
				throw new PermissionException(
						String.format("User %s tried to preview document %s but the checks did not pass",
								session.getUsername(), document));
		}
	}

	private String getVersion(HttpServletRequest request, Document document) {
		String ver = document.getVersion();
		if (StringUtils.isNotEmpty(request.getParameter(VERSION)))
			ver = request.getParameter(VERSION);
		return ver;
	}

	private void saveDocumentHistories(Session session, List<String> docIds) throws PersistenceException {
		DocumentDAO docDao = (DocumentDAO) Context.get().getBean(DocumentDAO.class);
		for (String id : docIds) {
			DocumentHistory transaction = new DocumentHistory();
			transaction.setSession(session);
			transaction.setEvent(DocumentEvent.EXPORTPDF.toString());
			docDao.saveDocumentHistory(docDao.findById(Long.parseLong(id)), transaction);
		}
	}

	private File[] getSortedPdfs(File pdfsDir) {
		// Now collect and sort each PDF
		File[] pdfs = pdfsDir.listFiles();

		Arrays.sort(pdfs, (File file1, File file2) -> file1.getName().compareTo(file2.getName()));
		return pdfs;
	}

	private void validateDocument(long docId, Document originalDoc, Document document, Session session)
			throws FileNotFoundException, PersistenceException, PermissionException {
		if (document != null && !session.getUser().isMemberOf(Group.GROUP_ADMIN)
				&& !session.getUser().isMemberOf("publisher") && !document.isPublishing())
			throw new FileNotFoundException(String.format("Document %s not published", originalDoc));

		DocumentDAO dao = (DocumentDAO) Context.get().getBean(DocumentDAO.class);
		if (!dao.isReadEnabled(docId, session.getUserId()))
			throw new PermissionException(
					String.format("User %s cannot access the document %s", session.getUsername(), originalDoc));
	}

	private void saveHistory(Document originalDocument, Document document, String fileVersion, boolean isPreview,
			Session session) {
		DocumentDAO docDao = (DocumentDAO) Context.get().getBean(DocumentDAO.class);
		DocumentHistory transaction = new DocumentHistory();
		try {
			transaction.setSession(session);
			docDao.initialize(document);

			if (isPreview) {
				log.debug("Session {} previewing the document {}", session.getSid(), originalDocument);

				// Add an history entry to track the view of the
				// file
				transaction.setEvent(DocumentEvent.VIEWED.toString());
				docDao.saveDocumentHistory(document, transaction);
			} else {
				log.debug("Session {} converting into PDF the document {}", session.getSid(), originalDocument);

				// Add an history entry to track the PDF download of
				// the document
				if (fileVersion.equals(document.getFileVersion())) {
					transaction.setEvent(DocumentEvent.EXPORTPDF.toString());
					docDao.saveDocumentHistory(document, transaction);
				}
			}

			/*
			 * Record the download event also to enforce security checks
			 */
			DocumentHistory downloadTransaction = new DocumentHistory(transaction);
			downloadTransaction.setEvent(DocumentEvent.DOWNLOADED.toString());
			if (document.getFileName().toLowerCase().endsWith(".pdf"))
				downloadTransaction.setComment("caused by preview");
			else
				downloadTransaction.setComment("downloaded the pdf conversion");
			docDao.saveDocumentHistory(document, downloadTransaction);
		} catch (Exception t) {
			log.warn("Cannot save event {} for document {}", transaction.getEvent(), originalDocument);
		}
	}

	private List<String> getDocIds(HttpServletRequest request) {
		List<String> docIds = new ArrayList<>();
		String docIdParam = request.getParameter(DOCUMENT_ID);
		if (docIdParam.contains(",")) {
			StringTokenizer st = new StringTokenizer(docIdParam, ",", false);
			while (st.hasMoreTokens())
				docIds.add(st.nextToken());
		} else {
			docIds.add(docIdParam);
		}
		return docIds;
	}

	private String getSuffix(String fileName) {
		String suffix = FormatConverterManager.PDF_CONVERSION_SUFFIX;
		boolean isPdf = "pdf".equals(FileUtil.getExtension(fileName.toLowerCase()));
		if (isPdf) {
			// Serve the document itself
			suffix = null;
		}
		return suffix;
	}

	private void securityChecks(Document document, Session session, boolean isPreview, boolean isDownloadAllowed)
			throws PermissionException {
		if (!isPreview && !isDownloadAllowed)
			throw new PermissionException(
					String.format("User %s cannot download the document %s", session.getUsername(), document));
	}

	private void removeSessionAttributes(HttpServletRequest request) {
		try {
			@SuppressWarnings("unchecked")
			Enumeration<String> attributes = request.getSession().getAttributeNames();
			Date now = new Date();
			while (attributes.hasMoreElements()) {
				String name = attributes.nextElement();
				if (name.startsWith("document-") && request.getSession().getAttribute(name) != null) {
					Date previewDate = (Date) request.getSession().getAttribute(name);
					if (TimeDiff.getTimeDifference(previewDate, now, TimeField.SECOND) > 30) {
						request.getSession().removeAttribute(name);
						log.debug("Removed attribute {}", name);
					}
				}
			}
		} catch (Exception t) {
			log.warn(t.getMessage());
		}
	}

	private void handleError(HttpServletRequest request, HttpServletResponse response, Throwable error) {
		/**
		 * Here we may receive a lot of
		 * org.apache.catalina.connector.ClientAbortException but we are not
		 * interested in logging them as errors, just a lightweight warning
		 */
		if (!error.getClass().getSimpleName().equalsIgnoreCase("ClientAbortException"))
			log.error(error.getMessage(), error);
		else
			log.warn(error.getMessage());

		try {
			ServletUtil.setContentDisposition(request, response, "notavailable.pdf");
			try (InputStream is = ConvertPdf.class.getResourceAsStream("/pdf/notavailable.pdf");
					OutputStream os = response.getOutputStream();) {
				IOUtils.copy(is, os);
			}
		} catch (Exception t) {
			ServletUtil.sendError(response, t.getMessage());
		}
	}

	/**
	 * Convert a selection of documents into PDF and stores them in a temporary
	 * folder
	 * 
	 * @param user The current user
	 * @param docIds List of documents to be converted
	 * 
	 * @return The temporary folder
	 * 
	 * @throws IOException
	 */
	private File preparePdfs(User user, List<String> docIds) throws IOException {

		File tempDir = FileUtil.createTempDirectory(MERGE);

		DecimalFormat nf = new DecimalFormat("00000000");
		int i = 0;
		for (String docId : docIds) {
			try {
				i++;
				long id = Long.parseLong(docId);
				DocumentDAO dao = (DocumentDAO) Context.get().getBean(DocumentDAO.class);
				Document document = dao.findDocument(id);

				if ((document != null && !user.isMemberOf(Group.GROUP_ADMIN) && !user.isMemberOf("publisher")
						&& !document.isPublishing()) || (!dao.isReadEnabled(id, user.getId())))
					continue;

				FormatConverterManager manager = (FormatConverterManager) Context.get()
						.getBean(FormatConverterManager.class);
				manager.convertToPdf(document, null);

				File pdf = new File(tempDir, nf.format(i) + ".pdf");

				manager.writePdfToFile(document, null, pdf, null);
			} catch (Exception t) {
				log.error(t.getMessage(), t);
			}
		}
		return tempDir;
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			doGet(request, response);
		} catch (Exception t) {
			// Nothing to do
		}
	}

	/**
	 * Merges different PDFs into a single PDF-
	 * 
	 * @param pdfs ordered array of pdf files to be merged
	 * @return The merged Pdf file
	 * 
	 * @throws IOException
	 * @throws COSVisitorException
	 */
	private static File mergePdf(File[] pdfs) throws IOException {

		File temp = null;
		try {
			temp = FileUtil.createTempDirectory(MERGE);

			File dst = FileUtil.createTempFile(MERGE, ".pdf");

			PDFMergerUtility merger = new PDFMergerUtility();
			for (File file : pdfs) {
				merger.addSource(file);
			}

			merger.setDestinationFileName(dst.getAbsolutePath());
			MemoryUsageSetting memoryUsage = MemoryUsageSetting.setupTempFileOnly();
			memoryUsage.setTempDir(temp);
			merger.mergeDocuments(memoryUsage);

			return dst;
		} finally {
			FileUtil.strongDelete(temp);
		}
	}
}