FROM tomcat:9-jre17
# Note: Tomcat 10 is not compatible with Spring Boot 2!!

RUN rm -rf $CATALINA_HOME/webapps/*
ADD src/main/resources/log4j2.xml $CATALINA_HOME/webapps/log4j2.xml
ADD src/main/resources/enows-server.properties $CATALINA_HOME/webapps/enows.properties
ADD ./target/*.war $CATALINA_HOME/webapps/ROOT.war