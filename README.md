#BioStudySubmProxy server

BioStudy Submission Proxy is a tool 

##### Configure the project

Create the configuration directory and file  where you can write sensitive information like: backedn url, tokens, passwords etc. 

	`mkdir .bsconfig`
	`mkdir WebContent`
	`touch .bsconfig/prod.properties`
	`touch .bsconfig/test.properties`

The template configuration file can be found in the example directory.

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


