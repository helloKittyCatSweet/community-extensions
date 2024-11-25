package com.logicaldoc.gui.frontend.client.services;

import java.util.List;
import com.logicaldoc.gui.common.client.ServerException;
import com.logicaldoc.gui.common.client.beans.GUIParameter;

public interface OCRService {
/**
* upload the configuration of OCR
* @return GUIParameter list
* @throws ServerException
* */
  GUIParameter[] loadSettings() throws ServerException;
}
