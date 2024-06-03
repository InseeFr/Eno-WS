FROM eclipse-temurin:17-jre

WORKDIR /opt/eno-ws/
COPY ./target/*.jar /opt/eno-ws/eno-ws.jar
ENTRYPOINT ["java", "-jar",  "/opt/eno-ws/eno-ws.jar"]