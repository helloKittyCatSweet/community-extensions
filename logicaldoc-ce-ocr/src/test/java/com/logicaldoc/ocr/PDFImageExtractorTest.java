package com.logicaldoc.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.logicaldoc.core.util.PDFImageExtractor;

public class PDFImageExtractorTest {

	File destFolder = null;

	@Before
	public void setUp() throws FileNotFoundException, IOException, SQLException {
		destFolder = new File("target", "destFolder");
		if (!destFolder.exists())
			destFolder.mkdir();
	}

	@Test
	public void testGetNumberOfPages() throws IOException {
		String filePath = URLDecoder.decode(getClass().getResource("/exportPDF_OFwImage.pdf").getPath(), "UTF-8");
		File pdffile = new File(filePath);
		PDFImageExtractor pdfReader = new PDFImageExtractor(pdffile);
		int pagesNumb = pdfReader.getNumberOfPages();
		pdfReader.close();
		Assert.assertEquals(1, pagesNumb);
	}

	@Test
	public void testExtactImageOO2_4() throws IOException {
		// PDF creato con OpenOffice 2.4 su Win Vista da img 24bit 200dpi .jpg
		extractImages("exportPDF_OFwImage");
	}

//  This test breacks because of OutOfMemory
//	@Test
//	public void testExtactImageOO2_3() throws IOException {
//		// PDF creato con OpenOffice 2.3
//		// Versione PDF 1.4 (Acrobat 5.x)
//		extractImages("LogicalDOC - Manuale Utente 1.1");
//
//		System.out.println("Total Memory: " + Runtime.getRuntime().totalMemory());
//		System.out.println("Free Memory: " + Runtime.getRuntime().freeMemory());
//	}

	@Test
	public void testExtactImagesCanoniR2020i() throws IOException {
		// PDF creato con Multifunzione Canon iR2020i
		extractImages("F78");
	}

	@Test
	public void testExtactImagesCanonScan01() throws IOException {
		// PDF creato con CanonScan Lide
		extractImages("File0001_Color200dpi");
	}

	@Test
	public void testExtactImagesCanonScan02() throws IOException {
		// PDF creato con CanonScan Lide
		extractImages("File0002_BW300dpi");
	}

	@Test
	public void testExtactImagesCanonScan03() throws IOException {
		// PDF creato con CanonScan Lide
		extractImages("File0003_BW300dpi_multi");
	}

	@Test
	public void testExtactImagesCanonScan04() throws IOException {
		// PDF creato con CanonScan Lide
		extractImages("File0004_Grays200dpi_multi");
	}

	@Test
	public void testExtactImagesHP01() throws IOException {
		// PDF creato con HP OfficeJET All-in-one
		extractImages("bando_300dpi_bw");
	}

	@Test
	public void testExtactImagesHP02() throws IOException {
		// PDF creato con HP OfficeJET All-in-one
		extractImages("fattura_300dpi_bw");
	}

	@Test
	public void testExtactImageTHBPdf01() throws IOException {
		extractImages("scanned-text-200dpi");
	}

	@Test
	public void testExtactImageTHBPdf02() throws IOException {
		extractImages("feature");
	}

	@Test
	public void testExtactImageCutePDF01() throws IOException {
		// PDF creato con CutePDF free 3.2 TIFF 150dpi 8bit
		extractImages("Firme_timbro");
	}

	@Test
	public void testRotatedPages() throws IOException {
		extractImages("page_rotate");
		extractImages("Solivettimf08101008500");
	}

	public void extractImages(String prefix) throws IOException {
		String filePath = URLDecoder.decode(getClass().getResource("/" + prefix + ".pdf").getPath(), "UTF-8");

		File pdffile = new File(filePath);

		PDFImageExtractor pdfReader = new PDFImageExtractor(pdffile);
		try {
			List<BufferedImage> imgs = pdfReader.extractImages();

			Assert.assertNotNull(imgs);
			Assert.assertTrue(imgs.size() > 0);

			for (int i = 0; i < imgs.size(); i++) {
				// System.err.println(imgs.get(i).getType());
				File destFile = new File(destFolder, prefix + "_" + i + ".bmp");
				ImageIO.write(imgs.get(i), "bmp", destFile);
			}
		} finally {
			pdfReader.close();
		}
	}
}