/**
 *	Copyright (c) 2017 Vör Security Inc.
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
package net.ossindex.common.request;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.ossindex.common.IPackageRequest;
import net.ossindex.common.PackageDescriptor;

/** Perform a package request.
 * 
 * https://ossindex.net/b/16-07/24.package-search
 * 
 * @author Ken Duck
 *
 */
public class PackageRequest extends AbstractOssIndexRequest implements IPackageRequest {
	List<PackageDescriptor> packages = new LinkedList<PackageDescriptor>();
	Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	
	/*
	 * (non-Javadoc)
	 * @see net.ossindex.common.IPackageRequest#add(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public PackageDescriptor add(String pm, String groupId, String artifactId, String version) {
		PackageDescriptor desc = new PackageDescriptor(pm, groupId, artifactId, version);
		packages.add(desc);
		return desc;
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.common.IPackageRequest#run()
	 */
	@Override
	public Collection<PackageDescriptor> run() throws IOException {
		String data = gson.toJson(packages);
		// Perform the OSS Index query
		String response = this.performPostRequest("package", data);
		
		// Convert the results to Java objects
		Type listType = new TypeToken<List<PackageDescriptor>>() {}.getType();
		return gson.fromJson(response, listType);
	}

}
