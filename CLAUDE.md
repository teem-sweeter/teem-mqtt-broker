# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Maven multi-module MQTT broker project using Spring Boot 4.0.6. It integrates the Moquette broker engine and provides a Web UI for monitoring and management.

## Build Commands

```bash
# Full build (including npm build for ui-webjar)
mvn clean install

# Build without tests
mvn clean package -DskipTests

# Build only admin module with dependencies
mvn clean install -pl admin -am

# Run the admin application (Spring Boot)
java -jar admin/target/admin-1.0.0.jar
```

## Module Structure

- **mqtt-broker**: Core MQTT broker module using Moquette 0.18.0. Contains `MoquetteAutoConfiguration` for Spring integration, `DefaultMoquetteAuthenticator` for authentication, and handler/persistence classes.
- **admin**: Main Spring Boot application. Depends on `mqtt-broker` and `ui-webjar`. Provides REST API (AuthController, MonitorController, MqttController), WebSocket support, and JWT-based security.
- **ui-webjar**: Frontend UI packaged as WebJars. Built with npm/Node (v20.19.5), outputs to `dist/` directory.
- **maven-jar-sign-plugin**: Custom Maven plugin to sign JAR files with `.bin` extension using RSA keys in `docs/release-private.pem`.

## Key Technologies

- Spring Boot 4.0.6
- Moquette MQTT broker 0.18.0 (via JitPack)
- JWT authentication (jjwt 0.12.6)
- Knife4j OpenAPI 4.5.0 (Swagger UI at `/doc.html`)
- WebSocket for real-time communication
- Lombok for code generation

## Configuration

- MQTT broker properties: `mqtt-broker/src/main/java/com/x/mqtt/MoquetteProperties.java`
- Admin configuration: Spring Boot standard `application.yml` in admin module