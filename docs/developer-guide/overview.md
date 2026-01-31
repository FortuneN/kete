# Developer Guide

Technical documentation for developers who want to understand KETE internals, extend functionality, or contribute to the project.

---

## Quick Start

```
Keycloak Event → Route → Matcher → Serializer → Destination
                           ↓           ↓            ↓
                       Filter?      Format       Send
```

**Core components:**

- **Routes** — Named pipelines connecting matchers → serializers → destinations
- **Matchers** — Filter events by type (list, glob, regex, SQL)
- **Serializers** — Convert events to bytes (JSON, XML, YAML, CBOR, CSV, TOML, Smile, Properties)
- **Destinations** — Send bytes to external systems (Kafka, RabbitMQ, MQTT, AMQP 1.0, HTTP)

---

## Documentation Map

###  Architecture & Design

| Document | Description |
|----------|-------------|
| [Architecture](architecture.md) | System design, event flow, threading model, destination pooling |
| [Component Scopes](component-scopes.md) | Dependency injection, SINGLETON vs TRANSIENT |
| [Transaction Support](transaction-support.md) | Keycloak transaction integration, commit/rollback handling |

### ⚙ Configuration

| Document | Description |
|----------|-------------|
| [Configuration](configuration.md) | All configuration properties reference |
| [Certificate Loaders](certificate-loaders.md) | TLS/SSL certificate loading (PKCS12, PEM, JKS, DER) |
| [OAuth](oauth.md) | OAuth 2.0 authentication for HTTP destinations |

###  Extending KETE

| Document | Description |
|----------|-------------|
| [Extending](extending.md) | Add custom destinations, serializers, matchers, certificate loaders |
| [Libraries](libraries.md) | Third-party dependencies and versions |

###  Testing

| Document | Description |
|----------|-------------|
| [Testing](testing.md) | Test types, running tests, test environment |
| [Integration Tests](integration-tests.md) | Testcontainers setup, debugging |
| [Test Patterns](test-patterns-and-conventions.md) | Code conventions, AAA pattern, file organization |

###  Development

| Document | Description |
|----------|-------------|
| [Development](development.md) | Project structure, building, IDE setup |
| [Maven Plugins](maven-plugins.md) | Build plugins configuration |

###  Deployment

| Document | Description |
|----------|-------------|
| [Dockerfile Reference](dockerfile-reference.md) | Multi-stage Docker build |
| [Docker Compose Reference](docker-compose-reference.md) | Infrastructure services |
| [Scripts](scripts/overview.md) | Build and test utilities |

###  Roadmap

| Document | Description |
|----------|-------------|
| [Future Enhancements](future-enhancements.md) | Planned features and destinations |

---

## Key Design Decisions

### Destination Pooling

All destinations use **Apache Commons Pool2** for connection management. This ensures:

- Virtual thread compatibility (no ThreadLocal issues)
- Predictable resource usage with configurable pool sizes
- Consistent behavior across all destination types

See [Architecture → Destination Pooling](architecture.md#4-destination-pooling) for details.

### Transaction Safety

Events are only published **after Keycloak transactions commit**. Rolled-back transactions discard events, ensuring external systems stay consistent with Keycloak's database.

See [Transaction Support](transaction-support.md) for details.

### Component Discovery

KETE uses a lightweight custom IoC with the Reflections library. Components are annotated with `@Component` and auto-discovered at startup.

See [Component Scopes](component-scopes.md) for details.

---

## Current Capabilities

### Destinations (6)

| Kind | Protocol | Description |
|------|----------|-------------|
| `kafka` | Apache Kafka | Event streaming |
| `amqp-0.9.1` | RabbitMQ | Message queue (AMQP 0-9-1) |
| `amqp-1` | AMQP 1.0 | Message queue (Azure Service Bus, ActiveMQ Artemis) |
| `mqtt-3` | MQTT 3.1.1 | IoT messaging |
| `mqtt-5` | MQTT 5.0 | IoT messaging with enhanced features |
| `http` | HTTP/HTTPS | REST APIs, webhooks |

### Serializers (8)

| Kind | Content Type | Use Case |
|------|--------------|----------|
| `json` | `application/json` | Modern APIs, REST endpoints |
| `xml` | `application/xml` | Legacy systems, SOAP, enterprise |
| `yaml` | `application/yaml` | Configuration, human-readable |
| `csv` | `text/csv` | Spreadsheet import, data analysis |
| `toml` | `application/toml` | Configuration management |
| `cbor` | `application/cbor` | IoT, constrained devices |
| `smile` | `application/x-jackson-smile` | High-performance binary JSON |
| `properties` | `text/plain` | Java applications |

### Matchers (4)

| Kind | Description | Example |
|------|-------------|---------|
| `list` | Comma-separated list (case-insensitive) | `list:LOGIN,LOGOUT` |
| `glob` | Unix-style wildcards | `glob:LOGIN*` |
| `regex` | Regular expressions | `regex:LOGIN.*ERROR` |
| `sql` | SQL LIKE patterns | `sql:%LOGIN%` |

### Certificate Loaders (11)

| Kind | Source | Format |
|------|--------|--------|
| `pkcs12-file-path` | File | PKCS12 (.p12, .pfx) |
| `pkcs12-file-base64` | Inline | PKCS12 (Base64) |
| `jks-file-path` | File | JKS/PKCS12 |
| `jks-file-base64` | Inline | JKS/PKCS12 (Base64) |
| `pem-file-path` | File | PEM |
| `pem-file-base64` | Inline | PEM (Base64) |
| `pem-file-text` | Inline | PEM (raw text) |
| `pkcs7-file-path` | File | PKCS7 (.p7b, .p7c) |
| `pkcs7-file-base64` | Inline | PKCS7 (Base64) |
| `der-file-path` | File | DER |
| `der-file-base64` | Inline | DER (Base64) |
