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
import net.ossindex.common.resource.PackageResource;
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

		String requestString = reqPath;
		String data = sb.toString();
		String json = doPost(requestString, data);
		Gson gson = new Gson();
		try
		{
			ArtifactResource[] resources = gson.fromJson(json, ArtifactResource[].class);

			// Preemptively cache the individual queries for these resources.
			cacheResources(resources);

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

		StringBuilder sb = new StringBuilder();
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

		String json = doGetArray(requestString);
		Gson gson = new Gson();
		try
		{
			FileResource[] resources = gson.fromJson(json, FileResource[].class);

			// Preemptively cache the individual queries for these resources.
			cacheResources(resources);

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

		StringBuilder sb = new StringBuilder();
		sb.append("/v1.0/scm/");
		for(int i = 0; i < scmIds.length; i++)
		{
			if(i > 0) sb.append(",");
			sb.append(scmIds[i]);
		}
		String requestString = sb.toString();

		String json = doGetArray(requestString);
		if(json != null)
		{
			Gson gson = new Gson();
			try
			{
				ScmResource[] resources = gson.fromJson(json, ScmResource[].class);

				// Preemptively cache the individual queries for these resources.
				cacheResources(resources);
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
		return null;
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

		String requestString = query;

		String json = doGetArray(requestString);
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
		return results;
	}

	/** Get a single instance of a resource. This is relatively slow. You should instead
	 * batch these queries if possible (see the 'find*' methods.)
	 * 
	 * @param cls
	 * @param id
	 * @return
	 * @throws ConnectException 
	 */
	public <T extends AbstractRemoteResource> T createResource(Class<T> cls, long id) throws IOException
	{
		String query = getResourceQuery(cls, id);
		if(query != null)
		{
			String json = doGet(query);
			json = json.trim();

			// We know there is only one value. Strip the array information and re-cache.
			if(json.startsWith("["))
			{
				json = json.substring(1, json.length() - 1);
				cache.cache(query, json);
			}
			Gson gson = new Gson();
			try
			{
				return gson.fromJson(json, cls);
			}
			catch(JsonSyntaxException e)
			{
				System.err.println("Exception parsing response from request '" + query + "'");
				System.err.println(json);

				// Throw a connect exception so that the caller knows not to try any more.
				throw new ConnectException(e.getMessage());
			}
		}

		return null;
	}

	/** Get a query for a resource of the specified class type
	 * 
	 * @param cls
	 * @param id
	 * @return
	 */
	private String getResourceQuery(Class<? extends AbstractRemoteResource> cls, long id)
	{
		if(ArtifactResource.class.isAssignableFrom(cls))
		{
			return "/v1.0/artifact/" + id;
		}
		if(PackageResource.class.isAssignableFrom(cls))
		{
			return "/v1.0/package/" + id;
		}
		if(ScmResource.class.isAssignableFrom(cls))
		{
			return "/v1.0/scm/" + id;
		}
		return null;
	}

	/** Due to caching we may not get an array result when expected. Rebuild
	 * the list.
	 * 
	 * @param requestString
	 * @return
	 * @throws IOException
	 */
	private String doGetArray(String requestString) throws IOException
	{
		String json = doGet(requestString);
		if(json != null)
		{
			json = json.trim();
			if(!json.startsWith("[")) json = "[" + json + "]";
		}
		return json;
	}

	/** Perform the query. Use the cache if possible.
	 * 
	 * @param requestString
	 * @return
	 * @throws IOException 
	 */
	private String doGet(String requestString) throws IOException
	{
		String json = null;

		// Is there a cached value?
		if(cache != null)
		{
			json = cache.get(requestString);
		}

		// Not cached
		if(json == null)
		{
			CloseableHttpClient httpClient = HttpClients.createDefault();
			try
			{
				HttpGet request = new HttpGet(getBaseUrl() + requestString);
				CloseableHttpResponse response = httpClient.execute(request);
				int code = response.getStatusLine().getStatusCode();
				if(code >= 200 && code < 300)
				{
					json = EntityUtils.toString(response.getEntity(), "UTF-8");
				}
			}
			finally
			{
				httpClient.close();
				//			System.err.println(" done");
			}

			if(json != null)
			{
				if(cache != null)
				{
					cache.cache(requestString, json);
				}
			}
		}
		return json;
	}

	/**
	 * 
	 * @param requestString
	 * @param data
	 * @return
	 * @throws IOException
	 */
	private String doPost(String requestString, String data) throws IOException
	{
		String json = null;

		String cacheId = requestString + "::" + data;

		// Is there a cached value?
		if(cache != null)
		{
			json = cache.get(cacheId);
		}

		// Not cached
		if(json == null)
		{
			HttpPost request = new HttpPost(getBaseUrl() + requestString);
			request.setEntity(new StringEntity(data));

			CloseableHttpClient httpClient = HttpClients.createDefault();
			try
			{
				CloseableHttpResponse response = httpClient.execute(request);
				int code = response.getStatusLine().getStatusCode();
				if(code < 200 || code > 299)
				{
					throw new ConnectException(response.getStatusLine().getReasonPhrase() + " (" + code + ")");
				}
				json = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
			finally
			{
				httpClient.close();
				//			System.err.println(" done");
			}

			if(json != null)
			{
				if(cache != null)
				{
					cache.cache(cacheId, json);
				}
			}
		}
		return json;
	}

	/** Preemptively cache the individual queries for these resources.
	 * 
	 * @param resources
	 */
	public <T extends AbstractRemoteResource> void cacheResources(T[] resources)
	{
		Gson gson = new Gson();
		if(resources != null)
		{
			if(cache != null)
			{
				for (T t : resources)
				{
					long id = t.getId();
					String query = getResourceQuery(t.getClass(), id);
					if(query != null)
					{
						String json = gson.toJson(t);
						cache.cache(query, json);
					}
				}
			}
		}
	}

	/**
	 * 
	 */
	public void closeCache()
	{
		cache.close();
	}

}
