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

import java.io.IOException;
import java.net.ConnectException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ossindex.common.utils.PackageDependency;
import net.ossindex.common.version.NpmVersion;
import net.ossindex.common.version.SemanticVersion;

import org.apache.http.client.methods.CloseableHttpResponse;
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
public class ArtifactResource extends AbstractRemoteResource implements Comparable<ArtifactResource>
{
	/**
	 * Temporary boolean for debugging purposes.
	 */
	private static boolean DEBUG = true;
	
	/**
	 * Package name
	 */
	private String name;
	
	/**
	 * Version
	 */
	private String version;
	
	/**
	 * Package description
	 */
	private String description;
	
	/**
	 * Package manager name
	 */
	private String package_manager;
	
	/**
	 * URI for the SCM
	 */
	private String scm;
	
	/**
	 * SCM id
	 */
	private long scm_id;
	
	/**
	 * URL for package download
	 */
	private String url;
	
	/**
	 * REST API URL to get artifact details
	 */
	private String details;
	
	/**
	 * REST API URL to get artifact dependencies
	 */
	private String dependencies;
	
	/**
	 * Search strings used to retrieve this artifact
	 */
	private String[] search;
	
	/**
	 * 
	 */
	private static final Pattern packagePattern = Pattern.compile("^(.*)-[0-9]+\\.[0-9]+\\.[0-9]");
	
	/**
	 * Required for deserialization
	 */
	ArtifactResource()
	{
	}
	
	public ArtifactResource(long id)
	{
		super(id);
	}
	
	public static ArtifactResource find(PackageDependency dep) throws IOException
	{
		ArtifactResource[] resources = find(new PackageDependency[] {dep});
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
	public static ArtifactResource[] find(PackageDependency[] pkgDeps) throws IOException
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

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.common.resource.AbstractRemoteResource#getResourceType()
	 */
	@Override
	protected String getResourceType()
	{
		return "artifact";
	}

	/**
	 * 
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getPackageName()
	{
		Matcher m = packagePattern.matcher(name);
		if(m.find())
		{
			return m.group(1);
		}
		return name;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getVersion()
	{
		return version;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getDescription()
	{
		return description;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getPackageManager()
	{
		return package_manager;
	}
	
	/**
	 * 
	 * @return
	 */
	public String[] getSearch()
	{
		return search;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getScmId()
	{
		return scm_id;
	}
	
	/** Get the semantic version for the resource.
	 * 
	 * @return
	 */
	public SemanticVersion getSemanticVersion()
	{
		if(version == null) return null;
		if(package_manager != null)
		{
			switch(package_manager)
			{
			case "npm": return new NpmVersion(version);
			}
		}
		return new SemanticVersion(version);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ArtifactResource res)
	{
		if(res == null) return 1;
		SemanticVersion myVersion = getSemanticVersion();
		SemanticVersion yourVersion = res.getSemanticVersion();
		if(yourVersion == null) return 1;
		if(myVersion == null) return -1;
		return myVersion.compareTo(yourVersion);
	}
}
