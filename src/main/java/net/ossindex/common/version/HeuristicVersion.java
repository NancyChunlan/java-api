/**
 *	Copyright (c) 2015 Vör Security Inc.
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
package net.ossindex.common.version;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Handle version numbers that are not defined using precise rules (or at least,
 * we don't know they have precise rules). This will try and guess based on various
 * common known versioning methods.
 * 
 * If the version cannot be parsed we retain a "named" version.
 * 
 * @author Ken Duck
 *
 */
public class HeuristicVersion extends SemanticVersion
{
	/**
	 * Variable used to record information stripped from input
	 * string prior to semantic version checking.
	 */
	private static String staticPrefix;

	private static String staticSuffix;

	
	public static Pattern mdmdp_s = Pattern.compile("^([0-9]+)\\.([0-9]+)\\.([0-9]+)(.*)?$");
	public static Pattern mdm_s = Pattern.compile("^([0-9]+)\\.([0-9]+)(.*)?$");
	public static Pattern m_s = Pattern.compile("^([0-9]+)(.*)?$");

	/**
	 * 
	 */
	private Date date;

	private String prefix;
	private String suffix;

	/**
	 * This is a named variable -- we couldn't parse it
	 */
	private String name;
	
	/** Create a version that has a date attached. This is useful when comparing
	 * named versions.
	 * 
	 * @param name
	 * @param date
	 */
	private HeuristicVersion(String buf, Date date)
	{
		super(buf);
		this.date = date;
		
		prefix = staticPrefix;
		suffix = staticSuffix;
	}

	/**
	 * 
	 * @param buf
	 * @param date2
	 * @param named This is a named version (we couldn't parse)
	 */
	public HeuristicVersion(String buf, Date date, boolean named)
	{
		super();
		this.date = date;
		this.name = buf;
	}

	/** Synchronized because constructing requires the use of static variables.
	 * 
	 * @param buf
	 * @param date
	 * @return
	 */
	public synchronized static HeuristicVersion getHeuristicVersion(String buf, Date date)
	{
		String ver = preprocess(buf);
		String[] tokens = ver.split("\\.");
		switch(tokens.length)
		{
		case 2:
			ver = ver + ".0";
			break;
		case 3:
			// In some rare cases the patch version starts with a zero. Consider this
			// a suffix instead.
			if(tokens[2].length() > 1 && tokens[2].startsWith("0"))
			{
				ver = tokens[0] + "." + tokens[1] + ".0";
				if(staticSuffix == null) staticSuffix = "p" + tokens[2];
				else staticSuffix = "p" + tokens[2] + "-" + staticSuffix;
			}
			break;
		case 4:
			// Ignore the last digit. Assume it is a suffix.
			ver = tokens[0] + "." + tokens[1] + "." + tokens[2];
			if(staticSuffix == null) staticSuffix = "p" + tokens[3];
			else staticSuffix = "p" + tokens[3] + "-" + staticSuffix;
			break;
		default:
			return new HeuristicVersion(buf, date, true);
		}
		try
		{
			if(staticSuffix == null) return new HeuristicVersion(ver, date);
			staticSuffix = staticSuffix.trim();
			return new HeuristicVersion(ver + "-" + staticSuffix, date);
		}
		catch(Exception e)
		{
			System.err.println("Getting named version: " + buf);
			return new HeuristicVersion(buf, date, true);
		}
	}
	
	/**
	 * 
	 * @param buf
	 * @return
	 */
	public static HeuristicVersion getHeuristicVersion(String buf)
	{
		return getHeuristicVersion(buf, null);
	}

	/** Preprocess to try to create a reasonable semantic version. Keep the
	 * remainder of the information in this class. This requires the use of
	 * some static fields, which means the constructors.
	 * 
	 * @param buf
	 * @return
	 */
	private static String preprocess(String buf)
	{
		// Ensure these always start fresh
		staticPrefix = null;
		staticSuffix = null;
		
		// First remove any prefix information
		int index;
		for(index = 0; index < buf.length(); index++)
		{
			char c = buf.charAt(index);
			if(c >= '0' && c <= '9') break;
		}
		if(index < buf.length())
		{
			staticPrefix = buf.substring(0, index);
			buf = buf.substring(index);
		}
		
		// Major.Minor.Patch-suffix
		Matcher m = mdmdp_s.matcher(buf);
		if(m.matches())
		{
			if(m.groupCount() == 4) staticSuffix = m.group(4);
			return m.group(1) + "." + m.group(2) + "." + m.group(3);
		}
		
		// Major.Minor-suffix
		m = mdm_s.matcher(buf);
		if(m.matches())
		{
			if(m.groupCount() == 3) staticSuffix = m.group(3);
			return m.group(1) + "." + m.group(2) + ".0";
		}
		
		// Major suffix
		m = m_s.matcher(buf);
		if(m.matches())
		{
			if(m.groupCount() == 2) staticSuffix = m.group(2);
			return m.group(1) + ".0.0";
		}

		return buf;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.vor.utils.version.SemanticVersion#toString()
	 */
	@Override
	public String toString()
	{
		if(name != null) return name;
		return super.toString();
	}

	/** Get the version's suffix.
	 * 
	 * @return
	 */
	public String getSuffix()
	{
		return suffix;
	}

	/**
	 * 
	 * @return
	 */
	public boolean hasSuffix()
	{
		return suffix != null && !suffix.trim().isEmpty();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.vor.utils.version.SemanticVersion#isStable()
	 */
	@Override
	public boolean isStable()
	{
		if(!isNamed())
		{
			if(!hasSuffix()) return true;
		}
		return false;
	}
}
