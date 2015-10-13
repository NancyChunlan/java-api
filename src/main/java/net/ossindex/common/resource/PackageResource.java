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
import java.util.Date;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import net.ossindex.common.ResourceFactory;

/** Representation of the FileResource, backed by the OSS Index REST API
 * 
 * @author Ken Duck
 *
 */
public class PackageResource extends AbstractRemoteResource implements Comparable<PackageResource>
{
	/**
	 * Package name
	 */
	private String name;

	/**
	 * 
	 */
	private long creation_date;

	/**
	 * 
	 */
	private long update_date;

	/**
	 * Package description
	 */
	private String description;

	/**
	 * Required for deserialization
	 */
	PackageResource()
	{
	}

	public PackageResource(long id)
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
		return "package";
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
		return name;
	}

	/**
	 * 
	 * @return
	 */
	public String getDescription()
	{
		return description;
	}

	/** Date the package was created, if known
	 * 
	 * @return
	 */
	public Date getCreationDate()
	{
		if(creation_date > 0)
		{
			return new Date(creation_date);
		}
		return null;
	}

	/** Date the package was last updated, if known
	 * 
	 * @return
	 */
	public Date getUpdateDate()
	{
		if(update_date > 0)
		{
			return new Date(update_date);
		}
		return null;
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PackageResource res)
	{
		if(res == null) return 1;
		if(name != null)
		{
			if(res.name != null) return name.compareTo(res.name);
			return 1;
		}
		if(res.name != null) return -1;
		if(getId() > res.getId()) return 1;
		if(getId() < res.getId()) return -1;
		return 0;
	}

	/** Get all of the artifacts owned by the package
	 * 
	 * @return
	 */
	public ArtifactResource[] getArtifacts()
	{
		try
		{
			TypeToken<ArrayList<ArtifactResource>> type = new TypeToken<ArrayList<ArtifactResource>>() {};
			List<ArtifactResource> results = ResourceFactory.getResourceFactory().getResources(type, "/v1.0/package/" + getId() + "/artifacts");
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
