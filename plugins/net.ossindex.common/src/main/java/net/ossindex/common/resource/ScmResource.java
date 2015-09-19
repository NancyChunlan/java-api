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
import java.io.IOException;
import java.net.ConnectException;

import net.ossindex.common.utils.PackageDependency;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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
public class ScmResource extends AbstractRemoteResource
{
	public String uri;
	public String name;
	public String description;
	public long size;
	public String scm_type;
	public String requires;
	public boolean hasVulnerability;
	public String vulnerabilities;
	public String references;
	public String releases;
	public String files;
	public String authors;
	public String languages;
	// FIXME: Not loading CPE result list, which may have one of two
	// different forms:
	//
	//     "cpes": [
	//      {
	//        "status": "none"
	//      }
	//    ],
	//    
	//    "cpes": [
	//             {
	//               "cpecode": "cpe:/a:jquery:jquery",
	//               "cpe": "http://localhost:8080/v1.0/cpe/a/jquery/jquery"
	//             }
	//           ],



	@Override
	protected String getResourceType()
	{
		return "scm";
	}
	
	/** Get an SCM resource list matching the supplied scm IDs.
	 * 
	 * @param scmIds
	 * @return
	 * @throws IOException
	 */
	public static ScmResource[] find(long[] scmIds) throws IOException
	{
		if(scmIds == null || scmIds.length == 0) return new ScmResource[0];
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		StringBuilder sb = new StringBuilder(getBaseUrl());
		sb.append("/v1.0/scm/");
		for(int i = 0; i < scmIds.length; i++)
		{
			if(i > 0) sb.append(",");
			sb.append(scmIds[i]);
		}
		String requestString = sb.toString();
		
		try
		{
			HttpGet request = new HttpGet(requestString);
			CloseableHttpResponse response = httpClient.execute(request);
			String json = EntityUtils.toString(response.getEntity(), "UTF-8");
			Gson gson = new Gson();
			try
			{
				ScmResource[] resources = gson.fromJson(json, ScmResource[].class);
				return resources;
			}
			catch(JsonSyntaxException e)
			{
				System.err.println("Exception parsing response from request '" + requestString + "'");
				System.err.println(json);
				
				// Throw a connect exception so that the caller knows not to try any more.
				throw new ConnectException(e.getMessage());
			}
		}
		finally
		{
//			System.err.println(" done");
		}
	}


	/** Get a list of all vulnerabilities affecting this resource.
	 * 
	 * @return
	 */
	public VulnerabilityResource[] getVulnerabilities() throws IOException
	{
		if(!hasVulnerability) return new VulnerabilityResource[0];
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		String requestString = getBaseUrl() + "/v1.0/scm/" + getId() + "/vulnerabilities";
		
		try
		{
			HttpGet request = new HttpGet(requestString);
			CloseableHttpResponse response = httpClient.execute(request);
			String json = EntityUtils.toString(response.getEntity(), "UTF-8");
			Gson gson = new Gson();
			try
			{
				VulnerabilityResource[] resources = gson.fromJson(json, VulnerabilityResource[].class);
				return resources;
			}
			catch(JsonSyntaxException e)
			{
				System.err.println("Exception parsing response from request '" + requestString + "'");
				System.err.println(json);
				
				// Throw a connect exception so that the caller knows not to try any more.
				throw new ConnectException(e.getMessage());
			}
		}
		finally
		{
//			System.err.println(" done");
		}
	}

	/** Get the SCM name
	 * 
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		if(uri == null) return name;
		return uri.toString();
	}
}
