# syntax=docker/dockerfile:1
FROM gradle:8.14-jdk21 AS build
WORKDIR /src
COPY --chown=gradle:gradle settings.gradle.kts build.gradle.kts ./
COPY --chown=gradle:gradle src ./src
RUN gradle --no-daemon bootJar

FROM eclipse-temurin:21-jre-jammy
RUN groupadd --system app && useradd --system --gid app --home /app app
WORKDIR /app
COPY --from=build --chown=app:app /src/build/libs/*.jar /app/playwright.jar
USER app
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+UseContainerSupport"
ENTRYPOINT ["java","-jar","/app/playwright.jar"]
