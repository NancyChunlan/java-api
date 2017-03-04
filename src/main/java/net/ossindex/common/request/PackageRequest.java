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
	
	@Override
	public void add(String pm, String groupId, String artifactId, String version) {
		packages.add(new PackageDescriptor(pm, groupId, artifactId, version));
	}

	@Override
	public Collection<PackageDescriptor> run() {
		String data = gson.toJson(packages);
		try {
			// Perform the OSS Index query
			String response = this.performPostRequest("package", data);
			System.err.println(response);
			
			// Convert the results to Java objects
			Type listType = new TypeToken<List<PackageDescriptor>>() {}.getType();
			return gson.fromJson(response, listType);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
