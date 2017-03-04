package net.ossindex.common;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

/** Test the package requests.
 * 
 * @author Ken Duck
 *
 */
public class PackageRequestTest {
	
	@Test
	public void singlePackageRequest() {
		IPackageRequest req = OssIndexApi.createPackageRequest();
		req.add("maven", "org.webjars.bower", "jQuery", "1.9");
		Collection<PackageDescriptor> packages = req.run();
		System.err.println("RESPONSE:");
		if (packages != null) {
			for (PackageDescriptor pkg : packages) {
				System.err.println("  " + pkg);
				List<VulnerabilityDescriptor> vulns = pkg.getVulnerabilities();
				if (vulns != null) {
					for (VulnerabilityDescriptor vuln : vulns) {
						System.err.println("    - " + vuln);
					}
				}
			}
		}
	}
}
