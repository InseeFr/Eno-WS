# Eno-WS : Questionnaire Generator REST Web Service

## Introduction

Eno is a tool that generates survey questionnaires starting from their formal description in [DDI](http://ddialliance.org/).

Due to its modular design, Eno can create questionnaires in different formats from the same DDI description. Currently, Eno generates XForms web questionnaires that can be executed on [Orbeon Forms Runner](http://www.orbeon.com/). PDF questionnaires is under development.

This project uses the prior Eno architecture of folders from the [ENO GitHub Project](https://github.com/InseeFr/Eno) and performs the same actions within a REST Web Service.

## Principles: 
 
The generation of XForms forms is performed using a number of XSLT transformations from a DDI input file that is sent to the main URL of the service.

The main URL to call is **http://localhost:8080/REST_Questionnaire_Generator/Main/Service/Generation** **(TO MODIFY)**
and takes two arguments:

- ***file*** : the input DDI file to be processed, this argument is mandatory.
- ***parameters*** : the parameters.xml file to be used for the process, this argument is optional. If not provided, a default parameters.xml file will be used.

The response element will contain the result of the process, which can be:

-  Success case: The output Xforms file 
-  Error case: The error message

## Getting Started
 
### Prior: 
 
 * Java 8
 * Any compatible container for the WAR file (Tomcat, Glassfish)
 * Source code from github.com
 * Architecture of the prior Eno ANT version : [ENO GitHub Project](https://github.com/InseeFr/Eno)
	 * The root folder of this architecture **must** be overloaded in a property located /src/main/java/fr/insee/utils/**Constants**.java

 
The first build of the project **must** be a maven clean install skipping tests. This will download the pom.xml dependencies to initialize the project: 

* Saxon HE 9.X or higher (The XSLT and XQuery Processor), see also : [Saxon](https://mvnrepository.com/artifact/net.sf.saxon/Saxon-HE)
* [RestAssured](http://rest-assured.io/) : used in the JUnit tests
* [Log4j](http://logging.apache.org/log4j/2.x/) : used to log the service
* All [Jersey](https://jersey.java.net/) related dependencies

After this first build and having the application running on your container, you should be able to perform unit tests by building the project without skipping tests.

### Example : 
 
In the project resources, you can find an example of a questionnaire (specified in the DDI format) named simpsons.xml

This example is used in a RestAssured JUnit test, meaning that the main URL will be called with *file* = simpsons.xml, creating the output form in /target/simpsons/v1/form/form.xhtml

To run this test, simply run a maven clean install without skipping the tests. 

**N.B:** The application must have been previously deployed on your web container(Tomcat for example) in order for this test to run.


### Non regression test: 
 
The expected XForms form file for the Simpsons questionnaire is present in the project resources.

A JUnit test is a non regression test verifying that the output Xforms file created during the simpsons test is exactly the same as the given simpsons-form.

The difference file stored in */target/nonRegressionTest/diff.txt*  specifies the index at which the file begins to differ and the difference between the generated Xform file and the expected one.