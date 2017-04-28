# Setting up a VM with BioStudies DB Backend application #

Here are the scripts which install a VM with [BioStudies DB Backend](https://github.com/EBIBioStudies/BackendWebApp) 

## Prerequisites ##
* VirtualBox (>= v5.1.18)
* Vagrant (>= v1.9.1)
* Ansible (>= v2.2.1.0)

*Note: The code was tested on Mac OS X 10.11.6*

*Note: There is an issue with mounting shared folders in Virtualbox 5.1.20. You can find more info on
[StackOverflow](http://stackoverflow.com/questions/43492322/vagrant-was-unable-to-mount-virtualbox-shared-folders)*

## Creating a VM ##

`./Vagrantfile` contains all VM specific settings (static IP, private network, memory size etc.). Feel free to 
customize it for your own purposes.

Next command creates a VM and does initial provisioning (by installing python libs and creating 'ansible' user) 

````sh
$ vagrant up 
````

If any errors happen during the VM start you can always re-run provisioning after the problem is resolved

````sh
$ vagrant provision
````

*Note: You now should be able to login to the VM by ssh without entering an password*
````sh
$ ssh -i ./data/id_rsa_ansible ansible@192.168.33.15
````

## Installing required software ##

BioStudies DB backend application depends on the following 3d party libraries, which unfortunately you'll have to download yourself 
and put jar files in the `./libs` folder:
* Smart GWT 5.0 (smartgwt.jar) LGPL edition; it can be found on [www.smartclient.com](http://www.smartclient.com/product/downloadOtherReleases.jsp)
* GWT Uploader 1.1.0 (org.moxieapps.gwt.uploader-1.1.0.jar); it can be found on [www.moxiegroup.com](http://www.moxiegroup.com/moxieapps/gwt-uploader/userguide.jsp) 
* MySQL java connector (mysql-connector-java-5.1.42-bin.jar); it can be found on [dev.mysql.com](https://dev.mysql.com/downloads/connector/j/5.1.html)

When everything is downloaded libs folder should look like this:
````
libs/
 |
 |--mysql-connector-java-5.1.42-bin.jar
 |--org.moxieapps.gwt.uploader-1.1.0.jar
 |--smartgwt.jar

````

These libraries are described in the `./roles/vars.yml` file. Feel free to update it If you have different jar versions:
 ````yaml
 smartgwt:
   jar: "smartgwt.jar"
   groupId: "com.isomorphic.smartgwt.lgpl"
   artifactId: "smartgwt-lgpl"
   version: "5.0-p20170421"
 
 gwt_uploader:
   jar: "org.moxieapps.gwt.uploader-1.1.0.jar"
   groupId: "org.moxieapps.gwt"
   artifactId: "org.moxieapps.gwt.uploader"
   version: "1.1.0"
 
 mysql_driver:
   jar: "mysql-connector-java-5.1.42-bin.jar"
 ````
 
When the libraries are ready you can run `env.yml` playbook to setup the environment: software which are required to build 
and deploy BioStudies DB application.
````sh
$ ansible-playbook -i ./hosts env.yml
````

## Building BioStudies DB ##

To (re)build BioStudies DB application from sources run `build.yml` playbook: 

````sh
$ ansible-playbook -i ./hosts build.yml
````

*Note: This script creates a war which can be found in the `/home/ansible/backend/dist` folder on the VM*  


## Deploying BioStudies DB ##

Before the deployment you'll need to create and edit `./roles/deploy/templates/config.properties` file:
````sh
$ cd ./roles/deploy/templates
$ cp config.example.properties config.properties
````

and when the config is ready you can run `deploy.yml` playbook:
````sh
$ ansible-playbook -i ./hosts deploy.yml
````

If deployment was successful you should be able to see login form which doesn't return 500 error on login: 
[http://192.168.33.15:8080/biostd-loc](http://192.168.33.15:8080/biostd-loc)

To see the application log files you can login to the VM and see the log files:
````sh
$ ssh ansible@192.168.33.15
Welcome to your Vagrant-built virtual machine.
Last login: Fri Apr 28 15:38:42 2017 from 192.168.33.1
ansible@precise64:~$ sudo ls -la /opt/apache-tomcat-8.5.14/logs
total 44
drwxr-x---  2 tomcat root    4096 Apr 28 14:14 .
drwxr-xr-x 11 tomcat tomcat  4096 Apr 28 14:14 ..
-rw-r-----  1 tomcat tomcat 26168 Apr 28 15:39 catalina.2017-04-28.log
-rw-r-----  1 tomcat tomcat     0 Apr 28 14:14 host-manager.2017-04-28.log
-rw-r-----  1 tomcat tomcat   836 Apr 28 15:39 localhost.2017-04-28.log
-rw-r-----  1 tomcat tomcat  1147 Apr 28 15:40 localhost_access_log.2017-04-28.txt
-rw-r-----  1 tomcat tomcat     0 Apr 28 14:14 manager.2017-04-28.log

```
