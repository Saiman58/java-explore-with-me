FROM eclipse-temurin:21-jdk

# Аргумент для версии JAR (по умолчанию SNAPSHOT)
ARG JAR_FILE=ewm-main-server-0.0.1-SNAPSHOT.jar

# Рабочая директория
WORKDIR /app

# Копируем JAR
COPY . .

RUN mvn clean package -DskipTests -pl ewm-main/server -am

# Финальный образ
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]