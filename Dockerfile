FROM tomcat:jre11-slim

RUN rm -rf $CATALINA_HOME/webapps/*
ADD src/main/resources/log4j2.xml $CATALINA_HOME/webapps/log4j2.xml
ADD src/main/resources/enows-server.properties $CATALINA_HOME/webapps/enows.properties
ADD ./target/*.war $CATALINA_HOME/webapps/ROOT.war