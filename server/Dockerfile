# server/Dockerfile
# ---- Build ----
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app
COPY . .

RUN gradle clean bootJar --no-daemon

# ---- Run ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy the fat JAR from the build stage (adjust path if your jar name differs)
COPY --from=build /app/build/libs/*-SNAPSHOT.jar /app/app.jar

# Optional: remote debug - uncomment to enable and map port 5005 in compose
# ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
