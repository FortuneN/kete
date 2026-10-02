# Third-Party Libraries

Complete list of dependencies used by KETE, with versions and purposes.

---

##  CRITICAL: Dependency Shading Requirement

**ALL runtime dependencies (non-`provided`, non-`test` scope) MUST be shade-relocated.**

### Why Shading is Mandatory

Keycloak is a large application with many internal dependencies. Different Keycloak versions bundle different versions of common libraries:

- **Guava**: Version varies across Keycloak releases → `NoSuchMethodError` at runtime
- **Jackson**: Keycloak provides 2.17.x, we need 2.20+ for TOML support → Method signature mismatches
- **Netty**: Transitive from Kafka, also used by Keycloak → Class loading conflicts
- **Apache Commons**: Multiple libraries (lang3, text, pool2, io) with version mismatches
- **BouncyCastle**: Newer Keycloak versions have post-quantum crypto incompatibility with older BC versions

### What Happens Without Shading

```
java.lang.NoSuchMethodError: com.google.common.base.Preconditions.checkArgument(...)
java.lang.ClassNotFoundException: org.apache.commons.pool2.impl.GenericObjectPoolConfig
java.lang.NoClassDefFoundError: org/bouncycastle/asn1/misc/MiscObjectIdentifiers
```

### How Shading Solves This

Maven Shade Plugin relocates all our dependencies under `kete.*`:

```
Original:  com.google.common.base.Preconditions
Relocated: kete.com.google.common.base.Preconditions
```

Result: **Zero classpath conflicts** - Our libraries are completely isolated from Keycloak's.

### Multi-Version Keycloak Compatibility

- Extension compiles against: **Keycloak 25.0.6** (minimum supported)
- Extension tested with: **Keycloak 26.0.0** (end-to-end tests) and **26.0.7** (quick-start image)
- Shading enables: **Same JAR works on both** without recompilation

### Implementation

See `pom.xml` → `maven-shade-plugin` → `<relocations>` section. Every runtime dependency has a corresponding relocation entry.

**DO NOT** add new runtime dependencies without adding a matching `<relocation>` entry.

---

## Runtime Dependencies

### Messaging & Streaming

| Library | Version | Purpose |
|---------|---------|---------|
| `kafka-clients` | 4.3.1 | Apache Kafka producer |
| `amqp-client` | 5.37.0 | RabbitMQ AMQP 0-9-1 client |
| `org.eclipse.paho.client.mqttv3` | 1.2.5 | MQTT 3.1.1 client |
| `org.eclipse.paho.mqttv5.client` | 1.2.5 | MQTT 5.0 client |
| `qpid-jms-client` | 2.10.0 | AMQP 1.0 JMS client (2.11 needs proton-j 0.35, which changes AMQP defaults) |
| `pooled-jms` | 3.2.5 | JMS connection pooling |
| `pulsar-client` | 3.3.3 | Apache Pulsar producer |
| `lettuce-core` | 6.5.3.RELEASE | Redis Pub/Sub and Streams client |
| `jnats` | 2.26.3 | NATS and JetStream client (its `bcprov-lts8on` is excluded; Ed25519 signing uses `bcprov-jdk18on`) |
| `activemq-stomp` | 6.3.2 | STOMP protocol client |
| `jeromq` | 0.6.0 | ZeroMQ (pure Java) |
| `Java-WebSocket` | 1.6.0 | WebSocket client |
| `signalr` | 10.0.12 | ASP.NET SignalR hub client |
| `socket.io-client` | 2.1.2 | Socket.IO protocol client |

### AWS SDK

| Library | Version | Purpose |
|---------|---------|---------|
| `sqs` | 2.31.51 | Amazon SQS client |
| `sns` | 2.31.51 | Amazon SNS client |
| `kinesis` | 2.31.51 | Amazon Kinesis client |
| `eventbridge` | 2.31.51 | Amazon EventBridge client |
| `sts` | 2.31.51 | AWS STS (assume role) |
| `url-connection-client` | 2.31.51 | HTTP transport for AWS SDK |

### Azure SDK

| Library | Version | Purpose |
|---------|---------|---------|
| `azure-storage-queue` | 12.30.0 | Azure Storage Queue client |
| `azure-messaging-webpubsub` | 1.5.6 | Azure Web PubSub client |
| `azure-messaging-eventhubs` | 5.21.5 | Azure Event Hubs client |
| `azure-messaging-servicebus` | 7.17.19 | Azure Service Bus client |
| `azure-messaging-eventgrid` | 4.31.7 | Azure Event Grid client |
| `azure-identity` | 1.18.4 | Managed Identity / Default Azure Credential |
| `azure-core-http-netty` | 1.16.5 | Netty HTTP transport for Azure SDK |

### Google Cloud SDK

| Library | Version | Purpose |
|---------|---------|---------|
| `google-api-services-pubsub` | v1-rev20250414-2.0.0 | GCP Pub/Sub REST client |
| `grpc-google-cloud-tasks-v2` | 2.97.0 | GCP Cloud Tasks gRPC stubs |
| `google-auth-library-oauth2-http` | 1.52.0 (managed via `google-auth-library-bom`) | Google OAuth2 credentials |

### Serialization (Jackson)

| Library | Version | Purpose |
|---------|---------|---------|
| `jackson-databind` | 2.19.2 | JSON serialization |
| `jackson-dataformat-xml` | 2.19.2 | XML serialization |
| `jackson-dataformat-yaml` | 2.19.2 | YAML serialization |
| `jackson-dataformat-csv` | 2.19.2 | CSV serialization |
| `jackson-dataformat-toml` | 2.19.2 | TOML serialization |
| `jackson-dataformat-smile` | 2.19.2 | Smile (binary JSON) serialization |
| `jackson-dataformat-cbor` | 2.19.2 | CBOR (binary) serialization |
| `jackson-dataformat-properties` | 2.19.2 | Java Properties serialization |

### Serialization (Avro & Protobuf)

| Library | Version | Purpose |
|---------|---------|---------|
| `avro` | 1.12.2 | Avro serializer (schema-based, `GenericRecord`) |
| `protobuf-java` | 4.36.2 (managed; at least the 4.33.6 the Cloud Tasks stubs are generated for) | Protobuf serializer (`DynamicMessage` from the bundled descriptor set) |

### gRPC

| Library | Version | Purpose |
|---------|---------|---------|
| `grpc-okhttp`, `grpc-auth` (declared) | 1.84.0 (managed via `grpc-bom`) | gRPC OkHttp transport and auth for the gRPC and GCP Cloud Tasks destinations |
| `grpc-api`, `grpc-core`, `grpc-stub`, `grpc-context`, `grpc-util`, `grpc-protobuf`, `grpc-protobuf-lite` (transitive) | 1.84.0 (managed via `grpc-bom`) | gRPC runtime pulled in by the declared gRPC artifacts and the Cloud Tasks stubs |

### HTTP, OAuth & Resilience

| Library | Version | Purpose |
|---------|---------|---------|
| `resilience4j-retry` | 2.4.0 | Retry with configurable wait duration |
| `oauth2-oidc-sdk` | 11.38.2 (with `nimbus-jose-jwt` 10.9.1, the version it declares) | OAuth 2.0 Client Credentials |

### Utilities

| Library | Version | Purpose |
|---------|---------|---------|
| `commons-lang3` | 3.20.0 | String utilities |
| `commons-io` | 2.22.0 | File utilities |
| `commons-text` | 1.15.0 | Template interpolation (StringSubstitutor) |
| `commons-configuration2` | 2.15.1 | Configuration parsing |
| `commons-pool2` | 2.13.1 | Destination pooling |
| `reflections` | 0.10.2 | Component discovery |
| `glob` | 0.9.0 | Unix glob pattern matching |
| `guava` | 33.7.2-jre | Caching |
| `slf4j-jdk14` | 2.0.19 | SLF4J to JUL bridge |

### Security

| Library | Version | Purpose |
|---------|---------|---------|
| `bcprov-jdk18on` | 1.86 | Bouncy Castle crypto provider |
| `bcpkix-jdk18on` | 1.86 | Bouncy Castle PKIX/CMS |
| `bcutil-jdk18on` | 1.86 | Bouncy Castle utilities |

### Code Generation

| Library | Version | Purpose |
|---------|---------|---------|
| `lombok` | 1.18.48 | Reduce boilerplate |

---

## Test Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| `junit-jupiter-api` | 5.14.4 (managed via `junit-bom`) | JUnit 5 API |
| `junit-jupiter-engine` | 5.14.4 (managed via `junit-bom`) | JUnit 5 engine |
| `junit-platform-engine` | 1.14.4 (managed via `junit-bom`) | JUnit Platform engine SPI |
| `junit-platform-commons` | 1.14.4 (managed via `junit-bom`) | JUnit Platform shared utilities |
| `assertj-core` | 3.27.7 | Fluent assertions |
| `mockito-core` | 5.23.0 | Mocking framework |
| `mockito-junit-jupiter` | 5.23.0 | Mockito JUnit 5 integration |
| `byte-buddy` | 1.18.14 | Mockito bytecode generation |
| `byte-buddy-agent` | 1.18.14 | Mockito Java agent |
| `mockwebserver` | 4.12.0 | HTTP mocking |
| `okhttp-tls` | 4.12.0 | `HeldCertificate` fixtures for the certificate-loader unit tests (test scope only; runtime test certificates come from `CertificateUtils`) |
| `slf4j-simple` | 2.0.19 | Logging for tests |
| `testcontainers` | 1.21.4 | Container-based integration tests |
| `testcontainers-kafka` | 1.21.4 | Kafka container module |
| `testcontainers-rabbitmq` | 1.21.4 | RabbitMQ container module |
| `testcontainers-hivemq` | 1.21.4 | HiveMQ container module |
| `testcontainers-keycloak` | 3.9.1 (4.x needs Testcontainers 2) | Keycloak container for E2E tests |
| `keycloak-admin-client` | _(managed)_ | Keycloak admin API for E2E tests |
| `maven-invoker` | 3.3.0 | Programmatic Maven invocation |
| `awaitility` | 4.3.0 | Async condition polling |

---

## Provided by Keycloak

These are NOT bundled in the JAR — Keycloak provides them at runtime:

| Library | Purpose |
|---------|---------|
| `keycloak-core` | Core Keycloak types |
| `keycloak-server-spi` | Event listener SPI |
| `keycloak-services` | Keycloak services |
| `micrometer-core` | Metrics (Keycloak's metrics subsystem) |

---

## Version Management

Versions are managed in `pom.xml`. Jackson, Netty, gRPC, JUnit, OpenTelemetry, Reactor, the Azure SDK and the Google HTTP client and auth libraries are controlled via their BOMs, and Keycloak-provided dependency versions via the `keycloak-parent` BOM. BOM import order matters — the first BOM that manages an artifact wins — so every library BOM is imported before `keycloak-parent`, which (through `quarkus-bom`) would otherwise manage many of them down to older versions. Artifact families that must move together (AWS SDK, BouncyCastle, gRPC, protobuf and `protoc`, Testcontainers, ActiveMQ, Mockito, Byte Buddy, SLF4J, Lombok, JaCoCo) share one version property each, so a Dependabot update moves the whole family; `run-coverage-badge.ps1` reads `jacoco.version` from the POM.

To check for available updates:

```bash
mvn versions:display-dependency-updates
```


