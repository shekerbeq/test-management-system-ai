FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -pl core -am -DskipTests package

FROM alpine:3.20
WORKDIR /artifact
COPY --from=build /workspace/core/target/*.jar ./core.jar
CMD ["sh", "-c", "ls -lh /artifact && echo core module artifact is ready"]
