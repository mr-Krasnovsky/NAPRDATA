# Этап сборки
FROM maven:3.9.4-eclipse-temurin-17 as build

WORKDIR /app

# Копируем POM и кастомный jar
COPY pom.xml .
COPY libs/jdxf.jar libs/jdxf.jar

# Устанавливаем кастомный JAR в локальный репозиторий
RUN mvn install:install-file \
    -Dfile=libs/jdxf.jar \
    -DgroupId=com.jdxf \
    -DartifactId=jdxf \
    -Dversion=2.1 \
    -Dpackaging=jar

# Кэшируем зависимости
RUN mvn dependency:go-offline

# Копируем исходный код
COPY src ./src

# Собираем fat-jar
RUN mvn clean package -DskipTests

# Этап рантайма
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
