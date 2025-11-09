#creating jar
#STAGE 1 : Build the JAR file
FROM maven:3.9-eclipse-temurin-21-alpine as build
ARG APP_ENV=dev
WORKDIR /url-app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

#STAGE 2 : Start the Spring Application
FROM amazoncorretto:21-alpine3.22-jdk

ARG APP_ENV=dev
LABEL build.env=${APP_ENV}

WORKDIR /url-app
COPY --from=build /url-app/target/url-shortener-app.jar .
EXPOSE 8080
ENTRYPOINT [ "java","-jar","url-shortener-app.jar" ]