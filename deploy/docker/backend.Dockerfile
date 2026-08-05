FROM eclipse-temurin:21-jre

WORKDIR /app
ARG JAR_FILE
COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=55 -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
