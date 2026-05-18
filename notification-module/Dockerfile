FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -pl notification-module -am -DskipTests package

FROM alpine:3.20
WORKDIR /artifact
COPY --from=build /workspace/notification-module/target/*.jar ./notification-module.jar
CMD ["sh", "-c", "ls -lh /artifact && echo notification-module artifact is ready"]
