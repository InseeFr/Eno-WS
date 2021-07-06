FROM openjdk:11-jdk-slim

ADD src/main/resources/log4j2.xml log4j2.xml
ADD src/main/resources/enows-server.properties enows.properties
COPY ./target/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar","--spring.config.location=file:///enows.properties"]

