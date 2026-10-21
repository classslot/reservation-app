# Render などへのデプロイ用（Gradle Wrapper 不要）
FROM gradle:8.10-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Tokyo"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
