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


/** Representation of the FileResource, backed by the OSS Index REST API
 * 
 * @author Ken Duck
 *
 */
public class FileResource extends AbstractRemoteResource
{
	/**
	 * Temporary boolean for debugging purposes.
	 */
	private static boolean DEBUG = false;
	
	/**
	 * File name. May be populated by OSS Index, but can be
	 * overridden by local file.
	 */
	private String name = null;
	
	/**
	 * Required for deserialization
	 */
	FileResource()
	{
	}
	
	/** Create a new file resource
	 * 
	 * @param id The ID of the resource in OSS Index
	 */
	public FileResource(long id)
	{
		super(id);
	}

	/**Get the name of the resource
	 * 
	 * @return The name of the resource
	 */
	public String getName()
	{
		return name;
	}
	
	/** Override the name provided by OSS Index.
	 * 
	 * @param name File's name
	 */
	public void setName(String name)
	{
		this.name = name;
	}


	/*
	 * (non-Javadoc)
	 * @see net.ossindex.common.resource.AbstractRemoteResource#getResourceType()
	 */
	@Override
	protected String getResourceType()
	{
		return "file";
	}
}
