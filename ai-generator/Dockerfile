FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -pl ai-generator -am -DskipTests package

FROM alpine:3.20
WORKDIR /artifact
COPY --from=build /workspace/ai-generator/target/*.jar ./ai-generator.jar
CMD ["sh", "-c", "ls -lh /artifact && echo ai-generator artifact is ready"]
