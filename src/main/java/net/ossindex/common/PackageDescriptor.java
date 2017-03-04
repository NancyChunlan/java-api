/**
 *	Copyright (c) 2017 Vör Security Inc.
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

import java.util.List;

import com.google.gson.annotations.SerializedName;

/** Represents an OSS Index package.
 * 
 * @author Ken Duck
 *
 */
public class PackageDescriptor {

	private long id;
	private String pm;
	private String name;
	private String version;
	private String group;

	@SerializedName("vulnerability-total")
	private int vulnerabilityTotal;

	@SerializedName("vulnerability-matches")
	private int vulnerabilityMatches;

	private List<VulnerabilityDescriptor> vulnerabilities;

	public PackageDescriptor(String pm, String groupId, String artifactId, String version) {
		this.pm = pm;
		this.name = artifactId;
		this.version = version;
		this.group = groupId;
	}

	public List<VulnerabilityDescriptor> getVulnerabilities() {
		return vulnerabilities;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PKG: [" + id + "] " + pm + "::" + group + "." + name + " " + version);
		return sb.toString();
	}

	public String getGroup() {
		if (group != null) {
			return group;
		}
		return "";
	}

	public String getName() {
		if (name != null) {
			return name;
		}
		return "";
	}

	public String getVersion() {
		if (version != null) {
			return version;
		}
		return "";
	}

	public int getVulnerabilityTotal() {
		return vulnerabilityTotal;
	}

	public int getVulnerabilityMatches() {
		return vulnerabilityMatches;
	}
}
