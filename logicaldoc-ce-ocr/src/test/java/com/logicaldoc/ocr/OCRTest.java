package com.logicaldoc.ocr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.logicaldoc.core.security.Tenant;
import com.logicaldoc.util.Context;

public class OCRTest extends AbstractOCRTestCase {

	@Before
	@Override
	public void setUp() throws FileNotFoundException, IOException, SQLException {
		super.setUp();

		OCRManager manager = (OCRManager) Context.get().getBean(OCRManager.class);
		manager.getEngines().put("Tesseract", new Tesseract());
	}

	@Test
	public void testJFIFImage() throws IOException {
		String filePath = URLDecoder.decode(getClass().getResource("/MissAmericana.jfif").getPath(), "UTF-8");

		File imgfile = new File(filePath);
		OCR ocr = ((OCRManager) Context.get().getBean(OCRManager.class)).getCurrentEngine();
		StringBuilder sb = new StringBuilder();
		ocr.extractText(imgfile, "eng", "default", sb, null);

		String recChars = sb.toString();

		assertNotNull(recChars);
		assertNotSame("", recChars.trim());
		assertTrue(recChars.length() > 2500);
	}

	@Test
	public void testTIFFMultiPage() throws IOException {
		String filePath = URLDecoder.decode(getClass().getResource("/multipage300dpiBW.tif").getPath(), "UTF-8");

		File imgfile = new File(filePath);
		OCR ocr = ((OCRManager) Context.get().getBean(OCRManager.class)).getCurrentEngine();
		StringBuilder sb = new StringBuilder();
		ocr.extractText(imgfile, "ita", "default", sb, null);

		String recChars = sb.toString();

		assertNotNull(recChars);
		assertNotSame("", recChars.trim());
		assertTrue(recChars.length() > 12000);
	}

	@Test
	public void testRecognizeTIFF_Deutch() throws IOException {
		// This is a Compressed TIF file 300dpi 1bit (b/n) CCITT T.4
		String filePath = URLDecoder.decode(getClass().getResource("/0742_0001.tif").getPath(), "UTF-8");

		File imgfile = new File(filePath);
		OCR ocr = ((OCRManager) Context.get().getBean(OCRManager.class)).getCurrentEngine();

		StringBuilder sb = new StringBuilder();
		ocr.extractText(imgfile, "deu", "default", sb, new OCRHistory());
		String recChars = sb.toString();

		assertNotNull(recChars);
	}

	@Test
	public void testRecognizeTIFF_Multipage() throws IOException {

		// This is a Multipage Compressed TIFF file 200dpi 1bit (b/n) CCITT T.4
		String filePath = URLDecoder.decode(getClass().getResource("/0750_0001.tif").getPath(), "UTF-8");

		File imgfile = new File(filePath);
		OCR ocr = ((OCRManager) Context.get().getBean(OCRManager.class)).getCurrentEngine();

		StringBuilder sb = new StringBuilder();
		ocr.extractText(imgfile, "ita", "default", sb, null);
		String recChars = sb.toString();

		assertNotNull(recChars);
	}

	@Test
	public void testRecognizeTIFF_LZW() throws IOException {

		// This is a Compressed TIF file 300dpi 1bit (b/n) LZW
		String filePath = URLDecoder.decode(getClass().getResource("/101D.tif").getPath(), "UTF-8");

		File imgfile = new File(filePath);
		OCR ocr = ((OCRManager) Context.get().getBean(OCRManager.class)).getCurrentEngine();

		StringBuilder sb = new StringBuilder();
		ocr.extractText(imgfile, "ita", "default", sb, null);
		String recChars = sb.toString();

		assertNotNull(recChars);
	}

	@Test
	public void testRecognizePDFImageCharacters01() throws UnsupportedEncodingException {
		// PDF with TIFF BW 300dpi
		String filePath = URLDecoder.decode(getClass().getResource("/fattura_300dpi_bw.pdf").getPath(), "UTF-8");

		File pdffile = new File(filePath);
		OCR ocr = ((OCRManager) Context.get().getBean(OCRManager.class)).getCurrentEngine();

		StringBuilder sb = new StringBuilder();

		String notThrownTest = null;
		try {
			ocr.extractPDFText(pdffile, "ita", "default", sb, new OCRHistory());
			notThrownTest = "ok";
		} catch (IOException e) {
			// Nothing to do
		}
		assertNotNull(notThrownTest);

		URLDecoder.decode(getClass().getResource("/1576_0001.pdf").getPath(), "UTF-8");

		pdffile = new File(filePath);

		sb = new StringBuilder();

		notThrownTest = null;
		try {
			ocr.extractPDFText(pdffile, "eng", "default", sb, new OCRHistory());
			notThrownTest = "ok";
		} catch (IOException e) {
			// Nothing to do
		}
		assertNotNull(notThrownTest);
	}

	public void testRecognizePDFImageCharacters(String resource, String language) throws IOException {
		String filePath = URLDecoder.decode(getClass().getResource(resource).getPath(), "UTF-8");
		File pdffile = new File(filePath);
		OCR ocr = ((OCRManager) Context.get().getBean(OCRManager.class)).getCurrentEngine();

		StringBuilder sb = new StringBuilder();

		String notThrownTest = null;
		try {
			ocr.extractPDFText(pdffile, language, "default", sb, null);
			notThrownTest = "ok";
		} catch (IOException e) {
			// Nothing to do
		}
		assertNotNull(notThrownTest);
	}

	public void testRecognizePDFImageCharacters02() throws IOException {
		testRecognizePDFImageCharacters("/scanned-text-200dpi.pdf", "eng");
	}

	@Test
	public void testRecognizePDFImageCharacters03() throws IOException {
		testRecognizePDFImageCharacters("/101D.pdf", "ita");
	}

	@Test
	public void testRecognizePDFImageCharacters04() throws IOException {
		testRecognizePDFImageCharacters("/F78.pdf", "ita");
	}

	@Test
	public void testRecognizePDFImageCharacters05() throws IOException {
		testRecognizePDFImageCharacters("/LogicalDOC - Manuale Utente 1.1.pdf", "ita");
	}

	@Test
	public void testRecognizePDF_TIFF() throws IOException {
		// PDF with TIFF 300dpi 1bit (B/W-Bitmap) with uncorrect rotation of
		// image [Canon iR2020]
		// Note: Tesseract v3.0 as well v2.0 does not support image deskew
		testRecognizePDFImageCharacters("/0752_0001.pdf", "ita");
	}

	@Test
	public void testRecognizePDF_Comp() throws IOException {

		// PDF Comp 300dpi 8bit (don't know if Comp stands 4 Composite or
		// compressed) with uncorrect skew of image [Canon iR2020]
		// This type of PDF can include images of different formats: jpg 8bit
		// and tiff 1bit
		// Note: Tesseract v3.0 as well v2.0 does not support image deskew
		testRecognizePDFImageCharacters("/0753_0001.pdf", "ita");
	}

	@Test
	public void testRecognizePDF_Bando() throws IOException {
		testRecognizePDFImageCharacters("/bando_300dpi_bw.pdf", "ita");
	}

	class TIFFFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			String nameL = name.toLowerCase();
			return (nameL.endsWith(".tiff") || nameL.endsWith(".tif"));
		}
	}

	@Test
	public void testCorruptedImages() throws IOException {
		// TIFF with JPEG Compression AND Manually Corrupted !!
		// This throws a RuntimeException
		String filePath = URLDecoder.decode(getClass().getResource("/CorruptedImage.tif").getPath(), "UTF-8");

		File imgfile = new File(filePath);
		OCR ocr = ((OCRManager) Context.get().getBean(OCRManager.class)).getCurrentEngine();

		StringBuilder sb = new StringBuilder();

		// This invocation must fail
		ocr.extractText(imgfile, "ita", "default", sb, null);
		assertEquals("", sb.toString());

		// GIF Manually Corrupted !!
		// This throws a RuntimeException
		filePath = URLDecoder.decode(getClass().getResource("/CorruptedGIFimage.gif").getPath(), "UTF-8");

		imgfile = new File(filePath);
		sb = new StringBuilder();

		// This invocation must fail
		ocr.extractText(imgfile, "ita", "default", sb, null);
		assertEquals("", sb.toString());
	}

	@Test
	public void testRecognizeImageIOCharacters() throws IOException {
		String filePath = URLDecoder.decode(getClass().getResource("/test200dpi24bit.jpg").getPath(), "UTF-8");

		File imgfile = new File(filePath);
		OCR ocr = ((OCRManager) Context.get().getBean(OCRManager.class)).getCurrentEngine();
		StringBuilder sb = new StringBuilder();
		ocr.extractText(imgfile, "ita", "default", sb, null);
		String recChars = sb.toString();

		assertNotNull(recChars);
		assertTrue(recChars.length() > 2000);

		// JPEG with 100 dpi 24 bit
		filePath = URLDecoder.decode(getClass().getResource("/scanned-text-100dpi.jpg").getPath(), "UTF-8");

		imgfile = new File(filePath);

		// JPEG with 200 dpi 24 bit
		filePath = URLDecoder.decode(getClass().getResource("/scanned-text-200dpi.jpg").getPath(), "UTF-8");
		imgfile = new File(filePath);

		// Choose null will default to the eng language
		sb = new StringBuilder();
		ocr.extractText(imgfile, null, Tenant.DEFAULT_NAME, sb, null);
		String recChars3 = sb.toString();

		assertNotNull(recChars3);
		assertTrue(recChars3.length() > 1000);
	}

	@Test
	public void testImageIONotExists() {
		// Image file NOT Exists
		File imgfile = new File("/C:/tmp/notExists.jpg");
		OCR ocr = ((OCRManager) Context.get().getBean(OCRManager.class)).getCurrentEngine();

		String errorCheck = null;
		try {
			StringBuilder sb = new StringBuilder();

			// This invocation must result in an exception
			ocr.extractText(imgfile, "ita", "default", sb, null);
			errorCheck = "ok";
		} catch (Exception e) {
			// Nothing to do
		}

		assertNull(errorCheck);
	}

	@Test
	public void testVanMark() throws IOException {
		String filePath = URLDecoder.decode(getClass().getResource("/IM1065.101669.pdf").getPath(), "UTF-8");

		File imgfile = new File(filePath);
		OCR ocr = ((OCRManager) Context.get().getBean(OCRManager.class)).getCurrentEngine();
		StringBuilder sb = new StringBuilder();
		ocr.extractPDFText(imgfile, "eng", "default", sb, null);
		String recChars = sb.toString();
		assertNotNull(recChars);
		assertTrue(recChars.contains("U035900W"));
	}
}