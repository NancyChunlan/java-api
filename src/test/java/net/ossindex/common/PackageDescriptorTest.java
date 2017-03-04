package net.ossindex.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Simple package tests
 * 
 * @author Ken Duck
 *
 */
public class PackageDescriptorTest {
	
	/**
	 * Test equals
	 */
	@Test
	public void simpleDependencyCheck() {
		PackageDescriptor commons1 = new PackageDescriptor("maven", "org.apache.commons", "commons-dbcp2", "2.1.1");
		PackageDescriptor commons2 = new PackageDescriptor("maven", "org.apache.commons", "commons-dbcp2", "2.1.1");
		assertEquals(commons1, commons2);
	}

}
