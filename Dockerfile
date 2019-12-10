FROM tomcat:jdk8-openjdk-slim

RUN rm -rf $CATALINA_HOME/webapps/*
ADD src/main/resources/log4j2.xml $CATALINA_HOME/webapps/log4j2.xml
ADD src/main/resources/enows-dev.properties $CATALINA_HOME/webapps/enows.properties
ADD ./target/*.war $CATALINA_HOME/webapps/ROOT.war