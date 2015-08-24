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
		FileResource resource = FileResource.find(empty);
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
		FileResource resource = FileResource.find(unique);
		assertTrue(resource.getId() < 0);
		assertNull(resource.getName());
	}

}
