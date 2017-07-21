/**
 *	Copyright (c) 2017 Vör Security Inc.
 *	All rights reserved.
 *	
 *	Redistribution and use in source and binary forms, with or without
 *	modification, are permitted provided that the following conditions are met:
 *	    * Redistributions of source code must retain the above copyright
 *	      notice, this list of conditions and the following disclaimer.
 *	    * Redistributions in binary form must reproduce the above copyright
 *	      notice, this list of conditions and the following disclaimer in the
 *	      documentation and/or other materials provided with the distribution.
 *	    * Neither the name of the <organization> nor the
 *	      names of its contributors may be used to endorse or promote products
 *	      derived from this software without specific prior written permission.
 *	
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *	ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *	DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 *	DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *	(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *	ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

	/** Perform the request with the given URL and JSON data.
	 * 
	 * @param requestString Server request relative URL
	 * @param data JSON data for the request
	 * @return JSON results of the request
	 * @throws IOException On query problems
	 */
	protected String performPostRequest(String requestString, String data) throws IOException {
		HttpPost request = new HttpPost(getBaseUrl() + requestString);

		String json = null;
		CloseableHttpClient httpClient = HttpClients.custom()
                    .useSystemProperties()
                    .build();
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

	/** Get the base URL for requests
	 * 
	 * @return The base URL
	 */
	private String getBaseUrl() {
		return BASE_URL;
	}
}
