#creating jar
#STAGE 1 : Build the JAR file
FROM maven:3.9-eclipse-temurin-21-alpine as build
WORKDIR /url-app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

#STAGE 2 : Start the Spring Application
FROM amazoncorretto:21-alpine3.22-jdk
WORKDIR /url-app
COPY --from=build /url-app/target/url-shortener-app.jar .
EXPOSE 8080
ENTRYPOINT [ "java","-jar","url-shortener-app.jar" ]