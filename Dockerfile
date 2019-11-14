FROM tomcat:8.5-jdk11-slim

RUN rm -rf $CATALINA_HOME/webapps/*
ADD src/main/resources/log4j2.xml $CATALINA_HOME/webapps/log4j2.xml
ADD target/*.war $CATALINA_HOME/webapps/ROOT.war
ADD tomcat-users.xml $CATALINA_HOME/conf/tomcat-users.xml