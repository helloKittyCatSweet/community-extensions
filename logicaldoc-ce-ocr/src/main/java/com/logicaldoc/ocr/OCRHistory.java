package com.logicaldoc.ocr;

import com.logicaldoc.core.History;

/**
 * History entry to record OCR events
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 8.8.5
 */
public class OCRHistory extends History {

	private static final long serialVersionUID = 1L;

	public static final String EVENT_OCR_SUCCESS = "event.ocr.success";

	public static final String EVENT_OCR_FAILURE = "event.ocr.failure";

	private long importFolderId;

	public OCRHistory() {
		super();
	}

	public OCRHistory(OCRHistory source) {
		this.importFolderId = source.importFolderId;

		setDate(source.getDate());
		setDocId(source.getDocId());
		setFolderId(source.getFolderId());
		setUser(source.getUser());
		setEvent(source.getEvent());
		setComment(source.getComment());
		setReason(source.getReason());
		setVersion(source.getVersion());
		setFileVersion(source.getFileVersion());
		setPath(source.getPath());
		setPathOld(source.getPathOld());
		setNotified(source.getNotified());
		setSessionId(source.getSessionId());
		setIsNew(source.getIsNew());
		setFilename(source.getFilename());
		setFilenameOld(source.getFilenameOld());
		setUserId(source.getUserId());
		setUsername(source.getUsername());
		setUserLogin(source.getUserLogin());
		setNotifyEvent(isNotifyEvent());
		setIp(source.getIp());
		setDevice(source.getDevice());
		setGeolocation(source.getGeolocation());
		setFileSize(source.getFileSize());
		setColor(source.getColor());
	}
}