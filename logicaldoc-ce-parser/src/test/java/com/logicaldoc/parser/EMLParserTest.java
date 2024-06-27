package com.logicaldoc.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.junit.Assert;
import org.junit.Test;

import com.logicaldoc.core.parser.ParseException;
import com.logicaldoc.core.security.Tenant;

public class EMLParserTest extends AbstractParserTestCase {

	StringBuilder sb;

	@Test
	public void testParseEMLFolder() throws ParseException {
		File dir = new File("target/test-classes/thunderbird");
		String[] files = dir.list(new SuffixFileFilter(".eml"));

		String notThrownTest = null;
		try {
			for (int i = 0; i < files.length; i++) {
				File file = new File(dir, files[i]);

				EMLParser parser = new EMLParser();
				String content = parser.parse(file, file.getName(), null, null, Tenant.DEFAULT_NAME);

				System.out.println("content: \n" + content);

				sb = new StringBuilder();
				sb.append(content);

				saveFile2(file.getAbsolutePath());
			}
			notThrownTest = "ok";
		} catch (IOException e) {
			// Nothing to do
		}
		Assert.assertNotNull(notThrownTest);
	}

	private File processFileName(String filename) {
		// Do no overwrite existing file
		File file = new File(filename);

		String fname = file.getName();
		fname = fname.substring(0, fname.lastIndexOf("."));
		fname += ".txt";
		file = new File(file.getParent(), fname);

		return file;
	}

	public void saveFile2(String filename) throws IOException, ParseException {

		if (sb.length() == 0)
			return;

		File file = processFileName(filename);

		OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		BufferedWriter bw = new BufferedWriter(output);

		char[] sss = new char[sb.length()];
		sb.getChars(0, sb.length() - 1, sss, 0);

		CharArrayReader car = new CharArrayReader(sss);
		BufferedReader br = new BufferedReader(car);
		String inputLine;
		while ((inputLine = br.readLine()) != null) {
			bw.write(inputLine);
			bw.newLine();
		}

		br.close();
		bw.close();
	}
}