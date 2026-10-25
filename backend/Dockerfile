FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src src
RUN mvn -B -ntp package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S ledgerflow && adduser -S ledgerflow -G ledgerflow
WORKDIR /app
COPY --from=build --chown=ledgerflow:ledgerflow /build/target/ledgerflow-1.0.0.jar app.jar
USER ledgerflow
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
