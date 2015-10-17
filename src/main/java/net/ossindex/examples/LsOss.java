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
package net.ossindex.examples;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.ossindex.common.ResourceFactory;
import net.ossindex.common.resource.FileResource;

/** LsOss lists all of the open source files within a given list
 * of files.
 * 
 * @author Ken Duck
 *
 */
public class LsOss
{
	/**
	 * Set to true if we want verbose output.
	 */
	private boolean verbose;
	
	/**
	 * List of files to find in the OSS Index
	 */
	private File[] files;
	
	/** Very simple argument parsing.
	 * 
	 * @param args Command arguments
	 */
	public LsOss(String[] args)
	{
		int i = 0;
		if(args.length > 0)
		{
			if("-v".equals(args[i]))
			{
				verbose = true;
				i++;
			}
		}
		
		if(i >= args.length)
		{
			usage();
		}
		
		// Identify all files that exist.
		List<File> files = new LinkedList<File>();
		while(i < args.length)
		{
			File file = new File(args[i]);
			if(file.exists() && file.isFile())
			{
				files.add(file);
			}
			i++;
		}
		this.files = files.toArray(new File[files.size()]);
	}


	/** Run OSS Index against the passed in files.
	 * 
	 * This does not do much error checking at all. It passes the
	 * problems up to the top whereupon they will become the users
	 * problem.
	 * 
	 * @throws IOException  On error
	 * 
	 */
	private void run() throws IOException
	{
		// Perform the actual request from OSS Index. This should
		// return a list of file resources the same size as the requested
		// files list. The result array is the same length as the request
		// array. Null entries in the result array indicate files that were
		// not found within the OSS Index database.
		FileResource[] resources = ResourceFactory.getResourceFactory().findFileResources(files);
		
		// Very blunt error handling.
		if(resources == null)
			throw new IOException("Unexpected results from FileResource.find(...)");
		
		if(resources.length != files.length)
			throw new IOException("Mismatch between request size and response size. " + files.length + " != " + resources.length);
		
		if(verbose)
		{
			System.out.println("The following files were found in the OSS Index:");
		}
		
		int identified = 0;
		
		for(int i = 0; i < resources.length; i++)
		{
			if(resources[i] != null)
			{
				identified++;
				if(verbose) System.out.print("  ");
				System.out.println(files[i].getAbsolutePath());
			}
		}
		
		if(verbose)
		{
			System.out.println("Checked " + files.length + " files");
			System.out.println("Identified " + identified + " files");
		}
	}
	
	/**
	 * Print usage and exit
	 */
	private void usage()
	{
		System.err.println("Usage: lsoss [options] <file> [file...]");
		System.err.println();
		System.err.println("Identify files that are open source (found in OSS Index)");
		System.err.println();
		System.err.println(" options:");
		System.err.println();
		System.err.println("   -v    Verbose output");
		System.err.println();
		System.err.println("Limitation: Command will fail with exit code if too many files");
		System.err.println("            are passed on the command line, or if there are");
		System.err.println("            connection problems with the server");
		System.exit(1);
	}

	/** Program main
	 * 
	 * @param args Program arguments
	 * @throws IOException On error
	 */
	public static void main(String[] args) throws IOException
	{
		LsOss lsoss = new LsOss(args);
		lsoss.run();
	}
}
