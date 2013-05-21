<h3>Summary</h3>

This Jenkins Plugin adds additional metadata to artifacts that are deployed to Nexus repository (Sonatype). 
It makes use of features from Nexus Professional.

This source code is part of a tutorial that you can find here:<br>
http://blog.codecentric.de/en/2012/08/tutorial-create-a-jenkins-plugin-to-integrate-jenkins-and-nexus-repository/

<h3>Installation</h3>

    git@github.com:marcelbirkner/nexus-metadata-plugin.git
    cd nexus-metadata-plugin
    mvn clean package

<h3>Requirements</h3>

- Jenkins CI Server
- Nexus Pro (Sonatype)
- Nexus Metadata Plugin

<h4>Maven Dependencies</h4>

After unpacking Nexus Pro you should find the *nexus-custom-metadata-plugin-2.0.6.jar* file under the optional-plugins.
Take that JAR file and add it to your local Maven Repository or to your Nexus installation.

- Directory: nexus-professional-trial-2.0.6-bundle\nexus-professional-trial-2.0.6\nexus\WEB-INF\optional-plugins\nexus-custom-metadata-plugin-2.0.6\nexus-custom-metadata-plugin-2.0.6\

```  
mvn install:install-file 
  -Dfile=nexus-custom-metadata-plugin.jar 
  -DgroupId=org.sonatype.nexus 
  -DartifactId=nexus-custom-metadata-plugin-client 
  -Dversion=1.0
```


<h5>Updates</h5>

(05/2013): The latest versions of Nexus do not have the nexus-custom-metadata-plugin Jar file any more. Please use Nexus Pro version 2.0.6 in case you want to try out this source code.


