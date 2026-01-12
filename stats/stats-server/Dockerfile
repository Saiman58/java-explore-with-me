#FROM eclipse-temurin:21-jre
#
#WORKDIR /app
#
#COPY target/stats-server-0.0.1-SNAPSHOT.jar app.jar
#
#EXPOSE 9090
#
#ENTRYPOINT ["java","-jar","/app/app.jar"]
# Используем легковесный JDK
FROM eclipse-temurin:21-jdk-alpine

# Копируем скомпилированный JAR
COPY ${JAR_FILE} app.jar

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app.jar"]

