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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zafarkhaja.semver.expr.LexerException;
import com.github.zafarkhaja.semver.expr.UnexpectedTokenException;


/** NPM specific extensions to semantic versioning
 * 
 * @author Ken Duck
 *
 */
public class NpmVersion extends SemanticVersion
{
	private static final Logger LOG = LoggerFactory.getLogger(NpmVersion.class);
	/**
	 * 
	 * @param buf
	 */
	public NpmVersion(String buf)
	{
		super(preprocess(buf));
	}

	/** Remove any syntactic sugar. Some of it is from here: https://github.com/npm/node-semver
	 * 
	 *   o A leading "=" or "v" character is stripped off and ignored.
	 * 
	 * @param buf
	 * @return
	 */
	private static String preprocess(String buf)
	{
		boolean doLoop = true;
		while(doLoop)
		{
			if(buf.length() == 0) break;
			switch(buf.charAt(0))
			{
			case '=':
			case 'v':
				buf = buf.substring(1);
				break;
			default:
				doLoop = false;
				break;
			}
		}
		return buf;
	}

	/**
	 * Some range processing so that the comparison can be handled by our
	 * SemanticVersion library.
	 */
	public boolean satisfies(String range)
	{
		if(range == null) range = "*";
		
		// Our "or" has only one pipe
		range = range.replaceAll("\\|\\|", "|");
		
		// From https://github.com/npm/node-semver
		// 
		//   Comparators can be joined by whitespace to form a comparator set, which
		//   is satisfied by the intersection of all of the comparators it includes.
		//
		// We want an "&" symbol there.
		range = range.replaceAll("([0-9]+) +([<>=])", "$1 & $2");
		
		// Replace .x.x with a single .x (the second is implicit)
		range = range.replaceAll("\\.x\\.x", ".x");
		
		// Shouldn't have wildcards and tildes together
		range = range.replaceAll("~([0-9]+)\\.([0-9]+)\\.x", "~$1.$2");
		
		if(range.trim().isEmpty()) range = "*";
		
		char c = range.charAt(0);
		if(c >= 'a' && c <= 'z') range = "*";
		if(c >= 'A' && c <= 'Z') range = "*";
		
		if(range.contains("-"))
		{
			// Our range matching is imperfect
			int index = range.indexOf('-');
			range = range.substring(0, index);
		}
		
		try
		{
			return super.satisfies(range);
		}
		catch(LexerException e)
		{
			LOG.error("Exception parsing range: '" + range + "'. Assuming satisfied.");
			return true;
		}
		catch(UnexpectedTokenException e)
		{
			LOG.error("Exception parsing range: '" + range + "'. Assuming satisfied.");
			return true;
		}
		
	}
}
