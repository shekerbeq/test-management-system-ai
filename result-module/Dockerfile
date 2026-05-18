FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -pl result-module -am -DskipTests package

FROM alpine:3.20
WORKDIR /artifact
COPY --from=build /workspace/result-module/target/*.jar ./result-module.jar
CMD ["sh", "-c", "ls -lh /artifact && echo result-module artifact is ready"]
