package net.ossindex.common;

import java.util.Collection;

/** Interface to be implemented for the package request API.
 * 
 * https://ossindex.net/b/16-07/24.package-search
 * 
 * @author Ken Duck
 *
 */
public interface IPackageRequest {

	/**
	 * Add a new artifact to search for. 
	 */
	public void add(String packageManager, String groupId, String artifactId, String version);

	/**
	 * Execute the request.
	 */
	public Collection<PackageDescriptor> run();

}
