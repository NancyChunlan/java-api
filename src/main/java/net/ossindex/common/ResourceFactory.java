/**
 *	Copyright (c) 2015 V�r Security Inc.
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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * 
 * @author Ken Duck
 *
 */
@SuppressWarnings("restriction")
public class ResourceFactory
{
	private static final long ONE_MINUTE = 60000;
	private static final long TEN_MINUTES = 10 * ONE_MINUTE;
	private static final long ONE_HOUR = 60 * ONE_MINUTE;
	private static final long ONE_DAY = ONE_HOUR * 24;
	
	private static ResourceFactory instance;
	
	static
	{
		// Default log4j configuration. Hides configuration warnings.
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.WARN);
	}

	/**
	 * Cache implementation
	 */
	private IOssIndexCache cache;

	/**
	 * Temporary boolean for debugging purposes.
	 */
	private static boolean DEBUG = false;
	
	/**
	 * Time of the last connection timeout, which is used to determine if we should
	 * check again.
	 */
	private static long timeout = 0;

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
	 * @return ResourceFactory instance
	 */
	public synchronized static ResourceFactory getResourceFactory()
	{
		if(instance == null) instance = new ResourceFactory();
		return instance;
	}

	/** Set the cache implementation
	 * 
	 * @param cache Cache implementation
	 */
	public void setCache(IOssIndexCache cache)
	{
		this.cache = cache;
	}

	/** Get the current cache implementation
	 * 
	 * @return Cache implementation
	 */
	public IOssIndexCache getCache()
	{
		return cache;
	}

	/** Get the base URL for REST requests.
	 * 
	 * @return Request URL
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

	/** Use the debug server
	 * 
	 * @param b Set to true to enable debug server access
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

	/** Find the ArtifactResource matching the specified package dependency
	 * 
	 * @param dep Package dependency
	 * @return Matching artifact resource (only returns one match)
	 * @throws IOException On error
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
	 * @param pkgDeps Array of package dependencies to find matching artifacts for
	 * @return Array of matching artifacts
	 * @throws IOException On error
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
			if(dep.getGroupId() != null) sb.append("\"group\": \"" + dep.getGroupId() + "\",");
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
	 * @param file File to retrieve a file resource for
	 * @return The matching file resource
	 * @throws IOException On error
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
	 * @param files Files to find matching file resources for
	 * @return Matching file resources
	 * @throws IOException On error
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
	 * @param file File to retrieve a checksum for
	 * @return The SHA1 checksum for the file
	 * @throws IOException On error
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
	 * @param scmIds SCM resource IDs
	 * @return Array of SCM resources
	 * @throws IOException On error
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
	 * @param type Array type for results
	 * @param query OSS Index REST query
	 * @param <T> Type of resource being returned
	 * @return Results matching the query with the specified type
	 * @throws IOException On error
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
	 * @param cls Class (type) of the expected resource
	 * @param id Resource ID to retrieve
	 * @param <T> Type of resource being returned
	 * @return Resource matching the specified ID
	 * @throws ConnectException On server connection issues
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
	 * @param cls Resource type that the query is being built for
	 * @param id ID of the resource to build the query for
	 * @return An OSSIndex REST query that will return the requested resource
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
	 * @param requestString QUERY being performed
	 * @return The JSON results of the query, converted to an array if required
	 * @throws IOException On error
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

	/** Perform a GET query. Use the cache if possible.
	 * 
	 * @param requestString QUERY being performed
	 * @return JSON results of the query
	 * @throws IOException On error
	 */
	private String doGet(String requestString) throws IOException
	{
		String json = null;

		// Is there a cached value?
		if(cache != null)
		{
			long delay = ONE_DAY;
			if(timeout > 0)
			{
				long now = System.currentTimeMillis();
				if(now - timeout > TEN_MINUTES)
				{
					timeout = 0;
				}
				else
				{
					delay = -1;
				}
			}
			
			json = cache.get(requestString, delay);
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
			catch(ConnectException e)
			{
				// Timed out -- there is no server. Use cached data for now.
				timeout = System.currentTimeMillis();
				// Try to get a backup from the cache, ignoring the time delay
				json = cache.get(requestString);
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

	/**Perform a POST query. Use the cache if possible.
	 * 
	 * @param requestString QUERY being performed
	 * @param data JSON data for the post query
	 * @return JSON results of the query
	 * @throws IOException On error
	 */
	private String doPost(String requestString, String data) throws IOException
	{
		String json = null;

		String cacheId = requestString + "::" + data;

		// Is there a cached value?
		if(cache != null)
		{
			long delay = ONE_DAY;
			if(timeout > 0)
			{
				long now = System.currentTimeMillis();
				if(now - timeout > TEN_MINUTES)
				{
					timeout = 0;
				}
				else
				{
					delay = -1;
				}
			}
			
			json = cache.get(cacheId, delay);
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
			catch(ConnectException e)
			{
				// Timed out -- there is no server. Use cached data for now.
				timeout = System.currentTimeMillis();
				// Try to get a backup from the cache, ignoring the time delay
				json = cache.get(requestString);
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
	 * @param resources Individual resources to cache
	 * @param <T> Type of resource being returned
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
	 * Close the cache
	 */
	public void closeCache()
	{
		cache.close();
	}

}
