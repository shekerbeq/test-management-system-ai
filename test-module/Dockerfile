FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -pl test-module -am -DskipTests package

FROM alpine:3.20
WORKDIR /artifact
COPY --from=build /workspace/test-module/target/*.jar ./test-module.jar
CMD ["sh", "-c", "ls -lh /artifact && echo test-module artifact is ready"]
