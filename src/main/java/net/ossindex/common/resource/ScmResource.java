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

import net.ossindex.common.ResourceFactory;

import com.google.gson.reflect.TypeToken;


/** Representation of the FileResource, backed by the OSS Index REST API
 * 
 * @author Ken Duck
 *
 */
@SuppressWarnings("restriction")
public class ScmResource extends AbstractRemoteResource
{
	private String uri;
	private String name;
	private String description;
	private long size;
	private String scm_type;
	private String requires;
	private boolean hasVulnerability;
	private String vulnerabilities;
	private String references;
	private String releases;
	private String files;
	private String authors;
	private String languages;
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

	/**
	 * Cache results from OSS Index for speed purposes
	 */
	private VulnerabilityResource[] vulnerabilityCache;


	/*
	 * (non-Javadoc)
	 * @see net.ossindex.common.resource.AbstractRemoteResource#getResourceType()
	 */
	@Override
	protected String getResourceType()
	{
		return "scm";
	}


	/** Get a list of all vulnerabilities affecting this resource.
	 * 
	 * @return An array of vulnerabilities affecting the SCM
	 */
	public VulnerabilityResource[] getVulnerabilities() throws IOException
	{
		if(vulnerabilityCache != null) return vulnerabilityCache;
		if(!hasVulnerability)
		{
			vulnerabilityCache = new VulnerabilityResource[0];
		}
		else
		{
			TypeToken<ArrayList<VulnerabilityResource>> type = new TypeToken<ArrayList<VulnerabilityResource>>() {};
			List<VulnerabilityResource> results = ResourceFactory.getResourceFactory().getResources(type, "/v1.0/scm/" + getId() + "/vulnerabilities");
			vulnerabilityCache = results.toArray(new VulnerabilityResource[results.size()]);
		}
		return vulnerabilityCache;
	}

	/** Get the SCM name
	 * 
	 * @return The SCM name
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

	/** Get the artifact description
	 * 
	 * @return A description
	 */
	public String getDescription()
	{
		return description;
	}
}
