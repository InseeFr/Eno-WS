FROM eclipse-temurin:25.0.4_7-jre

WORKDIR /opt/eno-ws/
COPY ./eno-ws/target/*.jar /opt/eno-ws/eno-ws.jar

ENV JAVA_TOOL_OPTIONS_DEFAULT \
    -XX:MaxRAMPercentage=75 \
    -XX:+UseZGC

ENV JAVA_USER_ID=10001
ENV JAVA_USER=java
RUN groupadd -g "$JAVA_USER_ID" "$JAVA_USER" && \
    useradd -r -l -u "$JAVA_USER_ID" -g "$JAVA_USER" "$JAVA_USER"

USER $JAVA_USER_ID

CMD ["java", \
    "-XX:MaxRAMPercentage=75", \
    "-XX:+UseZGC", \
    "-jar", \
    "/opt/eno-ws/eno-ws.jar"]
