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

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import net.ossindex.common.resource.ArtifactResource;
import net.ossindex.common.utils.PackageDependency;

/** Test the FileResource
 * 
 * @author Ken Duck
 *
 */
public class ArtifactResourceTest
{
	/** Test retrieve npm data
	 * 
	 * @throws IOException On error
	 */
	@Test
	public void testAsync() throws IOException
	{
//		AbstractRemoteResource.setDebug(true);
		PackageDependency dep = new PackageDependency("npm", "async", ">0");
		ArtifactResource resource = ResourceFactory.getResourceFactory().findArtifactResource(dep);
		assertTrue(resource.getId() > 0);
		assertTrue(resource.getName().startsWith("async"));
	}

	/** Test retrieve maven data
	 * 
	 * Ignore until apache repositories are added to OSS Index
	 * 
	 * @throws IOException On error
	 */
	@Test
	public void testCommonsLang() throws IOException
	{
//		AbstractRemoteResource.setDebug(true);
		PackageDependency dep = new PackageDependency("maven", "commons-lang3", "3.4");
		ArtifactResource[] resources = ResourceFactory.getResourceFactory().findArtifactResources(new PackageDependency[] {dep});
		boolean found = false;
		for (ArtifactResource resource : resources) {
			assertTrue(resource.getId() > 0);
			assertTrue(resource.getName().startsWith("commons-lang3"));
			long scmId = resource.getScmId();
			if(scmId > 0) found = true;
		}
		assertTrue(found);
	}

	/** Test retrieve maven data
	 * 
	 * @throws IOException On error
	 */
	@Test
	public void testJavaSemver() throws IOException
	{
//		AbstractRemoteResource.setDebug(true);
		PackageDependency dep = new PackageDependency("maven", "java-semver", "0.9.0");
		ArtifactResource[] resources = ResourceFactory.getResourceFactory().findArtifactResources(new PackageDependency[] {dep});
		for (ArtifactResource resource : resources) {
			assertTrue(resource.getId() > 0);
			assertTrue(resource.getName().startsWith("java-semver"));
			long scmId = resource.getScmId();
			assertTrue(scmId > 0);
		}
	}

	/** Test retrieve maven data
	 * 
	 * @throws IOException On error
	 */
	@Test
	public void testSlf4jApi() throws IOException
	{
//		AbstractRemoteResource.setDebug(true);
		PackageDependency dep = new PackageDependency("maven", "slf4j-api", "1.7.12");
		ArtifactResource[] resources = ResourceFactory.getResourceFactory().findArtifactResources(new PackageDependency[] {dep});
		boolean found = false;
		for (ArtifactResource resource : resources) {
			assertTrue(resource.getId() > 0);
			assertTrue(resource.getName().startsWith("slf4j-api"));
			long scmId = resource.getScmId();
			if(scmId > 0) found = true;
		}
		assertTrue(found);
	}

	/** Test getting data for multiple dependencies
	 * 
	 * @throws IOException On error
	 */
	@Test
	public void testMultiplePackages() throws IOException
	{
//		AbstractRemoteResource.setDebug(true);
		PackageDependency dep1 = new PackageDependency("maven", "slf4j-api", "1.7.12");
		PackageDependency dep2 = new PackageDependency("maven", "java-semver", "0.9.0");
		//PackageDependency dep3 = new PackageDependency("maven", "commons-lang3", "3.4");
		ArtifactResource[] resources = ResourceFactory.getResourceFactory().findArtifactResources(new PackageDependency[] {dep1, dep2});
		boolean foundSlf4j = false;
		boolean foundSemver = false;
		for (ArtifactResource resource : resources) {
			assertTrue(resource.getId() > 0);
			if(resource.getName().startsWith("slf4j-api"))
			{
				long scmId = resource.getScmId();
				if(scmId > 0) foundSlf4j = true;
			}
			if(resource.getName().startsWith("java-semver"))
			{
				long scmId = resource.getScmId();
				if(scmId > 0) foundSemver = true;
			}
//			if(resource.getName().startsWith("commons-lang3"))
//			{
//				long scmId = resource.getScmId();
//				assertTrue(scmId > 0);
//			}
		}
		assertTrue(foundSlf4j);
		assertTrue(foundSemver);
	}
}
