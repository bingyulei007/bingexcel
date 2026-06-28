# Multi-stage build: build jar with maven image, then run on slim JRE 17 image
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copy excel module first (demo depends on it)
COPY excel/pom.xml /build/excel/pom.xml
COPY excel/src /build/excel/src
COPY demo/pom.xml /build/demo/pom.xml
COPY demo/src /build/demo/src

# Install excel module to local repo, then build demo
RUN mvn -B -q -DskipTests -f /build/excel/pom.xml install
RUN mvn -B -q -DskipTests -Dmaven.test.skip=true -f /build/demo/pom.xml package

# --- Runtime stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /build/demo/target/*-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC -Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
