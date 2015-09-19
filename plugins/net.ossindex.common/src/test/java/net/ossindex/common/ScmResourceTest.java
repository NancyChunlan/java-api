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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import net.ossindex.common.resource.AbstractRemoteResource;
import net.ossindex.common.resource.ScmResource;
import net.ossindex.common.resource.VulnerabilityResource;

import org.junit.Test;

/** Test the FileResource
 * 
 * @author Ken Duck
 *
 */
public class ScmResourceTest
{
	@Test
	public void testSingleScmFind() throws IOException
	{
		AbstractRemoteResource.setDebug(true);
		ScmResource[] resources = ScmResource.find(new long[] {296375846});
		assertNotNull(resources);
		assertEquals(1, resources.length);
		assertTrue(resources[0].getId() > 0);
		assertEquals("jquery", resources[0].getName());
	}

	@Test
	public void testDoubleScmFind() throws IOException
	{
		AbstractRemoteResource.setDebug(true);
		ScmResource[] resources = ScmResource.find(new long[] {296375846, 290424103});
		assertNotNull(resources);
		assertEquals(2, resources.length);
		assertTrue(resources[0].getId() > 0);
		assertEquals("jquery", resources[0].getName());
		assertTrue(resources[1].getId() > 0);
		assertEquals("async", resources[1].getName());
	}

	@Test
	public void testGetVulnerabilities() throws IOException
	{
		AbstractRemoteResource.setDebug(true);
		ScmResource[] resources = ScmResource.find(new long[] {296375846});
		assertNotNull(resources);
		assertEquals(1, resources.length);
		assertTrue(resources[0].getId() > 0);
		assertEquals("jquery", resources[0].getName());
		
		VulnerabilityResource[] vulnerabilities = resources[0].getVulnerabilities();
		assertNotNull(vulnerabilities);
		assertEquals(2, vulnerabilities.length);
	}


}
