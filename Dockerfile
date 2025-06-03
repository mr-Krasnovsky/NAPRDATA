# Этап сборки
FROM maven:3.9.4-eclipse-temurin-17 as build

WORKDIR /app

# Копируем pom и кастомный jar
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

# Собираем fat-jar с зависимостями (убер-джар)
RUN mvn clean package -DskipTests

# Этап рантайма
# Используем более полный JRE без alpine (для надежности)
FROM eclipse-temurin:17-jre

WORKDIR /app

# Копируем точно имя jar, чтобы избежать проблем с подстановкой *
COPY --from=build /app/target/NaprData-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
