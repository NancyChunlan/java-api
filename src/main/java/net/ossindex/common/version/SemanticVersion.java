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

import com.github.zafarkhaja.semver.Version;

/** Useful docs here: https://github.com/zafarkhaja/jsemver
 * 
 * Semantic version parsing code.
 * 
 * Semantic versioning is described here: http://semver.org/
 * 
 * Subclass this class to provide special preprocessing and comparison handling.
 * 
 * @author Ken Duck
 *
 */
public class SemanticVersion implements Comparable<SemanticVersion>
{
	private Version v;
	
	/** Use an external library for parsing.
	 * 
	 * @param buf
	 */
	public SemanticVersion(String buf)
	{
		v = Version.valueOf(preprocess(buf));
	}

	/** Ensure there is a hyphen before the suffix
	 * 
	 * @param buf
	 * @return
	 */
	private String preprocess(String buf)
	{
		int index;
		// Now find the end of numerals and dots, we'll call what remains the
		// 'suffix'.
		for(index = 0; index < buf.length(); index++)
		{
			char c = buf.charAt(index);
			if(c < '0' || c > '9')
			{
				if(c != '.')
				{
					break;
				}
			}
		}
		
		String ver = buf.substring(0, index);
		String[] tokens = ver.split("\\.");
		StringBuilder sb = new StringBuilder();
		for (int tokenIndex = 0; tokenIndex < tokens.length; tokenIndex++)
		{
			String token = tokens[tokenIndex];
			if(tokenIndex > 0) sb.append(".");
			boolean hasNonZero = false;
			for(int i = 0; i < token.length(); i++)
			{
				char c = token.charAt(i);
				if(hasNonZero)
				{
					sb.append(c);
				}
				else if(c > '0' && c <= '9')
				{
					sb.append(c);
					hasNonZero = true;
				}
			}
			if(!hasNonZero) sb.append('0');
		}
		buf = sb.toString();

		if(index < buf.length())
		{
			String suffix = buf.substring(index);
			// Remove hyphens, we'll put them back later
			while(suffix.startsWith("-")) suffix = suffix.substring(1);
			buf = buf.substring(0, index) + "-" + suffix;
		}
		return buf;
	}

	/**
	 * Special case for when we cannot parse a semantic version.
	 */
	public SemanticVersion()
	{
	}

	/**
	 * 
	 * @return
	 */
	public int getMajor()
	{
		if(v == null) return 0;
		return v.getMajorVersion();
	}

	/**
	 * 
	 * @return
	 */
	public int getMinor()
	{
		if(v == null) return 0;
		return v.getMinorVersion();
	}

	/**
	 * 
	 * @return
	 */
	public int getPatch()
	{
		if(v == null) return 0;
		return v.getPatchVersion();
	}

	/**
	 * 
	 * @return
	 */
	public boolean isNamed()
	{
		return v == null;
	}

	/**
	 * 
	 * @param range
	 * @return
	 */
	public boolean satisfies(String range)
	{
		if(v == null) return false;
		
		// Convert some range variants to what the Version class expects
		if(range.endsWith(".x")) range = range.substring(0, range.length() - 2);
		
		return v.satisfies(range);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof SemanticVersion)
		{
			SemanticVersion v = (SemanticVersion)o;
			if(this.v != null)
			{
				return this.v.equals(v.v);
			}
			else
			{
				return super.equals(v);
			}
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
		if(v != null)
		{
			return v.hashCode();
		}
		return super.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SemanticVersion other)
	{
		if(this.isNamed())
		{
			if(other.isNamed())
			{
				// If they are both named, compare them as strings
				return toString().compareTo(other.toString());
			}
			else
			{
				return -1;
			}
		}
		else if(other.isNamed())
		{
			return 1;
		}
		
		// If neither is named, then compare them as semantic values
		return this.v.compareTo(other.v);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return v.toString();
	}

	/** Returns true if this represents a stable release. We take this to mean
	 * unnamed and no suffix.
	 * 
	 * @return
	 */
	public boolean isStable()
	{
		if(!isNamed())
		{
			return true;
		}
		return false;
	}
}
