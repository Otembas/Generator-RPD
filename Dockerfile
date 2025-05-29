ARG GRADLE_IMAGE=gradle:8.14.0-jdk21
FROM ${GRADLE_IMAGE} as gradle-cache
ENV BUILD_SRC /usr/src/generator
ENV GRADLE_USER_HOME /home/gradle/cache
RUN mkdir -p ${BUILD_SRC}
RUN mkdir -p ${GRADLE_USER_HOME}
COPY gradle ${BUILD_SRC}gradle
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ${BUILD_SRC}
WORKDIR ${BUILD_SRC}
RUN gradle build -i --build-cache || return 0 && echo "Error is ok at 1st stage"

FROM gradle-cache as builder
ENV BUILD_SRC /usr/src/generator
COPY . ${BUILD_SRC}
WORKDIR ${BUILD_SRC}
RUN gradle bootJar -i --stacktrace --build-cache

ENV EXTRACTED /opt/generator
RUN mkdir -p ${EXTRACTED} && (cd ${EXTRACTED}; java -Djarmode=layertools -jar ${BUILD_SRC}build/libs/generator-*.jar extract)
RUN du -hs ${EXTRACTED}/*

ARG JAVA_IMAGE=eclipse-temurin:21-jre
FROM ${JAVA_IMAGE}
ENV EXTRACTED /opt/generator
USER root
WORKDIR /opt/generator
COPY --from=builder ${EXTRACTED}/dependencies/ ./
COPY --from=builder ${EXTRACTED}/spring-boot-loader/ ./
COPY --from=builder ${EXTRACTED}/snapshot-dependencies/ ./
COPY --from=builder ${EXTRACTED}/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
