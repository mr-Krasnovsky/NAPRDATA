# Используем официальный образ Maven для сборки
FROM maven:3.9.4-eclipse-temurin-17 as build

# Рабочая директория внутри контейнера
WORKDIR /app

# Копируем pom.xml и скачиваем зависимости (кэшируем слои)
COPY pom.xml .

RUN mvn dependency:go-offline

# Копируем весь проект
COPY src ./src

# Собираем fat-jar с помощью maven-shade-plugin
RUN mvn clean package -DskipTests

# Второй этап: запускаем приложение в легковесном образе OpenJDK
FROM eclipse-temurin:17-jre-alpine

# Рабочая директория
WORKDIR /app

# Копируем собранный jar из build-этапа
COPY --from=build /app/target/*.jar app.jar

# Пробрасываем порт (если нужно, например для web-сервиса)
EXPOSE 8080

# Команда запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
