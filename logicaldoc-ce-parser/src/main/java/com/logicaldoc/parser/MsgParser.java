package com.logicaldoc.parser;

import java.io.InputStream;
import java.io.StringReader;

import org.apache.poi.hsmf.MAPIMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaldoc.core.communication.EMail;
import com.logicaldoc.core.communication.EMailAttachment;
import com.logicaldoc.core.communication.MailUtil;
import com.logicaldoc.core.parser.AbstractParser;
import com.logicaldoc.core.parser.ParseParameters;
import com.logicaldoc.util.Context;
import com.logicaldoc.util.StringUtil;
import com.logicaldoc.util.io.FileUtil;

/**
 * Text extractor for Microsoft Outlook messages. Note: (.msg files)
 */
public class MsgParser extends AbstractParser {

	/**
	 * Logger instance.
	 */
	protected static Logger log = LoggerFactory.getLogger(MsgParser.class);

	@Override
	public void internalParse(InputStream input, ParseParameters parameters, StringBuilder output) {

		try (MAPIMessage message = new MAPIMessage(input);) {
			StringBuilder buffer = new StringBuilder();
			buffer.append(message.getDisplayFrom()).append('\n');
			buffer.append(message.getDisplayTo()).append('\n');
			buffer.append(message.getSubject()).append('\n');
			buffer.append(message.getTextBody());
			output.append(StringUtil.writeToString(new StringReader(buffer.toString())));

			EMail email = MailUtil.msgToMail(input, true);
			for (EMailAttachment attachment : email.getAttachments().values()) {
				String includes = Context.get().getProperties().getString("parse.email.includes", "");
				String excludes = Context.get().getProperties().getString("parse.email.excludes", "*");
				if (FileUtil.matches(attachment.getFileName(), includes, excludes))
					buffer.append(attachment.parseContent()).append('\n');
			}
		} catch (Exception e) {
			log.warn("Failed to extract Message content", e);
		}
	}

	@Override
	public int countPages(InputStream input, String filename) {
		return MailUtil.countMsgAttachments(input) + 1;
	}
}
