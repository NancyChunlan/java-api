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
package net.ossindex.common.cache;

import java.util.HashMap;
import java.util.Map;

import net.ossindex.common.IOssIndexCache;

/** Memory only cache
 * 
 * @author Ken Duck
 *
 */
public class MemoryCache implements IOssIndexCache
{
	private Map<String,String> map;
	
	/** Create a cache at the specified location
	 * 
	 * @param root
	 */
	public MemoryCache()
	{
		map = new HashMap<String,String>();
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.common.IOssIndexCache#cache(java.lang.String, java.lang.String)
	 */
	@Override
	public void cache(String requestString, String json)
	{
		map.put(requestString, json);
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.common.IOssIndexCache#get(java.lang.String)
	 */
	@Override
	public String get(String requestString)
	{
		return map.get(requestString);
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.common.IOssIndexCache#commit()
	 */
	@Override
	public void commit()
	{
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.ossindex.common.IOssIndexCache#close()
	 */
	@Override
	public void close()
	{
	}

}
