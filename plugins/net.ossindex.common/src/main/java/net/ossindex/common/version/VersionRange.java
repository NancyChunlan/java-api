/************************************************************************
 * Copyright (c) 2015 TwoDucks, Inc.
 * All rights reserved.
 ************************************************************************/
package net.ossindex.common.version;

import com.github.zafarkhaja.semver.Version;

/** Represent a range of versions.
 * 
 * @author Ken Duck
 *
 */
public class VersionRange
{

	private String[] ranges;

	public VersionRange(String[] ranges)
	{
		this.ranges = ranges;
	}

	public VersionRange(String range)
	{
		if(range != null)
		{
			ranges = new String[1];
			ranges[0] = range;
		}
		else
		{
			ranges = new String[0];
		}
	}

	/** Return true if the provided range intersects our range
	 * 
	 * @param yourRange
	 * @return
	 */
	public boolean intersects(VersionRange range)
	{
		for(String myRange: ranges)
		{
			if(intersects(myRange, range)) return true;
		}
		return false;
	}

	/** Given two semantic ranges, see if they intersect
	 * 
	 * @param myRange
	 * @param range
	 * @return
	 */
	private boolean intersects(String range1, VersionRange range2)
	{
		for(String childRange: range2.ranges)
		{
			if(intersects(range1, childRange)) return true;
		}
		return false;
	}

	/**
	 * 
	 * @param range1
	 * @param range2
	 * @return
	 */
	private boolean intersects(String range1, String range2)
	{
		// try explicit versions
		try
		{
			Version v1 = Version.valueOf(range1);
			return v1.satisfies(range2);
		}
		catch(Exception e)
		{
			// Ignore
		}
		
		try
		{
			Version v2 = Version.valueOf(range2);
			return v2.satisfies(range1);
		}
		catch(Exception e)
		{
			// Ignore
		}
		
		// Try looser semantic versions
		try
		{
			SemanticVersion v1 = new SemanticVersion(range1);
			return v1.satisfies(range2);
		}
		catch(Exception e)
		{
			// Ignore
		}
		
		try
		{
			SemanticVersion v2 = new SemanticVersion(range2);
			return v2.satisfies(range1);
		}
		catch(Exception e)
		{
			// Ignore
		}
		

		
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < ranges.length; i++)
		{
			if(i > 0) sb.append(",");
			sb.append(ranges[i]);
		}
		return sb.toString();
	}
}
