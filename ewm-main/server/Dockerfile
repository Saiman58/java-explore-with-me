FROM eclipse-temurin:21-jdk

# Аргумент для версии JAR (по умолчанию SNAPSHOT)
ARG JAR_FILE=ewm-main-server-0.0.1-SNAPSHOT.jar
FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]