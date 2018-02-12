# Eno-WS : Questionnaire Generator REST Web Service


## Introduction

Eno is a tool that generates survey questionnaires starting from their formal description in [DDI](http://ddialliance.org/).

Due to its modular design, Eno can create questionnaires in different formats from the same DDI description. Currently, Eno generates XForms web questionnaires that can be executed on [Orbeon Forms Runner](http://www.orbeon.com/). PDF questionnaires is under development.

This project uses the prior Eno architecture of folders from the [ENO GitHub Project](https://github.com/InseeFr/Eno) v1.0.0 and performs the same actions within a REST Web Service.

## Principles: 
 
The generation of XForms forms is performed using a number of XSLT transformations from a DDI input file that is sent to the main URL of the service.

The main URL to call is **http://localhost:8080/api/api/eno**
and takes one arguments in the body :

- ***ddi*** : the input DDI description to be processed.

The response element will contain the result of the process, which can be:

-  Success case: The output Xforms result 
-  Error case: The error message


## Getting Started


### From war file : 
 
 * Java 8
 * Any compatible container for the WAR file (Tomcat, Glassfish)
 * War file from last release [ENO-WS last Release on GitHub](https://github.com/InseeFr/Eno-WS/releases/tag/v1.0.0)
 

### From code source : 

A dependency to eno-core is required but not satisfied via central nor a proxy at the moment.

Subsequently, those additional steps are required in order to build:

```bash
git pull https://github.com/InseeFr/Eno.git 
pushd Eno
mvn install && mvn install -DskipTests && mvn install:install-file -Dfile=target/eno-core-1.0.0.jar -DgroupId=fr.insee -DartifactId=eno-core -Dversion=1.0.0 -Dpackaging=jar
popd

```  
 
The first build of the project **must** be a maven clean install skipping tests. This will download the pom.xml dependencies to initialize the project: 

* Saxon HE 9.X or higher (The XSLT and XQuery Processor), see also : [Saxon](https://mvnrepository.com/artifact/net.sf.saxon/Saxon-HE)
* [RestAssured](http://rest-assured.io/) : used in the JUnit tests
* [Log4j](http://logging.apache.org/log4j/2.x/) : used to log the service
	* The log directory has to be defined in log4j.properties in src/main/resources.
* All [Jersey](https://jersey.java.net/) related dependencies

After this first build and having the application running on your container, you should be able to perform unit tests by building the project without skipping tests.

### Usage : 

The main URL to call is **http://localhost:8080/api/api/eno**
and takes one arguments in the body :

- ***ddi*** : the input DDI description to be processed.

The response element will contain the result of the process, which can be:

-  Success case: The output Xforms result 
-  Error case: The error message

```curl -X POST "http://localhost:8080/api/api/eno" -H "accept: application/xml" -H "content-type: application/xml" -d "<?xml version=\"1.0\" encoding=\"UTF-8\"?><DDIInstance .... </DDIInstance>"```


### Swagger UI : 

The main URL to call is **http://localhost:8080/swagger-ui/dist.index.html**


### Example : 
 
In the Eno project resources, you can find an example of a questionnaire (specified in the DDI format) named simpsons.xml


DDI example : [DDI Simpsons Questionnaire](https://github.com/InseeFr/Eno/blob/master/questionnaires/simpsons/ddi/simpsons.xml)
XForms expected : [XForms Simpsons Questionnaire] (https://github.com/InseeFr/Eno/blob/master/questionnaires/simpsons/xforms/v1/simpsons-form.xhtml)

