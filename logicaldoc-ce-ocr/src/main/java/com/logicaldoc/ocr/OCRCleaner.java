package com.logicaldoc.ocr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.logicaldoc.community.cleaner.Cleaner;
import com.logicaldoc.util.config.ContextProperties;

public class OCRCleaner extends Cleaner {

	public OCRCleaner() {
		super();
		setDbScript("/sql/clean-ocr.sql");
	}

	public OCRCleaner(Connection con) {
		super(con);
		setDbScript("/sql/clean-ocr.sql");
	}

	@Override
	protected void afterDbUpdate() {
		// Nothing to do
	}

	@Override
	protected void beforeDbUpdate() {
		// Mark as deleted the obsolete entries
		try {
			ContextProperties bean = new ContextProperties();
			int ttl = bean.getInt("history.ocr.ttl", -1);
			if (ttl <= 0)
				return;

			Date date = new Date();
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			cal.add(Calendar.DAY_OF_MONTH, -ttl);
			date = cal.getTime();
			try (PreparedStatement stmt = connection
.prepareStatement("update ld_ocr_history set ld_deleted=1 where ld_deleted = 0 and ld_date < ?")) {
				stmt.setTimestamp(1, new Timestamp(date.getTime()));
				int count = stmt.executeUpdate();
				log.debug("The number of obsolete entries is: {}", count);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}