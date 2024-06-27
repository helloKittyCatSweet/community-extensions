package com.logicaldoc.ocr;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.logicaldoc.core.PersistenceException;
import com.logicaldoc.core.document.DocumentHistoryDAO;
import com.logicaldoc.core.security.Session;
import com.logicaldoc.core.util.IconSelector;
import com.logicaldoc.util.Context;
import com.logicaldoc.util.io.FileUtil;
import com.logicaldoc.web.data.AbstractDataServlet;

/**
 * This servlet is responsible for OCR history data.
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 8.8.5
 */
public class OCRHistoriesDataServlet extends AbstractDataServlet {

	private static final long serialVersionUID = 1L;
	
	private static final String EVENT = "event";
	

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response, Session session, Integer max,
			Locale locale) throws PersistenceException, IOException {

		List<Object> records = findRecords(request, session, max);

		DateFormat df = getDateFormat();
		PrintWriter writer = response.getWriter();
		writer.write("<list>");

		/*
		 * Iterate over records composing the response XML document
		 */
		for (Object gridRecord : records) {
			Object[] cols = (Object[]) gridRecord;
			writer.print("<history>");
			writer.write("<id>" + cols[9] + "</id>");
			writer.print("<event>" + cols[0] + "</event>");
			writer.print("<date>" + df.format((Date) cols[1]) + "</date>");
			if (cols[2] != null)
				writer.print("<comment><![CDATA[" + cols[2] + "]]></comment>");
			if (cols[3] != null) {
				writer.print("<filename><![CDATA[" + cols[3] + "]]></filename>");
				writer.print("<icon>"
						+ FileUtil.getBaseName(IconSelector.selectIcon(FileUtil.getExtension((String) cols[3])))
						+ "</icon>");
			}
			writer.print("<new>" + (1 == (Integer) cols[4]) + "</new>");
			if (cols[5] != null)
				writer.print("<folderId>" + cols[5] + "</folderId>");
			if (cols[6] != null)
				writer.print("<docId>" + cols[6] + "</docId>");
			if (cols[7] != null)
				writer.print("<path><![CDATA[" + cols[7] + "]]></path>");
			if (cols[8] != null)
				writer.write("<color><![CDATA[" + cols[8] + "]]></color>");
			if (cols[10] != null)
				writer.write("<size>" + cols[10] + "</size>");
			writer.print("</history>");
		}

		writer.write("</list>");
	}

	private List<Object> findRecords(HttpServletRequest request, Session session, Integer max)
			throws PersistenceException {
		Map<String, Object> params = new HashMap<>();

		DocumentHistoryDAO dao = (DocumentHistoryDAO) Context.get().getBean(DocumentHistoryDAO.class);
		StringBuilder query = new StringBuilder(
				"select A.event, A.date, A.comment, A.filename, A.isNew, A.folderId, A.docId, A.path, A.color, A.id, A.fileSize from OCRHistory A where A.deleted = 0 ");

		query.append(" and A.tenantId = :tenantId");
		params.put("tenantId", session.getTenantId());

		if (request.getParameter(EVENT) != null) {
			query.append(" and A.event = :event");
			params.put(EVENT, request.getParameter(EVENT));
		}
		query.append(" order by A.date desc ");

		return dao.findByQuery(query.toString(), params, max != null ? max : 100);
	}
}