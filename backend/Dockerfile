FROM openjdk:8-jdk-alpine as build

WORKDIR /app

RUN pwd

# Only copy dependency-related files
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

# Only download dependencies
# Eat the expected build failure since no source code has been copied yet
RUN ./gradlew clean build --no-daemon > /dev/null 2>&1 || true


COPY src src

RUN ./gradlew bootJar -i --stacktrace --no-daemon



FROM openjdk:8-alpine

ENV ARTIFACT_NAME=BlokAtUGent-0.2.jar

WORKDIR /app
COPY --from=build /app/build/libs/$ARTIFACT_NAME .
COPY agent agent

EXPOSE 8080
ENTRYPOINT java -Dskywalking.agent.service_name=gateway -javaagent:/app/agent/skywalking-agent.jar -jar $ARTIFACT_NAME
