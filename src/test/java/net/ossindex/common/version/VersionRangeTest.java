package net.ossindex.common.version;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 * @author Ken Duck
 *
 */
public class VersionRangeTest
{
	@Test
	public void testIntersection()
	{
		VersionRange range1 = new VersionRange(">1.6.1");
		VersionRange range2 = new VersionRange(new String[] {"1.6","1.6.1","1.6.2"});
		
		assertTrue(range1.intersects(range2));
	}
}
