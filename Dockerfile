FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -B dependency:go-offline
COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B package -DskipTests

FROM eclipse-temurin:25-jre
RUN useradd --system --create-home app
WORKDIR /app
COPY --from=build /workspace/target/warranty-app-*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
