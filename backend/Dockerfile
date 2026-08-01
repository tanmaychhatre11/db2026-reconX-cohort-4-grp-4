# ============================================================================
# TICKET-ADV146 — Spring Boot Dockerfile (multi-stage Maven -> JRE 25 slim)
#
# Stage 1: build the JAR with Temurin JDK 25 + Maven.
# Stage 2: copy ONLY the JAR onto a slim JRE image.
# ============================================================================

FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

# Parent project
COPY pom.xml .

# Maven wrapper
COPY recon-service/mvnw .
COPY recon-service/.mvn .mvn
RUN chmod +x mvnw

# Copy both modules
COPY recon-service recon-service
COPY recon-audit-starter recon-audit-starter

# Build the entire reactor
RUN ./mvnw -q -B -pl recon-service -am -DskipTests package

FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S reconx && adduser -S reconx -G reconx

USER reconx
WORKDIR /app

COPY --from=build /workspace/recon-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]