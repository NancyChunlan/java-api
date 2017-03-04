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
}
