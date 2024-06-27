package com.logicaldoc.ocr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.logicaldoc.util.exec.Exec;
import com.logicaldoc.util.io.FileUtil;

/**
 * Specific implementation that uses the Tesseract OCR.
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 3.0
 */
public class Tesseract extends OCR {

	@Override
	public List<String> getParameterNames() {
		return Arrays.asList("path");
	}

	@Override
	protected void runOCR(File tmpImage, String lang, StringBuilder sb) throws IOException {
		if (lang != null && lang.startsWith("zh"))
			lang = "chi_sim";

		String text = runOCR(tmpImage, lang);
		if (StringUtils.isEmpty(text))
			text = runOCR(tmpImage, null);

		if (StringUtils.isNotEmpty(text)) {
			sb.append("\n");
			sb.append(text);
		}
	}

	private String runOCR(File tmpImage, String lang) {
		File ocrCommand = new File(getParameter("path"));

		String outFilePath = System.getProperty("java.io.tmpdir") + File.separator + "ocr-"
				+ System.currentTimeMillis();
		outFilePath = FilenameUtils.normalize(outFilePath);
		File outFile = new File(outFilePath + ".txt");

		try {
			if (lang != null && lang.startsWith("zh"))
				lang = "chi_sim";
			if (StringUtils.isEmpty(lang) || lang.equals("standard"))
				lang = "eng";

			List<String> cmd = new ArrayList<>();
			cmd.add(ocrCommand.getAbsolutePath());
			cmd.add(tmpImage.getAbsolutePath());
			cmd.add(outFilePath);
			cmd.add("-l");
			cmd.add(lang);

			new Exec().exec2(cmd, null, getBatchTimeout());

			wait(outFile);

			return FileUtil.readFile(outFile);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return "";
		} catch (Exception t) {
			log.error(t.getMessage(), t);
			return "";
		} finally {
			FileUtils.deleteQuietly(outFile);
		}
	}

	private synchronized void wait(File outFile) throws InterruptedException {
		int count = 0;
		while (!outFile.exists() && count < 20) {
			count++;
			this.wait(1000);
		}
	}
}