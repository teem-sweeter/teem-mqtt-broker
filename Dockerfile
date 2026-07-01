# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY mqtt-broker/pom.xml mqtt-broker/
COPY admin/pom.xml admin/
COPY ui-webjar/pom.xml ui-webjar/
COPY maven-jar-sign-plugin/pom.xml maven-jar-sign-plugin/
RUN mvn dependency:go-offline -B || true
COPY . .
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/admin/target/admin-1.0.0.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV JWT_SECRET=""
ENV MQTT_BROKER_PASSWORD=""

EXPOSE 8080 1883 8083

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
