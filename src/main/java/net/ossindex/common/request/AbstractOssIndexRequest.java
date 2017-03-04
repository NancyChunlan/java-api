package net.ossindex.common.request;

import java.io.IOException;
import java.net.ConnectException;

import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/** Code that actually performs the requests to the server
 * 
 * @author Ken Duck
 *
 */
public class AbstractOssIndexRequest {

	private static final String BASE_URL = "https://ossindex.net/v2.0/";

	/**
	 * Perform the request with the given URL and JSON data.
	 */
	protected String performPostRequest(String requestString, String data) throws IOException {
		HttpPost request = new HttpPost(getBaseUrl() + requestString);

		String json = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			request.setEntity(new StringEntity(data));
			CloseableHttpResponse response = httpClient.execute(request);
			int code = response.getStatusLine().getStatusCode();
			if(code < 200 || code > 299) {
				throw new ConnectException(response.getStatusLine().getReasonPhrase() + " (" + code + ")");
			}
			json = EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch(ParseException e) {
			throw new IOException(e);
		} finally {
			httpClient.close();
		}
		return json;
	}

	private String getBaseUrl() {
		return BASE_URL;
	}
}
