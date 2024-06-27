package com.logicaldoc.conversion;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaldoc.util.io.FileUtil;
import com.openhtmltopdf.bidi.support.ICUBidiReorderer;
import com.openhtmltopdf.bidi.support.ICUBidiSplitter;
import com.openhtmltopdf.java2d.api.DefaultPageProcessor;
import com.openhtmltopdf.java2d.api.FSPageOutputStreamSupplier;
import com.openhtmltopdf.java2d.api.Java2DRendererBuilder;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

/**
 * Converts an HTML document into PDF document
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 7.4.1
 */
public class HTMLConverter extends CommunityAbstractFormatConverter {
	private static final String LOCAL_URL = "localUrl";

	private static final String USE_LIBREOFFICE = "useLibreOffice";

	private static final String FONTS = "fonts";

	protected static Logger log = LoggerFactory.getLogger(HTMLConverter.class);

	public HTMLConverter() {
		super();
	}

	@Override
	public List<String> getParameterNames() {
		return Arrays.asList(LOCAL_URL, USE_LIBREOFFICE, FONTS);
	}

	@Override
	public void internalConvert(String sid, com.logicaldoc.core.document.Document document, File src, File dest)
			throws IOException {
		super.checkFeature(dest);

		File htmlFile = FileUtil.createTempFile("html", ".html");
		FileUtil.copyFile(src, htmlFile);

		try {
			try {
				filterHTML(sid, htmlFile);
			} catch (Exception t) {
				log.error("Unable to parse the HTML document", t);
			}

			if ("true".equals(getParameter(USE_LIBREOFFICE))) {
				new LibreOfficeConverter().convertWithLibreOffice(htmlFile, dest);
			} else {
				convertHTML(htmlFile, dest);
			}
		} finally {
			FileUtils.deleteQuietly(htmlFile);
		}
	}

	private void convertHTML(File htmlInputFile, File destFile) throws IOException {
		try (OutputStream os = new FileOutputStream(destFile)) {
			if (destFile.getName().toLowerCase().endsWith(".pdf")) {
				PdfRendererBuilder builder = new PdfRendererBuilder();
				builder.withFile(htmlInputFile);
				builder.toStream(os);
				builder.useFastMode();

				/**
				 * These settings allows for good rendering of hebrew texts
				 */
				builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
				builder.useUnicodeBidiReorderer(new ICUBidiReorderer());
				builder.useHttpStreamImplementation(new HttpClientStreamFactory());

				useFonts(builder);

				builder.run();
			} else {
				DefaultPageProcessor processor = new DefaultPageProcessor(new FSPageOutputStreamSupplier() {
					@Override
					public OutputStream supply(int zeroBasedPageNumber) throws IOException {
						return os;
					}
				}, BufferedImage.TYPE_4BYTE_ABGR, "png");
				// For a white solid background use
				// BufferedImage.TYPE_3BYTE_BGR

				Graphics2D g2d = processor.createLayoutGraphics();

				Java2DRendererBuilder builder = new Java2DRendererBuilder();
				builder.withFile(htmlInputFile);
				builder.useLayoutGraphics(g2d);
				builder.toPageProcessor(processor);

				// Without this it does not work
				builder.useEnvironmentFonts(true);

				useFonts(builder);

				builder.runFirstPage();
				g2d.dispose();
			}
		}
	}

	private void useFonts(@SuppressWarnings("rawtypes")
	BaseRendererBuilder builder) {
		Map<String, String> fonts = getFonts();
		for (Map.Entry<String, String> entry : fonts.entrySet())
			builder.useFont(new File(entry.getValue()), entry.getKey());
	}

	private void filterHTML(String sid, File htmlFile) throws IOException {
		String content = FileUtil.readFile(htmlFile);

		// For more security convert each entity in their code
		content = convertEntities(htmlFile, content);

		// If the case enclose the content in the <html> element
		content = encloseInHTML(htmlFile, content);

		// Add the HTML declaration
		content = addHTMLDeclaration(htmlFile, content);
		if (log.isDebugEnabled())
			log.debug("Filtered HTML {}", StringUtils.abbreviate(content, 300));

		// Original Url - Rewrote Url
		Map<String, String> imageUrls = new HashMap<>();

		URL localUrl = getLocalURL();

		if (localUrl != null) {
			Document htmlDoc = Jsoup.parse(FileUtil.readFile(htmlFile));
			Elements elements = htmlDoc.getElementsByTag("img");

			// Iterate over all images 'src' values
			for (Element element : elements) {
				String srcVal = element.attr("src");
				String urlSpec = srcVal;
				if (urlSpec.contains("docId=") && urlSpec.contains("/download")) {
					// The URL downloads an image from LogicalDOC
					urlSpec = urlSpec.replace("docId=", "sid=" + sid + "&docId=");
					URL imageUrl = new URL(urlSpec);
					URL urlRewrite = new URL(localUrl.getProtocol(), localUrl.getHost(), localUrl.getPort(),
							"/download?" + imageUrl.getQuery());
					if (!imageUrls.containsKey(srcVal))
						imageUrls.put(srcVal, urlRewrite.toString());
				}
			}

			// Now replace the original URLs with the local counterpart
			for (Map.Entry<String, String> entry : imageUrls.entrySet()) {
				String spec = entry.getKey().replace("?", "\\?").replace(":", "\\:");
				FileUtil.replaceInFile(htmlFile.getAbsolutePath(), spec, entry.getValue());
			}
		}
	}

	private URL getLocalURL() {
		URL localUrl = null;
		try {
			localUrl = new URL(getParameter(LOCAL_URL));
		} catch (Exception t) {
			// Nothing to do
		}
		return localUrl;
	}

	private String convertEntities(File htmlFile, String content) throws IOException {
		Properties entities = new Properties();
		entities.load(this.getClass().getResourceAsStream("/htmlentities.properties"));
		for (Map.Entry<Object, Object> entry : entities.entrySet())
			content = content.replace("&" + entry.getKey().toString() + ";", entry.getValue().toString());
		FileUtil.writeFile(content, htmlFile.getAbsolutePath());
		return content;
	}

	private String addHTMLDeclaration(File htmlFile, String content) {
		if (!content.toUpperCase().contains("<!DOCTYPE")) {
			content = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
					+ content;
			FileUtil.writeFile(content, htmlFile.getPath());
		}
		return content;
	}

	private String encloseInHTML(File htmlFile, String content) {
		if (!content.toLowerCase().contains("<html")) {
			content = "<html><body>\n" + content + "\n</body></html>";
			FileUtil.writeFile(content, htmlFile.getAbsolutePath());
		}
		return content;
	}

	/**
	 * Elaborates the fonts parameter producing the map font family -> font file
	 */
	private Map<String, String> getFonts() {
		String fontSpec = getParameter(FONTS);
		Map<String, String> map = new HashMap<>();
		if (StringUtils.isNotEmpty(fontSpec)) {
			StringTokenizer st = new StringTokenizer(fontSpec, ",", false);
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				String[] spec = token.split("\\|");
				map.put(spec[0], spec[1]);
			}
		}
		return map;
	}
}