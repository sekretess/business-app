# Business application to send encrypted messages
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## Table of contents
- Overview
- Features
- Architecture
- Prerequisites
- Build & run
- Configuration
- REST API (examples)
- Security & encryption
- Testing
- Docker
- Contributing
- License

## Overview
This Spring Boot application provides a secure backend to send, store and retrieve encrypted messages for business workflows. It exposes a REST API to encrypt messages for recipients and to decrypt messages when permitted. The project uses Java and Maven.

## Features
- RESTful API for sending and retrieving messages
- Message encryption using configurable algorithms (symmetric and/or asymmetric)
- Secure key management configuration
- Persistence via a simple repository (JPA)
- Unit and integration test setup
- Ready for containerized deployment

## Architecture
- Spring Boot application (controller -> service -> repository)
- Encryption handled in a dedicated service/component
- Persistence via Spring Data JPA (configurable RDBMS)
- Configuration via `application.properties` or environment variables

## Prerequisites
- JDK 17+ (or the version configured in `pom.xml`)
- Maven 3.6+
- Optional: Docker (for container builds)

## Build & run
Build:

    mvn clean package -DskipTests

Run locally:

    java -jar target/*.jar

Run with Maven (dev):

    mvn spring-boot:run

## Configuration
Key runtime properties are read from `application.properties` or environment variables. Example keys (adjust names used in the project):

- `server.port` - HTTP port
- `spring.datasource.*` - datasource configuration

Set sensitive values using environment variables or an external secrets manager; do not commit keys to source control.

## REST API (examples)
Base path: `/api/messages` (adjust if different in code)

Send message (encrypt for recipient):
- POST `/api/messages`
- Body (example JSON):

  {
  "recipientId": "user-123",
  "payload": "Hello, confidential data"
  }

Response (example):

    {
        "id": "msg-456",
        "encrypted": true,
        "createdAt": "2025-01-01T12:00:00Z"
    }

Retrieve message (decrypted if requester authorized):
- GET `/api/messages/{id}`

Example curl (replace host/port accordingly):

    curl -X POST http://localhost:8080/api/messages \
      -H "Content-Type: application/json" \
      -d '{"recipientId":"user-123","payload":"Secret"}'

Authorization headers (JWT/API key) should be used if the project implements auth.

## Security & encryption
- Encryption algorithms and key lengths must follow organizational security standards.
- Prefer storing private keys in dedicated secret stores or HSMs.
- Use TLS for all network traffic (HTTPS).
- Ensure proper access control on endpoints that return decrypted content.
- Rotate keys regularly and provide migration procedures.

## Testing
Run unit and integration tests:

    mvn test

Consider using profiles for integration tests that require a real database or key material.

## Docker
Example build and run (if `Dockerfile` exists):

Build:

    mvn package -DskipTests
    docker build -t encrypted-messages-app .

Run:

    docker run -e SPRING_PROFILES_ACTIVE=prod -p 8080:8080 encrypted-messages-app

## Contributing
- Follow the project's code style and commit message conventions.
- Add tests for new features or bug fixes.
- Open an issue / pull request with a clear description and test steps.

## License
GNU General Public License v3.0 (GPL-3.0) or later. See `LICENSE` file for details.

---
