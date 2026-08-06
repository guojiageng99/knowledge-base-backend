FROM eclipse-temurin:21-jre

WORKDIR /app
COPY fonts/NotoSansSC-Variable.ttf /usr/share/fonts/truetype/noto/NotoSansSC-Variable.ttf
ARG JAR_FILE
COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=55 -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
