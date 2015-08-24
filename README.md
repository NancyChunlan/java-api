# OSS Index java-api

This project contains a Java API to the OSS Index web service. This API wraps the REST calls which provide access to the OSS Index database.

Functionality of the API will roughly track the availability of the REST API itself, and will progress reasonably rapidly over the next several weeks. This code builds either using Maven, or as an Eclipse plugin for use with the [OSS Index Eclipse integration](https://github.com/OSSIndex/ossindex-eclipse). We do not currently use Tycho to build the Eclipse plugins, but that is surely to come at some point in the future.

Sample code can be found in the `plugins/net.ossindex.common/src/main/java/net/ossindex/examples` folder.

Examples
--------

### LsOss.java

```
Usage: java -cp <classpath> net.ossindex.examples.LsOss [options] <file> [file...]

Identify files that are open source (found in OSS Index)

 options:

   -v    Verbose output

Limitation: Command will fail with exit code if too many files
            are passed on the command line, or if there are
            connection problems with the server
```

**Sample verbose output**

```
The following files were found in the OSS Index:

  /home/vor/js/jquery-1.11.2.min.js
  /home/vor/js/jquery-1.11.2.min.map
  /home/vor/js/jquery.animate-enhanced.js
  /home/vor/js/jquery.js
  /home/vor/js/jquery.simulate.js

Checked 7 files
Identified 5 files
```
