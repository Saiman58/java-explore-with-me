# Этап сборки
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Копируем ВЕСЬ проект
COPY . .

# Собираем только stats-server модуль
RUN mvn clean package -DskipTests -pl stats/stats-server -am

# Финальный образ
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/stats/stats-server/target/*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]