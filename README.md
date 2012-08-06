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

After unpacking Nexus Pro you should find the nexus-custom-metadata-plugin-client.jar file under the optional-plugins.
Take that JAR file and add it to your local Maven Repository or to your Nexus installation.

- Directory: nexus-professional-trial-2.0.6-bundle\nexus-professional-trial-2.0.6\nexus\WEB-INF\optional-plugins\nexus-custom-metadata-plugin-2.0.6\nexus-custom-metadata-plugin-2.0.6\docs\

    mvn install:install-file -Dfile=nexus-custom-metadata-plugin-client.jar -DgroupId=org.sonatype.nexus -DartifactId=nexus-custom-metadata-plugin-client -Dversion=1.0
    

                       