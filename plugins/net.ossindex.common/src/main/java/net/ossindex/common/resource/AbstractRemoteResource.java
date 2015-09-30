/**
 *	Copyright (c) 2015 V�r Security Inc.
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
package net.ossindex.common.resource;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/** Provides common code for OSS Index API access.
 * 
 * @author Ken Duck
 *
 */
public abstract class AbstractRemoteResource
{
	/**
	 * Bad form! Hard-coded for prototype.
	 */
	private static String scheme = "https";
	private static String host = "ossindex.net";
	private static int port = -1; // Use default port
	
	/**
	 * OSS Index ID. Special values:
	 * 
	 *   >0 Valid OSS Index resource ID.
	 *   -1 OSS Index not checked
	 *   -2 Resource not in OSS Index
	 */
	private long id = -1;
	
	static
	{
		// Default log4j configuration. Hides configuration warnings.
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.WARN);
	}
	
	/**
	 * Required for deserialization
	 */
	AbstractRemoteResource()
	{
	}
	
	/**
	 * 
	 * @param id
	 */
	public AbstractRemoteResource(long id)
	{
		this.id = id;
	}

	/** Get the OSS Index ID.
	 * 
	 * @return
	 */
	public long getId()
	{
		return id;
	}
	
	/** Returns true if the resource exists at OSS Index
	 * 
	 * @return
	 */
	public boolean exists()
	{
		return id > 0;
	}
	
	/** Get the base URL for REST requests.
	 * 
	 * @return
	 */
	protected static String getBaseUrl()
	{
		if(port >= 0)
		{
			return scheme + "://" + host + ":" + port;
		}
		else
		{
			return scheme + "://" + host;
		}
	}
	
	/** Get the OSS Index resource type.
	 * 
	 * @return
	 */
	protected abstract String getResourceType();

	public static void setDebug(boolean b)
	{
		if(b)
		{
			scheme = "http";
			host = "localhost";
			port = 8080;
		}
		else
		{
			scheme = "https";
			host = "ossindex.net";
			port = -1; // Use default port
		}
	}
}
