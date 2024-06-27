package com.logicaldoc.conversion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;


import com.openhtmltopdf.java2d.api.BufferedImagePageProcessor;
import com.openhtmltopdf.java2d.api.Java2DRendererBuilder;

public class EnterpriseConversionWorkbench {
	final static String BASE = "http://localhost:9080/services";

	public static void main(String[] args) throws Exception {

		// testLibreOffice();
		// testConvertio();
		// testSoapWebservice();
		// testRestWebservice();
		// testHebrew();
		testHTML2Png();
	}

	static void testHTML2Png() throws IOException {
		Java2DRendererBuilder builder = new Java2DRendererBuilder();
		builder.withHtmlContent("<html><body>pippo</body></html>", "https://localhost:8080");
		builder.useFastMode();
		builder.useEnvironmentFonts(true);

		BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(
				BufferedImage.TYPE_INT_RGB, 1.0);

		builder.toSinglePage(bufferedImagePageProcessor);
		try {
			builder.runFirstPage();
			ImageIO.write(bufferedImagePageProcessor.getPageImages().get(0), "png",
					new FileOutputStream("target/out.png"));
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	static void testHebrew() throws IOException {
		HTMLConverter converter = new HTMLConverter();
		converter.convert(null, null, new File("C:\\tmp\\hebrew.html"), new File("C:\\tmp\\hebrew.pdf"));
	}

	static void testLibreOffice() throws IOException {
		LibreOfficeConverter converter = new LibreOfficeConverter();
		converter.convert(null, null, new File("C:\\tmp\\diff.html"), new File("C:\\tmp\\diff.pdf"));
	}

}