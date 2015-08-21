/**
 *	Copyright (c) 2015 Vör Security Inc.
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
package net.ossindex.common.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/** Representation of the FileResource, backed by the OSS Index REST API
 * 
 * @author Ken Duck
 *
 */
@SuppressWarnings("restriction")
public class FileResource extends AbstractRemoteResource
{
	/**
	 * File name. May be populated by OSS Index, but can be
	 * overridden by local file.
	 */
	private String name = null;

	/**
	 * Required for deserialization
	 */
	FileResource()
	{
	}
	
	public FileResource(long id)
	{
		super(id);
	}

	/**
	 * 
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	/** Override the name provided by OSS Index.
	 * 
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/** Find an applicable resource, otherwise return null. Use a combination
	 * of HttpClient and GSON to handle the request and response.
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	public static FileResource find(File file) throws IOException
	{
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String requestString = getBaseUrl() + "/rest/sha1/" + getSha1(file);
		System.err.print("Request: " + requestString + "...");
		try
		{
			HttpGet request = new HttpGet(requestString);
			CloseableHttpResponse response = httpClient.execute(request);
			String json = EntityUtils.toString(response.getEntity(), "UTF-8");
			Gson gson = new Gson();
			try
			{
				FileResource result = gson.fromJson(json, FileResource.class);
				if(result != null && result.getId() > 0)
				{
					// Override the file name
					result.setName(file.getName());
					return result;
				}
			}
			catch(JsonSyntaxException e)
			{
				System.err.println("Exception parsing response from request '" + requestString + "'");
			}
			return null;
		}
		finally
		{
			System.err.println(" done");
		}
	}

	/** Get the SHA1 checksum for the file.
	 * 
	 * @return
	 * @throws IOException
	 */
	private static String getSha1(File file) throws IOException
	{
		// Get the SHA1 sum for a file, then check if the MD5 is listed in the
		// OSS Index (indicating it is third party code).
		FileInputStream is = null;
		try
		{
			is = new FileInputStream(file);
			return DigestUtils.shaHex(is);
		}
		finally
		{
			if(is != null)
			{
				is.close();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.common.resource.AbstractRemoteResource#getResourceType()
	 */
	@Override
	protected String getResourceType()
	{
		return "file";
	}
}
