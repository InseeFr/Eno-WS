FROM openjdk:11-jre-slim

ADD ./target/*.jar app.jar

ENTRYPOINT ["java","-jar","./app.jar"]

