#Maven Build
FROM maven:3.9.7-eclipse-temurin-22 AS builder
COPY pom.xml /app/
COPY src /app/src
RUN mvn -f /app/pom.xml clean package -DskipTests

#Run
FROM eclipse-temurin:22-jdk
COPY --from=builder /app/target/monitor-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
