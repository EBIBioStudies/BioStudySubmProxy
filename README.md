# BioStudySubmProxy server
A Tomcat-based proxy server for the [BioStudies Submission Tool](https://github.com/EBIBioStudies/BioStudyUISub) that normalises the differences between the server and browser side:

* Makes the separation between temporary and full submissions at the API level transparent to the Submission Tool.
* Converts between REST and non-REST requests when exchanging data with the custom API for BioStudies.

# Getting the proxy up and running locally
Please note that the IDE assumed throughout this document is IntelliJ IDEA v2017. Other IDEs or versions might require slightly different configuration steps.

### Dependencies
- [JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [Tomcat](https://tomcat.apache.org/download-80.cgi)

### 1.Set up Java
Install the Java Development Kit (JDK) for your OS. Once installed, check the following:

- On most platforms, a new `JAVA_HOME` environment variable will be created. If not, the variable has to be [manually exported](http://www.baeldung.com/java-home-on-windows-7-8-10-mac-os-x-linux) since the IDE relies on it to detect your Java installation.
- Set the IDE's JDK path to the newly installed one. On IntelliJ IDEA and other similar IDEs, this can be set globally under the "Default project structure" settings section.

### 2.Configure the project
Once the repository has been cloned locally, proceed to import it into your IDE. With the project imported, do the following:
```
cp gradle_sample.properties gradle.properties
```
This will create a `gradle.properties` file with stub values for some project-wide settings like backend URLs (refer to internal wiki documentation on available Tomcat instances for the latter). Other settings –possibly sensitives ones such as tokens or passwords– may also be saved in this file. 
Some IDEs will automatically detect the presence of the newly created properties file and will ask the user apply or import the changes.

### 3.Build the project
Most IDEs allow running the build task from within the UI. If not, the build can also be run from the console if the corresponding `gradle` command is installed:
```
gradle clean build war
```

In recent versions of the JDK for Mac, an error may crop up during the build process:
`objc[5398]: Class JavaLaunchHelper is implemented in both /Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/bin/java and /Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/jre/lib/libinstrument.dylib. One of the two will be used. Which one is undefined.`

This is [totally benign](http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8022291) and should not affect the running of the project.

Once the build is finished, a .WAR file should have been generated inside the project's `/build/libs` folder.

### 4.Set up Tomcat
Decompress the Tomcat installation files, ideally somewhere under your home directory to avoid further complications with system privileges. Especially on Mac.

On the IDE, add a run configuration for a new non-EE, local Tomcat server. Normally you have to specify the home directory for the server as part of the configuration.

A gradle deployment task should also be added to the run configuration. This entails the selection of a gradle artifact for the project's .WAR file (select the exploded version if available). On ItelliJ Idea, this is done under the "Deployment section". The application's context should be set to "/proxy".

### 5.Add an HTTPS port
By default, the Submission Tool app will use HTTPS in every transaction with the proxy. This requires [generating a new certificate keystore](https://tomcat.apache.org/tomcat-8.0-doc/ssl-howto.html#Prepare_the_Certificate_Keystore), which will be password-protected and saved locally. The keystore should then be declared within a new connector entry in Tomcat's `server.xml` file, located typically under the project's [`/scripts/submtool-ui/roles/deploy/templates/server.xml`](https://github.com/EBIBioStudies/BioStudySubmProxy/blob/master/scripts/submtool-ui/roles/deploy/templates/server.xml):
`
<Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
      maxThreads="150" scheme="https" secure="true"
      clientAuth="false" sslProtocol="TLS"
      keystoreFile="/path/to/keystore"
      keystorePass="keystore_password" />
`
The newly created run configuration for Tomcat should reflect the port being used by the Submission Tool app, typically 10281.

### 6.Run the proxy server
Now a local instance of the proxy server can be run using the run configuration for Tomcat created above. Should errors be listed in Tomcat's logs, check if they are warnings. If so, the proxy will probably be able to run regardless of them.
