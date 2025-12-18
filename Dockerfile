# Stage 1: Build the Spring Boot JAR
#FROM maven:3.8.3-openjdk-11 AS build
# Senin projen Java 17 olduğu için 'openjdk-17' kullanan Maven imajı seçtik
FROM maven:3.8.5-openjdk-17 AS build

WORKDIR /app

# Copy the project's POM file and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

ARG sonarProjectKey='simple-java'
ARG sonarLoginToken='sqp_18edf432e534ae4652cf09a17c6bbca952ae901d'
ARG sonarHostUrl='http://192.168.1.2:9000'
# Copy the source code and build the JAR
COPY src /app/src
RUN mvn clean verify sonar:sonar -Dsonar.projectKey=${sonarProjectKey} -Dsonar.host.url=${sonarHostUrl} -Dsonar.login=${sonarLoginToken} -DskipTests
# Testleri atlayarak paketi oluşturuyoruz (Zaten Jenkins'te test ettik)
RUN mvn clean package -DskipTests

# Stage 2: Create the final image with the built JAR
#FROM openjdk:11-jre-slim
# Yine Java 17 imajı kullanıyoruz (Çok önemli!)
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar

# Expose the port that the Spring Boot app will run on
EXPOSE 8080

# Specify the command to run the Spring Boot application
CMD ["java", "-jar", "app.jar"]