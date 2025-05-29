FROM ubuntu:latest AS build
RUN apt-get update
RUN apt-get install openjdk-21-jdk -y
COPY . .
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
EXPOSE 8083
COPY --from=build /build/libs/generator-rpd-0.0.1.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
