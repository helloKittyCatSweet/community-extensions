package com.logicaldoc.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaldoc.core.PersistenceException;
import com.logicaldoc.core.folder.FolderDAO;
import com.logicaldoc.core.imaging.ImageUtil;
import com.logicaldoc.core.util.PDFImageExtractor;
import com.logicaldoc.util.Context;
import com.logicaldoc.util.config.ContextProperties;
import com.logicaldoc.util.exec.Exec;
import com.logicaldoc.util.io.FileUtil;
import com.logicaldoc.util.time.TimeDiff;
import com.logicaldoc.util.time.TimeDiff.TimeField;

/**
 * 
 * This OCR engine is capable of recognizing characters (letter and numbers)
 * accurately
 * 
 * @author Alessandro Gasparini
 */
public abstract class OCR {

	private static final String OCR_NOT_ENABLED = "OCR not enabled";

	protected static Logger log = LoggerFactory.getLogger(OCR.class);

	protected static final String IMAGE_PREFIX = "image";

	protected Map<String, String> parameters = new HashMap<>();

	private int maxThreads = 0;

	private Semaphore threadSemaphore;

	protected OCR() {
		initThreadSemaphore();
	}

	public void loadParameters() {
		try {
			ContextProperties config = Context.get().getProperties();
			List<String> params = getParameterNames();
			for (String param : params) {
				String key = getParameterPropertyName(param);
				parameters.put(param, config.getProperty(key));
			}
		} catch (Exception t) {
			// Nothing to do
		}
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public String getParameter(String name) {
		return getParameters().get(name);
	}

	public List<String> getParameterNames() {
		return new ArrayList<>();
	}

	private String getParameterPropertyName(String paramName) {
		return "ocr." + this.getClass().getSimpleName() + "." + paramName;
	}

	public boolean isAvailable() {
		return true;
	}

	/**
	 * Runs the OCR
	 * 
	 * @param tmpImage The temporary tiff to be processed
	 * @param lang Language of the document in ISO3 (ISO 639-2) lowercase (Can
	 *        increase accuracy). The default language is eng (English).
	 * @param buffer the buffer that will receive the extracted text
	 * @throws IOException
	 */
	protected abstract void runOCR(File tmpImage, String lang, StringBuilder buffer) throws IOException;

	/**
	 * Extracts the text from PDF file
	 * 
	 * @param pdffile the file to ocr
	 * @param lang the language in which the document is written
	 * @param tenant name of the tenant
	 * @param buffer the buffer to store the extracted text
	 * @param transaction informations about the indexing transaction
	 * 
	 * @throws IOException In case of OCR error
	 */
	public void extractPDFText(File pdffile, String lang, String tenant, StringBuilder buffer, OCRHistory transaction)
			throws IOException {
		if (!isAvailable()) {
			log.warn(OCR_NOT_ENABLED);
			return;
		}

		checkFileExistence(pdffile);

		// extract all the images and launch the OCR process for every image
		try (PDFImageExtractor pdfReader = new PDFImageExtractor(pdffile);) {
			long maxWait = getMaxThreadsWait();
			if (threadSemaphore.tryAcquire(maxWait, TimeUnit.SECONDS)) {
				extractPDFText(pdffile, pdfReader, lang, tenant, buffer);
			} else {
				throw new IOException(String.format("Cannot access the OCR within %d seconds", maxWait));
			}

			saveTransaction(buffer, transaction);
		} catch (InterruptedException e) {
			log.warn(e.getMessage());
			Thread.currentThread().interrupt();
		} catch (Exception ex) {
			if (transaction != null) {
				transaction.setEvent(OCRHistory.EVENT_OCR_FAILURE);
				transaction.setComment(ex.getMessage());
				recordHistory(transaction);
			}
			throw ex;
		} finally {
			if (threadSemaphore != null)
				threadSemaphore.release();
		}
	}

	private void extractPDFText(File pdffile, PDFImageExtractor pdfReader, String lang, String tenant,
			StringBuilder buffer) throws IOException {
		// From time to time the number of allowed thread may change
		// so check
		// and initialize the semaphore
		initThreadSemaphore();

		// Iterate over pages
		int pageTotal = pdfReader.getNumberOfPages();

		/**
		 * In order to optimize OCR invocations, render batch of pages into
		 * single compressed multi-layer TIFFs
		 */
		int batch = Context.get().getProperties().getInt("ocr.batch", 5);
		if (batch <= 0)
			batch = 1;

		Date startTime = new Date();
		int page = 1;
		while (page <= pageTotal) {
			long elapsedTime = TimeDiff.getTimeDifference(startTime, new Date(), TimeField.SECOND);
			if (elapsedTime > getTimeout()) {
				if (log.isWarnEnabled())
					log.warn("OCR interrupted after {}", TimeDiff.printDuration(elapsedTime));
				break;
			}

			int startPage = page;
			int lastPage = (page + batch) > pageTotal ? pageTotal : (page + batch - 1);
			File tmp = FileUtil.createTempFile("render", ".tif");
			try {
				renderPages(pdffile, tmp, startPage, lastPage,
						Context.get().getProperties().getBoolean("ocr.cropImage", false), false);

				ocrImage(tmp, lang, tenant, buffer);

				buffer.append("\n\n");
			} catch (Exception e) {
				log.warn("WARNING: OCR extractPDFText(): {}", e.getMessage());
			} finally {
				FileUtils.deleteQuietly(tmp);
			}
			page += batch;
		}

		failOnEmptyExtraction(tenant, buffer);
	}

	private void failOnEmptyExtraction(String tenant, StringBuilder buffer) throws IOException {
		if (Context.get().getProperties().getBoolean(tenant + ".ocr.erroronempty", true)
				&& StringUtils.isEmpty(buffer.toString()))
			throw new IOException("Cannot extract texts");
	}

	private void saveTransaction(StringBuilder buffer, OCRHistory transaction) {
		if (transaction != null) {
			transaction.setEvent(OCRHistory.EVENT_OCR_SUCCESS);
			transaction.setComment(buffer.toString());
			recordHistory(transaction);
		}
	}

	private void checkFileExistence(File pdffile) {
		if (pdffile == null || !pdffile.exists())
			throw new IllegalArgumentException(pdffile + ": IS null or not Exists");
	}

	private void recordHistory(OCRHistory history) {
		if (history != null && Context.get().getProperties().getBoolean("ocr.events.record", true)) {
			FolderDAO fDao = (FolderDAO) Context.get().getBean(FolderDAO.class);
			OCRHistoryDAO dao = (OCRHistoryDAO) Context.get().getBean(OCRHistoryDAO.class);
			try {
				if (OCRHistory.EVENT_OCR_SUCCESS.equals(history.getEvent()))
					history.setComment(StringUtils.abbreviate(history.getComment(),
							Context.get().getProperties().getInt("ocr.events.maxtext", 100)));
				if (history.getFolderId() != null)
					history.setPath(fDao.computePathExtended(history.getFolderId()));
				dao.store(history);
			} catch (PersistenceException e) {
				log.warn(e.getMessage(), e);
			}
		}
	}

	public void extractText(File imgfile, String lang, String tenant, StringBuilder sb, OCRHistory transaction)
			throws IOException {
		if (!isAvailable()) {
			log.warn(OCR_NOT_ENABLED);
			return;
		}

		// From time to time the number of allowed thread may change so check
		// and initialize the semaphore
		initThreadSemaphore();

		try {
			long maxWait = getMaxThreadsWait();
			if (threadSemaphore.tryAcquire(maxWait, TimeUnit.SECONDS)) {
				ocrImage(imgfile, lang, tenant, sb);

				failOnEmptyExtraction(tenant, sb);
			} else
				throw new IOException(String.format("Cannot access the OCR within %d seconds", maxWait));

			if (transaction != null) {
				transaction.setEvent(OCRHistory.EVENT_OCR_SUCCESS);
				transaction.setComment(StringUtils.abbreviate(sb.toString(), 100));
				recordHistory(transaction);
			}
		} catch (InterruptedException e) {
			log.warn(e.getMessage());
			Thread.currentThread().interrupt();
		} catch (Exception ex) {
			if (transaction != null) {
				transaction.setEvent(OCRHistory.EVENT_OCR_FAILURE);
				transaction.setComment(ex.getMessage());
				recordHistory(transaction);
			}
			throw ex;
		} finally {
			threadSemaphore.release();
		}
	}

	private void ocrImage(File imgfile, String lang, String tenant, StringBuilder sb) throws IOException {
		if (!isAvailable()) {
			log.warn(OCR_NOT_ENABLED);
			return;
		}

		if (imgfile == null || (!imgfile.exists()))
			throw new IllegalArgumentException(imgfile + ": IS null or does not Exists");

		try {
			BufferedImage bimg = ImageIO.read(imgfile);
			int threshold = getResolutionThreshold(tenant);
			if (bimg.getWidth() < threshold && bimg.getHeight() < threshold) {
				log.debug("Image skipped because too small");
				return;
			}
		} catch (Exception t) {
			// Nothing to do
		}

		loadParameters();

		// From time to time the number of allowed thread may change so check
		// and initialize the semaphore
		initThreadSemaphore();

		runOCR(imgfile, lang, sb);
	}

	public int getResolutionThreshold(String tenant) {
		if (StringUtils.isEmpty(tenant))
			return 600;
		return Context.get().getProperties().getInt(tenant + ".ocr.resolution.threshold", 600);
	}

	/**
	 * Renders a PDF page into a destination monochrome png. Makes use of
	 * GhostScript.
	 * 
	 * @param src the source file
	 * @param dst the rendered file
	 * @param firstPage The first page to render(starts from 1)
	 * @param lastPage index of the last page
	 * @param cropImage True if the source image should be cropped to retain
	 *        just the visible content
	 * @param barcodes True if the rendering must be optimized for barcodes
	 *        detection
	 * 
	 * @throws IOException Error in I/O
	 */
	private void renderPages(File src, File dst, int firstPage, int lastPage, boolean cropImage, boolean barcodes)
			throws IOException {
		ContextProperties config = Context.get().getProperties();

		String resolution = barcodes ? config.getProperty("ocr.rendres.barcode") : config.getProperty("ocr.rendres");
		if (StringUtils.isEmpty(resolution))
			resolution = "300";
		String device = barcodes ? "pngmono" : "tiffgray -sCompression=lzw";

		String srcPath = new Exec().normalizePathForCommand(src.getPath());

		File tmpFile = null;
		try {
			String dstPath = new Exec().normalizePathForCommand(dst.getPath());

			if (cropImage) {
				tmpFile = FileUtil.createTempFile("render", barcodes ? ".png" : ".tif");
				dstPath = new Exec().normalizePathForCommand(tmpFile.getPath());
			}

			String command = new Exec().normalizePathForCommand(
					new File(config.getProperty("converter.GhostscriptConverter.path")).getPath());
			command += " -q -dBATCH -dNOPAUSE -r" + resolution + " -sDEVICE=" + device + "  -dFirstPage=" + firstPage
					+ " -dLastPage=" + lastPage + "  -sOutputFile=" + dstPath + " " + srcPath;

			if (log.isDebugEnabled())
				log.debug("Executing: {}", command);
			try {
				new Exec().exec(command, null, null, getBatchTimeout());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			// Crop the rendered image
			if (tmpFile != null)
				ImageUtil.cropVisibleContent(tmpFile, dst);
		} finally {
			FileUtil.strongDelete(tmpFile);
		}
	}

	/**
	 * Initializes the semaphore to allow a maximum number of threads, tht is
	 * specified by the setting <code>ocr.threads</code>
	 */
	private synchronized void initThreadSemaphore() {
		int threads = Context.get().getProperties().getInt("ocr.threads", 1);
		if (threads != maxThreads) {
			maxThreads = threads;
			threadSemaphore = new Semaphore(maxThreads);
		}
	}

	public static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		// windows
		return (os.indexOf("win") >= 0);

	}

	/**
	 * Gets the timeout of a single batch
	 * 
	 * @return the batch timeout in seconds
	 */
	protected int getBatchTimeout() {
		return Context.get().getProperties().getInt("ocr.timeout.batch", 120);
	}

	/**
	 * Gets the timeout of the whole OCR process
	 * 
	 * @return the batch timeout in seconds
	 */
	protected int getTimeout() {
		return Context.get().getProperties().getInt("ocr.timeout", 60);
	}

	/**
	 * Gets the max number of seconds to wait a thread slot
	 * 
	 * @return max number of second to wait for a thread slot
	 */
	private long getMaxThreadsWait() {
		return Context.get().getProperties().getLong("ocr.threads.wait", 60L);
	}
}