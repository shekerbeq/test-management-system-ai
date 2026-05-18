FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -pl user-module -am -DskipTests package

FROM alpine:3.20
WORKDIR /artifact
COPY --from=build /workspace/user-module/target/*.jar ./user-module.jar
CMD ["sh", "-c", "ls -lh /artifact && echo user-module artifact is ready"]
