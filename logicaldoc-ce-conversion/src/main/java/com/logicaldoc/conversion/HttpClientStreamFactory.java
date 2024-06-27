package com.logicaldoc.conversion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaldoc.core.store.Storer;
import com.logicaldoc.util.Context;
import com.logicaldoc.util.http.HttpUtil;
import com.logicaldoc.util.http.UrlUtil;
import com.logicaldoc.util.io.AutoDeleteInputStream;
import com.logicaldoc.util.io.FileUtil;
import com.logicaldoc.util.io.IOUtil;
import com.openhtmltopdf.extend.FSStream;
import com.openhtmltopdf.extend.FSStreamFactory;
import com.openhtmltopdf.swing.NaiveUserAgent.DefaultHttpStream;

/**
 * Am HttpStreamFactory that uses HTTPClient
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 8.3.4
 */
public class HttpClientStreamFactory implements FSStreamFactory {
	protected static Logger log = LoggerFactory.getLogger(HttpClientStreamFactory.class);

	@Override
	public FSStream getUrl(String url) {
		// First check if the URL references a document
		if (url.contains("docId=")) {
			try {
				long docId = Long.parseLong(UrlUtil.getParams(url).get("docId"));
				Storer storer = (Storer) Context.get().getBean(Storer.class);
				InputStream is = storer.getStream(docId, storer.getResourceName(docId, null, null));
				return new DefaultHttpStream(is);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return null;
			}
		} else {
			try (CloseableHttpClient client = HttpUtil.getNotValidatingClient(300)) {
				HttpGet request = new HttpGet(url);
				HttpResponse response = client.execute(request);

				File tempFile = FileUtil.createTempFile("httpstream", "");
				IOUtil.write(response.getEntity().getContent(), tempFile);
				AutoDeleteInputStream is = new AutoDeleteInputStream(tempFile);
				return new DefaultHttpStream(is);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				return null;
			}
		}
	}
}