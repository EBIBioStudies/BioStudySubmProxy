#BioStudySubmProxy server

A proxy server for BioStudies Submission Tool. 

##### Configure the project

Create the configuration directory and file  where you can write sensitive information like: backedn url, tokens, passwords etc. 

```
mkdir .bsconfig
mkdir WebContent
touch .bsconfig/prod.properties
touch .bsconfig/test.properties
```

`gradle_sample.properties` file contains an example of properties that have to be set.

You can also create just `gradle.properties` in the project root and use it without specifying -Penv=... in the command line.

##### Prebuild the client app (JS, CSS, HTML etc)
Copy the client part to WebContent directory. 

##### Build the project
Production environment

```
gradle clean build -Penv=prod
```

Test environment

```
gradle clean build -Penv=test
```


