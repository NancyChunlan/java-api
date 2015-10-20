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
package net.ossindex.common.utils;

import java.io.IOException;

import net.ossindex.common.resource.ArtifactResource;
import net.ossindex.common.resource.ScmResource;
import net.ossindex.common.resource.VulnerabilityResource;

/** Define a dependency from an artifact to a package/version. This
 * class also provides mechanisms for adding additional data, such
 * as the artifact defined by the dependency package/version, and the
 * SCM that contains the source for the artifact.
 * 
 * @author Ken Duck
 *
 */
public class PackageDependency
{
	/**
	 * Package manager that defines this dependency
	 */
	private String pkgManager;
	
	/**
	 * Name of the dependency
	 */
	private String name;
	
	/**
	 * In package systems that have an extra field for package definition.
	 */
	private String groupId;
	
	/**
	 * Version number of the dependency
	 */
	private String version;
	
	/**
	 * Best match for the dependency name/version
	 */
	private ArtifactResource artifact;
	
	/**
	 * SCM that provides sources for the artifact
	 */
	private ScmResource scm;

	/**
	 * Line number in the parent file that this dependency resides on
	 */
	private FilePosition position;

	/**
	 * Is this dependency optional?
	 */
	private boolean optional = false;

	/** Is this a root package, which means it is directly referenced (as opposed to transitively).
	 * 
	 */
	private boolean isRoot;

	/**
	 * 
	 * @param position
	 * @param pkgManager
	 * @param pkgName
	 * @param version
	 */
	public PackageDependency(FilePosition position, String pkgManager, String pkgName, String version)
	{
		this.position = position;
		this.name = pkgName;
		this.version = version;
		this.pkgManager = pkgManager;
	}
	/**
	 * 
	 * @param position
	 * @param pkgManager
	 * @param groupId
	 * @param pkgName
	 * @param version
	 */

	public PackageDependency(FilePosition position, String pkgManager, String groupId, String pkgName, String version)
	{
		this.position = position;
		this.groupId = groupId;
		this.name = pkgName;
		this.version = version;
		this.pkgManager = pkgManager;
	}

	/**
	 * 
	 * @param pkgManager
	 * @param pkgName
	 * @param version
	 */
	public PackageDependency(String pkgManager, String pkgName, String version)
	{
		this.name = pkgName;
		this.version = version;
		this.pkgManager = pkgManager;
	}
	
	/**
	 * 
	 * @param pkgManager
	 * @param pkgName
	 * @param version
	 */
	public PackageDependency(String pkgManager, String groupId, String artifactId, String version)
	{
		this.groupId = groupId;
		this.name = artifactId;
		this.version = version;
		this.pkgManager = pkgManager;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof PackageDependency)
		{
			PackageDependency dep = (PackageDependency)o;
			return toString().equals(dep.toString());
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	/** Get the package manager name
	 * 
	 * @return
	 */
	public String getPackageManager()
	{
		return pkgManager;
	}

	/** Get the artifact id/name
	 * 
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	/** Get the group id/name
	 * 
	 * @return
	 */
	public String getGroupId()
	{
		return groupId;
	}
	
	public String getVersion()
	{
		return version;
	}
	
	/** Get the line number of the dependency owner file that defines this dependency
	 * 
	 * @return
	 */
	public int getLine()
	{
		if(position != null)
			return position.getLine();
		return 0;
	}
	public int getOffset()
	{
		if(position != null)
			return position.getOffset();
		return 0;
	}
	public int getLength()
	{
		if(position != null)
			return position.getLength();
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[" + pkgManager + "] " + name + " " + version;
	}

	/** Set the artifact that is deemed to be the best match.
	 * 
	 * @param artifact
	 */
	public void setArtifact(ArtifactResource artifact)
	{
		this.artifact = artifact;
	}

	/** Set the SCM resource that is deemed to be the dependency source
	 * 
	 * @param scmResource
	 */
	public void setScm(ScmResource scmResource)
	{
		this.scm = scmResource;
	}
	
	/** Get the artifact for this dependency
	 * 
	 * @return
	 */
	public ArtifactResource getArtifact()
	{
		return artifact;
	}
	
	/** Get the SCM resource that is deemed to be the dependency source
	 * 
	 * @return
	 */
	public ScmResource getScm()
	{
		return this.scm;
	}

	/** Get a description of the dependency
	 * 
	 * @return
	 */
	public String getDescription()
	{
		if(artifact != null) return artifact.getDescription();
		return "unknown";
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public VulnerabilityResource[] getVulnerabilities() throws IOException
	{
		if(scm != null)
		{
			return scm.getVulnerabilities();
		}
		return new VulnerabilityResource[0];
	}

	/** Get the simple fully descriptive ID for the package
	 * 
	 * @return
	 */
	public String getId()
	{
		return groupId + ":" + name + ":" + version;
	}

	/**
	 * 
	 * @param optional
	 */
	public void setOptional(Boolean optional)
	{
		if(optional != null)
		{
			this.optional  = optional;
		}
	}
	
	/** Is this an optional dependency?
	 * 
	 * @return
	 */
	public boolean getOptional()
	{
		return optional;
	}
	
	/** Is this a root package, which means it is directly referenced (as opposed to transitively).
	 * 
	 * @return
	 */
	public void setIsRoot(boolean b)
	{
		this.isRoot = b;
	}

	/** Is this a root package, which means it is directly referenced (as opposed to transitively).
	 * 
	 * @return
	 */
	public boolean isRoot()
	{
		return isRoot;
	}
}
