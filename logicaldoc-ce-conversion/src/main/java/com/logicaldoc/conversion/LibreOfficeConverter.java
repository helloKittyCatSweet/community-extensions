package com.logicaldoc.conversion;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.JodConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaldoc.core.communication.EMail;
import com.logicaldoc.core.communication.MailUtil;
import com.logicaldoc.core.document.Document;
import com.logicaldoc.enterprise.thumbnail.EmailThumbnailBuilder;
import com.logicaldoc.util.SystemUtil;
import com.logicaldoc.util.exec.Exec;
import com.logicaldoc.util.io.FileUtil;

/**
 * A converter that uses OpenOffice/LibreOffice to convert different formats.
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 4.5
 */
public class LibreOfficeConverter extends CommunityAbstractFormatConverter {

	protected static Logger log = LoggerFactory.getLogger(LibreOfficeConverter.class);
	
	private static final String TASK_TIMEOUT = "taskTimeout";

	private static final String RUN_AS_DAEMON = "runAsDaemon";

	private static final List<String> EMAIL_EXTS = new ArrayList<>(Arrays.asList("eml", "msg"));

	public LibreOfficeConverter() {
		super();
	}

	@Override
	public void internalConvert(String sid, Document document, File src, File dest) throws IOException {
		super.checkFeature(dest);

		String srcExtension = FileUtil.getExtension(src.getName()).toLowerCase();
		if (EMAIL_EXTS.contains(srcExtension)) {
			if ("msg".equals(srcExtension)) {
				try {
					EMail email = MailUtil.msgToMail(src, false);
					if (!email.getMessageText().startsWith("<html") && (email.getMessageText().contains("\\rtf1")
							|| email.getMessageText().contains("\\htmlrtf"))) {
						// This is an RTF content
						File tmpRtf = FileUtil.createTempFile("rtfmail", ".rtf");
						try {
							FileUtil.writeFile(email.getMessageText(), tmpRtf.getAbsolutePath());
							convertWithLibreOffice(tmpRtf, dest);
							return;
						} finally {
							FileUtil.strongDelete(tmpRtf);
						}
					}
				} catch (Exception e) {
					// Nothing to do
				}

			}

			EmailThumbnailBuilder builder = new EmailThumbnailBuilder();
			File html = builder.createHtmlFile(src, srcExtension);
			try {
				convertWithLibreOffice(html, dest);
			} finally {
				FileUtils.deleteQuietly(html);
			}
		} else {
			convertWithLibreOffice(src, dest);
		}
	}

	private boolean isDaemon() {
		return "".equals(getParameter(RUN_AS_DAEMON)) || "true".equals(getParameter(RUN_AS_DAEMON));
	}

	/**
	 * Convert a file format supported by OpenOffice or LibreOffice
	 */
	protected synchronized void convertWithLibreOffice(File src, File dest) throws IOException {
		try {
			if (isDaemon()) {
				initOfficeManager();
				JodConverter.convert(src).to(dest).execute();
			} else {
				// Stop the daemon, if it was running
				LibreOfficeManager.stop();

				File tempDir = new File(System.getProperty("java.io.tmpdir"));
				String targetExtension = FileUtil.getExtension(dest.getName()).toLowerCase();
				List<String> cmd = new ArrayList<>();
				cmd.add(getParameter("path") + File.separatorChar + "program" + File.separatorChar + "soffice"
						+ (SystemUtil.isWindows() ? ".exe" : ""));
				cmd.add("--headless");
				cmd.add("--convert-to");
				cmd.add(targetExtension);
				cmd.add("--outdir");
				cmd.add(tempDir.getAbsolutePath());
				cmd.add(src.getAbsolutePath());

				// Lunch the conversion
				new Exec().exec2(cmd, tempDir, -1);
				File convertedFile = new File(tempDir, FileUtil.getBaseName(src.getName()) + "." + targetExtension);

				// Wait until when the conversion finishes
				try {
					long convertedFileSize = convertedFile.length();
					while (!convertedFile.exists() && convertedFileSize != convertedFile.length()) {
						convertedFileSize = convertedFile.length();
						synchronized (this) {
							wait(1000);
						}
					}

					// Sometimes if the LibreOffice is not executed as daemon,
					// the first page is blank
					if ("pdf".equalsIgnoreCase(targetExtension))
						removeFirstBlankPage(convertedFile);

					// Copy the file to the destination
					FileUtil.copyFile(convertedFile, dest);
				} finally {
					FileUtil.strongDelete(convertedFile);
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			throw new IOException("Unable to convert the file.", e);
		}
	}

	private void initOfficeManager() throws OfficeException {
		String prt = getParameter("ports");
		String[] tokens = prt.split("\\,");
		int[] ports = new int[tokens.length];
		for (int i = 0; i < tokens.length; i++)
			ports[i] = Integer.parseInt(tokens[i].trim());

		long taskTimeout = StringUtils.isNotEmpty(getParameter(TASK_TIMEOUT))
				? Integer.parseInt(getParameter(TASK_TIMEOUT)) * 1000L
				: 120000L;

		LibreOfficeManager.start(ports, taskTimeout, Integer.parseInt(getParameter("tasks")), getParameter("path"));
	}

	public void dispose() {
		LibreOfficeManager.stop();
	}

	@Override
	public List<String> getParameterNames() {
		return Arrays.asList("path", RUN_AS_DAEMON, "ports", "tasks", TASK_TIMEOUT);
	}

	/**
	 * Remove the first page if it is detected as blank
	 */
	private void removeFirstBlankPage(File pdfFile) throws IOException {
		PDDocument pdfDoc = PDDocument.load(pdfFile);
		try {
			// In case of just one page, do not remove anything
			if (pdfDoc.getNumberOfPages() < 2)
				return;

			PDFRenderer pdfRenderer = new PDFRenderer(pdfDoc);
			BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
			long count = 0;
			int height = bufferedImage.getHeight();
			int width = bufferedImage.getWidth();
			Double areaFactor = (width * height) * 0.99;

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					Color c = new Color(bufferedImage.getRGB(x, y));
					// verify light gray and white
					if (c.getRed() == c.getGreen() && c.getRed() == c.getBlue() && c.getRed() >= 248) {
						count++;
					}
				}
			}

			if (count >= areaFactor) {
				log.info("First page is blank, removed it.");
				pdfDoc.removePage(0);
				PDPageXYZDestination action = new PDPageXYZDestination();
				// WHen the PDF is open, go to the first page
				action.setPage(pdfDoc.getPage(0));
				pdfDoc.getDocumentCatalog().setOpenAction(action);
				pdfDoc.save(pdfFile);
			}
		} finally {
			pdfDoc.close();
		}
	}
}