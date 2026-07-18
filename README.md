# Sekretess Business App
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

`business-app` is a Spring Boot REST API for businesses that want to send end-to-end encrypted messages and files to consumers through the Sekretess platform.

It is built on top of [`business-java-sdk`](https://github.com/sekretess/business-java-sdk), which provides the Signal-based encryption, session handling, and Sekretess platform integration used by this application.

## What this application does

This service exposes HTTP endpoints for:

- sending encrypted direct messages to a consumer
- sending encrypted files to a consumer
- sending advertisement messages

The app wraps the SDK with a REST interface and persists SDK state locally using Spring Data JPA and H2.

## Architecture

- **Spring Boot API**: exposes the REST endpoints
- **Service layer**: validates requests and delegates to the SDK
- **`business-java-sdk`**: handles encryption, session creation, file encryption, and message delivery
- **JPA + H2**: stores identity keys, consumer sessions, and group session data

## Related project

- [`business-java-sdk`](https://github.com/sekretess/business-java-sdk) - Java SDK used by this service for encryption, session management, and Sekretess API integration

## Requirements

- Java 21
- Maven 3.6+
- Docker (optional, recommended for running the published image)

## Running the application

### Option 1: Run the published image from GitHub Container Registry

This repository publishes container images to GitHub Container Registry:

- `ghcr.io/sekretess/business-app:latest`
- `ghcr.io/sekretess/business-app:<release-tag>`

Pull and run:

```bash
docker pull ghcr.io/sekretess/business-app:latest

docker run --rm -p 8080:8080 \
  -e SEKRETESS_AUTH_MODE=api_key \
  -e BUSINESS_USER_NAME=my-business-id \
  -e SEKRETESS_API_KEY=your-api-key \
  -e SEKRETESS_API_SECRET=your-api-secret \
  -e SEKRETESS_BUSINESS_SERVER_URL=https://business.sekretess.io \
  -e SPRING_DATASOURCE_URL=jdbc:h2:file:/data/h2/sekretess-business-app-db \
  -v sekretess-business-data:/data/h2 \
  ghcr.io/sekretess/business-app:latest
```

The explicit `SPRING_DATASOURCE_URL` above makes the H2 database use the mounted `/data/h2` volume.

`SPRING_DATASOURCE_URL` is **optional**. If you do not set it, the application uses the default H2 path `jdbc:h2:file:./sekretess-business-app-db`. Set it when you want the database file to live in a mounted Docker volume or in another custom location.

### Option 2: Run from source with Maven

Build:

```bash
mvn clean package
```

Run:

```bash
java -jar target/*.jar
```

Or run directly with Maven:

```bash
mvn spring-boot:run
```

### Option 3: Build the Docker image locally

```bash
mvn clean package
docker build -t sekretess-business-app .

docker run --rm -p 8080:8080 \
  -e SEKRETESS_AUTH_MODE=api_key \
  -e BUSINESS_USER_NAME=my-business-id \
  -e SEKRETESS_API_KEY=your-api-key \
  -e SEKRETESS_API_SECRET=your-api-secret \
  -e SEKRETESS_BUSINESS_SERVER_URL=https://business.sekretess.io \
  -e SPRING_DATASOURCE_URL=jdbc:h2:file:/data/h2/sekretess-business-app-db \
  -v sekretess-business-data:/data/h2 \
  sekretess-business-app
```

## Configuration

The application uses `application.yml` for Spring and database settings. The Sekretess SDK reads its own settings directly from process environment variables.

### Application and storage

| Variable | Required | Default | Purpose |
| --- | --- | --- | --- |
| `SPRING_DATASOURCE_URL` | No | `jdbc:h2:file:./sekretess-business-app-db` | Optional H2 database location override; recommended for Docker volume persistence |

### SDK authentication

This app depends on `business-java-sdk`, so authentication is configured with SDK environment variables that must be set in the container or shell.

#### API key mode

```bash
export SEKRETESS_AUTH_MODE=api_key
export BUSINESS_USER_NAME=my-business-id
export SEKRETESS_API_KEY=your-api-key
export SEKRETESS_API_SECRET=your-api-secret
export SEKRETESS_BUSINESS_SERVER_URL=https://business.sekretess.io
```

#### Mutual TLS mode

```bash
export SEKRETESS_AUTH_MODE=mtls
export BUSINESS_USER_NAME=my-business-id
export SEKRETESS_BUSINESS_SERVER_URL=https://business.sekretess.io
export IDENTITY_PROVIDER_URL=https://auth.sekretess.net/realms/business/protocol/openid-connect/token
export USER_CERTIFICATE_PATH=/path/to/client.crt
export USER_CERTIFICATE_KEY=/path/to/client.key
export USER_CERTIFICATE_PASSWORD=your-key-password
```

For the full SDK authentication and integration details, see the [`business-java-sdk` README](https://github.com/sekretess/business-java-sdk).

## API

Base path: `/api/v1/business`

### Send a private message

**Endpoint**

```http
POST /api/v1/business/messages
Content-Type: application/json
```

**Request body**

```json
{
  "text": "You have an important update in your account",
  "consumer": "receiver-name"
}
```

**Example**

```bash
curl -X POST http://localhost:8080/api/v1/business/messages \
  -H "Content-Type: application/json" \
  -d '{"text":"You have an important update in your account","consumer":"receiver-name"}'
```

### Send an encrypted file

**Endpoint**

```http
POST /api/v1/business/messages/files
Content-Type: multipart/form-data
```

**Example**

```bash
curl -X POST http://localhost:8080/api/v1/business/messages/files \
  -F "file=@/path/to/document.pdf" \
  -F "consumer=receiver-name"
```

### Send an advertisement message

**Endpoint**

```http
POST /api/v1/business/ads/messages
Content-Type: application/json
```

**Request body**

```json
{
  "text": "Special offer for all subscribers"
}
```

**Example**

```bash
curl -X POST http://localhost:8080/api/v1/business/ads/messages \
  -H "Content-Type: application/json" \
  -d '{"text":"Special offer for all subscribers"}'
```

Successful requests return HTTP `202 Accepted`.

## Persistence

By default, the application uses a file-based H2 database and persists:

- business identity key material
- consumer session records
- group session data used for advertisement messages

For containerized usage, mount a volume and set `SPRING_DATASOURCE_URL` to a path under `/data/h2`.

## Testing

Run the test suite with:

```bash
mvn test
```

## Contributing

- Follow the existing project conventions
- Add or update tests when changing application behavior
- Open a clear pull request or issue with reproduction steps and context

## License

See the [LICENSE](LICENSE) file for details.
