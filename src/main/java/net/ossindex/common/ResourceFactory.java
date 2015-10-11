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
package net.ossindex.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.util.List;

import net.ossindex.common.resource.AbstractRemoteResource;
import net.ossindex.common.resource.ArtifactResource;
import net.ossindex.common.resource.FileResource;
import net.ossindex.common.resource.ScmResource;
import net.ossindex.common.utils.PackageDependency;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * @author Ken Duck
 *
 */
@SuppressWarnings("restriction")
public class ResourceFactory
{
	private static ResourceFactory instance;

	/**
	 * Cache implementation
	 */
	private IOssIndexCache cache;

	/**
	 * Temporary boolean for debugging purposes.
	 */
	private static boolean DEBUG = false;

	/**
	 * Bad form! Hard-coded for prototype.
	 */
	private static String scheme = "https";
	private static String host = "ossindex.net";
	private static int port = -1; // Use default port

	private ResourceFactory()
	{

	}

	/** Get the resource factory instance.
	 * 
	 * @return
	 */
	public synchronized static ResourceFactory getResourceFactory()
	{
		if(instance == null) instance = new ResourceFactory();
		return instance;
	}

	/** Set the cache implementation
	 * 
	 * @param cache
	 */
	public void setCache(IOssIndexCache cache)
	{
		this.cache = cache;
	}

	/** Get the current cache implementation
	 * 
	 * @return
	 */
	public IOssIndexCache getCache()
	{
		return cache;
	}

	/** Get the base URL for REST requests.
	 * 
	 * @return
	 */
	protected static String getBaseUrl()
	{
		if(port >= 0)
		{
			return scheme + "://" + host + ":" + port;
		}
		else
		{
			return scheme + "://" + host;
		}
	}

	/**
	 * 
	 * @param b
	 */
	public static void setDebug(boolean b)
	{
		if(b)
		{
			scheme = "http";
			host = "localhost";
			port = 8080;
		}
		else
		{
			scheme = "https";
			host = "ossindex.net";
			port = -1; // Use default port
		}
	}

	/**
	 * 
	 * @param dep
	 * @return
	 * @throws IOException
	 */
	public ArtifactResource findArtifactResource(PackageDependency dep) throws IOException
	{
		ArtifactResource[] resources = findArtifactResources(new PackageDependency[] {dep});
		if(resources != null && resources.length > 0) return resources[0];
		return null;
	}

	/** Get multiple matching resources for the specified files. If a file
	 * does not have a match then a null will be placed in the results array.
	 * 
	 * This is done so the user knows which result belongs with which
	 * input file.
	 * 
	 * @param files
	 * @return
	 * @throws IOException
	 */
	public ArtifactResource[] findArtifactResources(PackageDependency[] pkgDeps) throws IOException
	{
		if(pkgDeps == null || pkgDeps.length == 0) return new ArtifactResource[0];

		CloseableHttpClient httpClient = HttpClients.createDefault();

		String reqPath = "/v1.0/search/artifact/";

		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(int i = 0; i < pkgDeps.length; i++)
		{
			PackageDependency dep = pkgDeps[i];
			if(i > 0) sb.append(",");
			sb.append("{");
			sb.append("\"pm\": \"" + dep.getPackageManager() + "\",");
			sb.append("\"name\": \"" + dep.getName() + "\",");
			sb.append("\"version\": \"" + dep.getVersion() + "\"");
			sb.append("}");
		}
		sb.append("]");

		if(DEBUG)
		{
			System.err.println("Request: " + reqPath + "...");
			System.err.println("  DATA: " + sb.toString());
		}

		try
		{
			HttpPost request = new HttpPost(getBaseUrl() + reqPath);
			request.setEntity(new StringEntity(sb.toString()));

			CloseableHttpResponse response = httpClient.execute(request);
			int code = response.getStatusLine().getStatusCode();
			if(code < 200 || code > 299)
			{
				throw new ConnectException(response.getStatusLine().getReasonPhrase() + " (" + code + ")");
			}
			String json = EntityUtils.toString(response.getEntity(), "UTF-8");
			Gson gson = new Gson();
			try
			{
				ArtifactResource[] resources = gson.fromJson(json, ArtifactResource[].class);
				return resources;
			}
			catch(JsonSyntaxException e)
			{
				System.err.println("Exception parsing response from request '" + reqPath + "'");
				System.err.println(json);

				// Throw a connect exception so that the caller knows not to try any more.
				throw new ConnectException(e.getMessage());
			}
		}
		finally
		{
			httpClient.close();
			//			System.err.println(" done");
		}
	}

	/** Find an applicable resource, otherwise return null. Use a combination
	 * of HttpClient and GSON to handle the request and response.
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	public FileResource findFileResource(File file) throws IOException
	{
		FileResource[] resources = findFileResources(new File[] {file});
		if(resources != null && resources.length > 0) return resources[0];
		return null;
	}

	/** Get multiple matching resources for the specified files. If a file
	 * does not have a match then a null will be placed in the results array.
	 * 
	 * This is done so the user knows which result belongs with which
	 * input file.
	 * 
	 * @param files
	 * @return
	 * @throws IOException
	 */
	public FileResource[] findFileResources(File[] files) throws IOException
	{
		if(files == null || files.length == 0) return new FileResource[0];

		CloseableHttpClient httpClient = HttpClients.createDefault();
		StringBuilder sb = new StringBuilder(getBaseUrl());
		sb.append("/v1.0/sha1/");
		for(int i = 0; i < files.length; i++)
		{
			File file = files[i];
			if(i > 0) sb.append(",");
			String sha1 = getSha1(file);
			sb.append(sha1);
		}
		String requestString = sb.toString();
		if(DEBUG) System.err.print("Request: " + requestString + "...");

		try
		{
			HttpGet request = new HttpGet(requestString);
			CloseableHttpResponse response = httpClient.execute(request);
			String json = EntityUtils.toString(response.getEntity(), "UTF-8");
			Gson gson = new Gson();
			try
			{
				FileResource[] resources = gson.fromJson(json, FileResource[].class);
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
			httpClient.close();
			//			System.err.println(" done");
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

	/** Get an SCM resource list matching the supplied scm IDs.
	 * 
	 * @param scmIds
	 * @return
	 * @throws IOException
	 */
	public ScmResource[] findScmResources(long[] scmIds) throws IOException
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
			httpClient.close();
			//			System.err.println(" done");
		}
	}

	/** Build resources out of the results of the specified query.
	 * 
	 * @param cls
	 * @return
	 * @throws IOException
	 */
	public <T extends AbstractRemoteResource> List<T> getResources(TypeToken<?> type, String query) throws IOException
	{
		List<T> results = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();

		String requestString = getBaseUrl() + query;

		try
		{
			HttpGet request = new HttpGet(requestString);
			CloseableHttpResponse response = httpClient.execute(request);
			String json = EntityUtils.toString(response.getEntity(), "UTF-8");
			Gson gson = new Gson();
			try
			{
				results = gson.fromJson(json, type.getType());
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
			httpClient.close();
			//			System.err.println(" done");
		}
		return results;
	}

}
