package com.logicaldoc.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaldoc.core.communication.EMail;
import com.logicaldoc.core.communication.EMailAttachment;
import com.logicaldoc.core.communication.MailUtil;
import com.logicaldoc.core.parser.AbstractParser;
import com.logicaldoc.core.parser.HTMLParser;
import com.logicaldoc.core.parser.ParseException;
import com.logicaldoc.core.parser.ParseParameters;
import com.logicaldoc.util.Context;
import com.logicaldoc.util.charset.CharsetUtil;
import com.logicaldoc.util.io.FileUtil;
//import com.logicaldoc.util.io.IOUtil;
import org.apache.commons.io.IOUtils;

/**
 * Parses Mozilla .eml files
 * 
 * @author Alessandro Gasparini - LogicalDOC
 * @since 4.6
 */
public class EMLParser extends AbstractParser {

	protected static Logger log = LoggerFactory.getLogger(EMLParser.class);

	private StringBuilder sb;

	private boolean multipartAlternative;

	private boolean extractHeaders = true;

	public boolean isExtractHeaders() {
		return extractHeaders;
	}

	public void setExtractHeaders(boolean extractHeaders) {
		this.extractHeaders = extractHeaders;
	}

	private void extractHeaders(MimeMessage message) throws MessagingException {
		if (!extractHeaders)
			return;

		// Print sender From
		Address[] address = message.getFrom();
		printAddress(address);

		// Print Recipients TO
		address = message.getRecipients(RecipientType.TO);
		printAddress(address);

		// Print Recipients TO
		address = message.getRecipients(RecipientType.CC);
		printAddress(address);

		// Print Recipients TO
		address = message.getRecipients(RecipientType.BCC);
		printAddress(address);

		// replace all '<' and '>' characters with space
		int chi = 0;
		while ((chi = sb.indexOf("<")) != -1) {
			sb.setCharAt(chi, ' ');
		}
		while ((chi = sb.indexOf(">")) != -1) {
			sb.setCharAt(chi, ' ');
		}

		// Add Subject
		sb.append(message.getSubject()).append('\n');
	}

	private void printAddress(Address[] address) {
		if (address != null) {
			for (int i = 0; i < address.length; i++) {
				if (address[i] instanceof InternetAddress ia) {
					sb.append(ia.toUnicodeString()).append('\n');
				} else {
					sb.append(address[i]).append('\n');
				}
			}
		}
	}

	private void handleMultipart(Multipart multipart) throws MessagingException, IOException {

		if (multipart.getContentType().indexOf("multipart/alternative") != -1)
			multipartAlternative = true;

		for (int i = 0, n = multipart.getCount(); i < n; i++) {
			try {
				handlePart(multipart.getBodyPart(i));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	private void handlePart(Part part) throws MessagingException, IOException, ParseException {
		String disposition = part.getDisposition();
		String contentType = part.getContentType();

		if (isDispositionInline(disposition)) {
			// When just body
			log.debug("contentType: {}", contentType);
			// Check if plain
			if (contentType.startsWith("text/plain")) {
				String charset = CharsetUtil.getCharset(contentType);
				//byte[] pbs = IOUtil.getBytesOfStream(part.getInputStream());
				byte[] pbs = IOUtils.toByteArray(part.getInputStream());
				sb.append(new String(pbs, charset)).append(" ");
			} else if (contentType.startsWith("text/html")) {
				if (!multipartAlternative) {
					try {
						handleHTMLContent(part);
					} catch (RuntimeException e) {
						log.error(e.getMessage());
					}
				}
			} else if (contentType.startsWith("multipart")) {
				Object content = part.getContent();
				if (content instanceof Multipart multipart) {
					handleMultipart(multipart);
				}
			}
		}
	}

	private boolean isDispositionInline(String disposition) {
		return disposition == null || Part.INLINE.equalsIgnoreCase(disposition);
	}

	private void handleHTMLContent(Part part) throws IOException, MessagingException, ParseException {
		HTMLParser htmlp = new HTMLParser();

		try (InputStream is = part.getInputStream();
				Reader read = new StringReader(htmlp.parse(is, null, null, null, null));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				OutputStreamWriter output = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
				BufferedWriter bw = new BufferedWriter(output);
				BufferedReader br = new BufferedReader(read);) {

			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				bw.write(inputLine);
				bw.newLine();
			}

			String htmlContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);
			sb.append(htmlContent).append(" ");
		}
	}

	public String parse(Message message) {
		StringBuilder stringBuffer = new StringBuilder();
		parse(message, stringBuffer);
		return stringBuffer.toString();
	}

	public void parse(Message message, StringBuilder output) {
		sb = new StringBuilder();

		try {
			// Extract sender, recipients and subject
			if (message instanceof MimeMessage mime)
				extractHeaders(mime);

			Object content = message.getContent();
			if (content instanceof Multipart multipart) {
				handleMultipart(multipart);
			} else {
				handlePart(message);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		output.append(sb.toString());
	}

	@Override
	public void internalParse(InputStream input, ParseParameters parameters, StringBuilder output) {

		Properties props = System.getProperties();
		props.put("mail.host", "smtp.dummydomain.com");
		props.put("mail.transport.protocol", "smtp");

		try {
			Session mailSession = Session.getInstance(props, null);
			MimeMessage message = new MimeMessage(mailSession, input);
			parse(message, output);

			EMail email = MailUtil.messageToMail(message, true);
			for (EMailAttachment attachment : email.getAttachments().values()) {
				String includes = Context.get().getProperties().getString("parse.email.includes", "");
				String excludes = Context.get().getProperties().getString("parse.email.excludes", "*");
				if (FileUtil.matches(attachment.getFileName(), includes, excludes))
					output.append('\n').append(attachment.parseContent());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			//IOUtil.close(input);
			try {
				IOUtils.close(input);
			} catch (IOException ioe) {}
		}
	}

	/**
	 * Each attachment is considered as an additional page
	 */
	@Override
	public int countPages(InputStream input, String filename) {
		return MailUtil.countEmlAttachments(input) + 1;
	}
}