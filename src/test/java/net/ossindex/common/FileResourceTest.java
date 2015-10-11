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
package net.ossindex.common;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import net.ossindex.common.resource.FileResource;

import org.junit.Test;

/** Test the FileResource
 * 
 * @author Ken Duck
 *
 */
public class FileResourceTest
{
	/**
	 * The empty file is in OSS Index.
	 * @throws IOException 
	 */
	@Test
	public void testEmptyFile() throws IOException
	{
		File empty = File.createTempFile("ossindex.", ".test");
		FileResource resource = ResourceFactory.getResourceFactory().findFileResource(empty);
		assertTrue(resource.getId() > 0);
		assertNotNull(resource.getName());
	}
	
	/** A random file is likely unique. I am using a random file
	 * in case some funny person decides to add my test file to
	 * a repository somewhere.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testUniqueFile() throws IOException
	{
		File unique = File.createTempFile("ossindex.", ".test");
		SecureRandom random = new SecureRandom();
		String randomString = new BigInteger(130, random).toString(32);
		FileWriter out = new FileWriter(unique);
		out.append(randomString);
		out.close();
		FileResource resource = ResourceFactory.getResourceFactory().findFileResource(unique);
		assertTrue(resource.getId() < 0);
		assertNull(resource.getName());
	}

}
