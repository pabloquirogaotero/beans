# Build stage: compile and package with the project's own Maven wrapper.
FROM eclipse-temurin:21.0.13_9-jdk-noble AS build
WORKDIR /workspace

# Dependencies first, in their own layer, so a source change does not download them again.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q package -DskipTests

# Run stage: only a JRE and the jar, running as an unprivileged user.
FROM eclipse-temurin:21.0.13_9-jre-noble
WORKDIR /app
RUN useradd --system --uid 10001 beans
COPY --from=build /workspace/target/beans-*.jar app.jar
USER beans
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
