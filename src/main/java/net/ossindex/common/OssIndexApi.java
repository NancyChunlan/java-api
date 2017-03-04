package net.ossindex.common;

import net.ossindex.common.request.PackageRequest;

/** Main class for access of the OSS Index API. Use this to create request
 * objects.
 * 
 * @author Ken Duck
 *
 */
public class OssIndexApi {
	
	public static IPackageRequest createPackageRequest() {
		return new PackageRequest();
	}
}
