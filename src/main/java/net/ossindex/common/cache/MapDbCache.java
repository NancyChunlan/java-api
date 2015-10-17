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

import java.io.File;
import java.util.concurrent.ConcurrentNavigableMap;

import net.ossindex.common.IOssIndexCache;

import org.mapdb.DB;
import org.mapdb.DBMaker;

/** An implementation of IOssIndexCache that uses MapDb as a back end.
 * 
 * @author Ken Duck
 *
 */
public class MapDbCache implements IOssIndexCache
{
	private DB db;
	private ConcurrentNavigableMap<String,String> map;

	/** Create a cache at the specified location
	 * 
	 * @param root Root directory for cache storage
	 */
	public MapDbCache(File root)
	{
		db = DBMaker.fileDB(new File(root, "ossindex.cache"))
				.closeOnJvmShutdown()
				.make();
		map = db.treeMap("queryCache");
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.common.IOssIndexCache#cache(java.lang.String, java.lang.String)
	 */
	@Override
	public void cache(String requestString, String json)
	{
		map.put(requestString, "(T=" + System.currentTimeMillis() + ")" + json);
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.common.IOssIndexCache#get(java.lang.String)
	 */
	@Override
	public String get(String requestString)
	{
		String buf = map.get(requestString);
		if(buf != null)
		{
			if(buf.startsWith("(T="))
			{
				int index = buf.indexOf(')');
				buf = buf.substring(index + 1);
			}
		}
		return buf;
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.common.IOssIndexCache#get(java.lang.String, long)
	 */
	@Override
	public String get(String requestString, long delay)
	{
		if(delay > 0)
		{
			String buf = map.get(requestString);
			if(buf != null)
			{
				if(buf.startsWith("(T="))
				{
					buf = buf.substring(3);
					int index = buf.indexOf(')');
					long timestamp = Long.parseLong(buf.substring(0, index));
					buf = buf.substring(index + 1);
					long now = System.currentTimeMillis();
					if((now - timestamp) < delay)
					{
						return buf;
					}
				}
				else
				{
					// This will only happen to old stored data, and should
					// never happen again.
					return null;
				}
			}
			return null;
		}
		else
		{
			return get(requestString);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.common.IOssIndexCache#commit()
	 */
	@Override
	public void commit()
	{
		db.commit();
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.common.IOssIndexCache#close()
	 */
	@Override
	public void close()
	{
		db.commit();
		//db.compact();
		db.close();
	}

}
