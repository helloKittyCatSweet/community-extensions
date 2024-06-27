package com.logicaldoc.parser;

import java.io.IOException;

import com.pff.PSTException;

public class ParserTestbench {
	public static void main(String[] args) throws IOException, PSTException {

//		// Open a DOC file
//		File file = new File("C:\\tmp\\IO_6.1.2.3_00.doc");
//		HWPFDocument document = new HWPFDocument(new FileInputStream(file));
//		Range range = document.getHeaderStoryRange();
//
//		Section section = range.getSection(0);
//		System.out.println(section.numParagraphs() + " , " + section.numSections());
//
//		for (int i = 0; i < section.numParagraphs(); i++) {
//			Paragraph p = section.getParagraph(i);
//			if (i == 13)
//				System.out.println(i + ": " + p.text());
//		}
//
//		// Open a XLSM file
//		try (FileInputStream is = new FileInputStream(new File("C:\\tmp\\metadata_sample.xlsm"));) {
//			System.out.println("found file");
//			XSSFWorkbook workbook = new XSSFWorkbook(is);
//			System.out.println("in workbook");
//			XSSFSheet sheet = workbook.getSheet("MetaData");
//			System.out.println("got sheet");
//			int firstRow = sheet.getFirstRowNum();
//			int lastRow = sheet.getLastRowNum();
//
//			for (int i = firstRow; i <= lastRow; i++) {
//				XSSFRow row = sheet.getRow(i);
//				if (row == null)
//					continue;
//				XSSFCell cell = row.getCell(3);
//
//				System.out.println("Cell3: " + cell.getRawValue());
//
//				cell = row.getCell(2);
//				if (cell != null)
//					cell.setCellValue("1213");
//				System.out.println("Modified Cell2");
//			}
//		}

		// workbook.write(new FileOutputStream(new
		// File("C:\\tmp\\metadata_sample_written.xlsm")));
	}
}