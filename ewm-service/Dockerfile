# Этап сборки: Maven + JDK
FROM maven:3.9.1-eclipse-temurin-17 AS build

WORKDIR /app

# Копируем pom.xml и исходники
COPY pom.xml .
COPY src ./src

# Сборка jar (skip тесты)
RUN mvn clean package -DskipTests

# Финальный образ: только JDK
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Копируем jar из сборочного этапа
COPY --from=build /app/target/ewm-service-0.0.1-SNAPSHOT.jar ewm-service.jar

# Открываем порт
EXPOSE 8080

# Команда запуска
ENTRYPOINT ["java", "-jar", "ewm-service.jar"]
