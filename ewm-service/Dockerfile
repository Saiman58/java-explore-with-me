
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Копируем pom.xml и исходники
COPY pom.xml .
COPY src ./src

# Сборка jar (skip тесты)
RUN mvn clean package -DskipTests

# Финальный образ: только JDK
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Копируем jar из сборочного этапа
COPY --from=build /app/target/ewm-service-0.0.1-SNAPSHOT.jar ewm-service.jar

# Открываем порт
EXPOSE 8080

# Команда запуска
ENTRYPOINT ["java", "-jar", "ewm-service.jar"]