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
	 * The parent is the package that brought this one in as a transitive dependency.
	 */
	private PackageDependency parent;

	/** Create a package dependency
	 * 
	 * @param position Position of the dependency
	 * @param pkgManager Package manager the dependency belongs to
	 * @param pkgName The package name
	 * @param version The version
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

	/** Create a package dependency
	 * 
	 * @param pkgManager Package manager the dependency belongs to
	 * @param pkgName The package name
	 * @param version The version
	 */
	public PackageDependency(String pkgManager, String pkgName, String version)
	{
		this.name = pkgName;
		this.version = version;
		this.pkgManager = pkgManager;
	}
	
	/** Create a package dependency
	 * 
	 * @param pkgManager Package manager the dependency belongs to
	 * @param groupId The groupId of the package
	 * @param artifactId The artifact name
	 * @param version The version
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
	 * @return The package manager name
	 */
	public String getPackageManager()
	{
		return pkgManager;
	}

	/** Get the artifact id/name
	 * 
	 * @return The package name
	 */
	public String getName()
	{
		return name;
	}
	
	/** Get the group id/name
	 * 
	 * @return The group ID name
	 */
	public String getGroupId()
	{
		return groupId;
	}
	
	/** Get the package version
	 * 
	 * @return The version string
	 */
	public String getVersion()
	{
		return version;
	}
	
	/** Get the line number of the dependency owner file that defines this dependency
	 * 
	 * @return File line the package was found at
	 */
	public int getLine()
	{
		if(position != null)
			return position.getLine();
		return 0;
	}
	
	/** Get the character offset
	 * 
	 * @return Character offset the package was found at
	 */
	public int getOffset()
	{
		if(position != null)
			return position.getOffset();
		return 0;
	}
	
	/** Length of the selection
	 * 
	 * @return Length of the selection
	 */
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
	 * @param artifact Set the artifact for the package
	 */
	public void setArtifact(ArtifactResource artifact)
	{
		this.artifact = artifact;
	}

	/** Set the SCM resource that is deemed to be the dependency source
	 * 
	 * @param scmResource The SCM resource that was found for the package
	 */
	public void setScm(ScmResource scmResource)
	{
		this.scm = scmResource;
	}
	
	/** Get the artifact for this dependency
	 * 
	 * @return The artifact belonging to the dependency
	 */
	public ArtifactResource getArtifact()
	{
		return artifact;
	}
	
	/** Get the SCM resource that is deemed to be the dependency source
	 * 
	 * @return The SCM the package belongs to
	 */
	public ScmResource getScm()
	{
		return this.scm;
	}

	/** Get a description of the dependency
	 * 
	 * @return The package description
	 */
	public String getDescription()
	{
		if(artifact != null) return artifact.getDescription();
		return "unknown";
	}

	/** Get the vulnerabilities that apply to the package
	 * 
	 * @return An array of vulnerabilities that affect the package
	 * @throws IOException On error
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
	 * @return Return a string ID describing the package
	 */
	public String getId()
	{
		return groupId + ":" + name + ":" + version;
	}

	/** Indicates that this is an "optional" package
	 * 
	 * @param optional True if this is an optional package
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
	 * @return True if this is an optional package
	 */
	public boolean getOptional()
	{
		return optional;
	}
	
	/** Is this a root package, which means it is directly referenced
	 * (as opposed to transitively).
	 * 
	 * @param b True if this is a root package
	 */
	public void setIsRoot(boolean b)
	{
		this.isRoot = b;
	}

	/** Is this a root package, which means it is directly referenced
	 * (as opposed to transitively).
	 * 
	 * @return True if this package is the "root" package
	 */
	public boolean isRoot()
	{
		return isRoot;
	}
	
	/** The parent is the package that brought this one in as a transitive dependency.
	 * 
	 * @param pkg
	 */
	public void setParent(PackageDependency pkg)
	{
		parent = pkg;
	}
	
	/** The parent is the package that brought this one in as a transitive dependency.
	 * 
	 * @return
	 */
	public PackageDependency getParent()
	{
		return parent;
	}
}
