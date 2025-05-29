FROM gradle:8.14.0 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build

FROM openjdk:21-jre-slim
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/generator-rpd-0.0.1.jar /app/
RUN bash -c 'touch /app/generator-rpd-0.0.1.jar'
ENTRYPOINT ["java", "-jar", "/app/generator-rpd-0.0.1.jar"]
