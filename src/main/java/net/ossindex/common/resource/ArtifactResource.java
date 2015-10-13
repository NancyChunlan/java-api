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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.reflect.TypeToken;

import net.ossindex.common.ResourceFactory;
import net.ossindex.version.IVersion;
import net.ossindex.version.VersionFactory;

/** Representation of the FileResource, backed by the OSS Index REST API
 * 
 * @author Ken Duck
 *
 */
public class ArtifactResource extends AbstractRemoteResource implements Comparable<ArtifactResource>
{
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
	 * 
	 */
	private long package_id;

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
	private static final Pattern packagePattern3 = Pattern.compile("^(.*)-[0-9]+\\.[0-9]+\\.[0-9]+");
	private static final Pattern packagePattern2 = Pattern.compile("^(.*)-[0-9]+\\.[0-9]+");

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
		// Best matching is done by the expected version
		if(version != null)
		{
			int index = name.lastIndexOf(version);
			if(index > 0)
			{
				String result = name.substring(0, index);
				while(result.endsWith("-")) result = result.substring(0, result.length() - 1);
				return result;
			}
		}
		
		// Fall back that should likely never happen
		Matcher m = packagePattern3.matcher(name);
		if(m.find())
		{
			return m.group(1);
		}
		
		// Fall back that should likely never happen
		m = packagePattern2.matcher(name);
		if(m.find())
		{
			return m.group(1);
		}
		
		// Everything failed
		return name;
	}

	/**
	 * 
	 * @return
	 */
	public String getVersionString()
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
	public IVersion getVersion()
	{
		return VersionFactory.getVersionFactory().getVersion(package_manager, version);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ArtifactResource res)
	{
		if(res == null) return 1;
		IVersion myVersion = getVersion();
		IVersion yourVersion = res.getVersion();
		if(yourVersion == null) return 1;
		if(myVersion == null) return -1;
		return myVersion.compareTo(yourVersion);
	}

	/** Get the package the artifact belongs to
	 * 
	 * @return
	 * @throws IOException
	 */
	public PackageResource getPackage() throws IOException
	{
		if(package_id > 0)
		{
			return ResourceFactory.getResourceFactory().createResource(PackageResource.class, package_id);
		}
		return null;
	}

	/** Get the dependency graph for the artifact
	 * 
	 * @return
	 */
	public ArtifactResource[] getDependencyGraph()
	{
		try
		{
			TypeToken<ArrayList<ArtifactResource>> type = new TypeToken<ArrayList<ArtifactResource>>() {};
			List<ArtifactResource> results = ResourceFactory.getResourceFactory().getResources(type, "/v1.0/artifact/" + getId() + "/dependency_graph");
			if(results != null)
			{
				ArtifactResource[] resources = results.toArray(new ArtifactResource[results.size()]);
				ResourceFactory.getResourceFactory().cacheResources(resources);
				return resources;
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
