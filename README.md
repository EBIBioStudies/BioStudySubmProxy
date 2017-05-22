# BioStudySubmProxy server

The proxy server for BioStudies Submission Tool (UI). 

## Configure the project

Create the configuration file  where you can write sensitive information like: backedn url, tokens, passwords etc. 

```
cp gradle_sample.properties gradle.properties

```

`gradle_sample.properties` file contains an example of properties that have to be set.

## Prebuild the client app (JS, CSS, HTML etc)
Copy the client part to WebContent directory. 

## Build the project

```
gradle clean build war
```