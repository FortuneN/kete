# Third-Party Libraries

Complete list of dependencies used by KETE, with versions and purposes.

---

##  CRITICAL: Dependency Shading Requirement

**ALL runtime dependencies (non-`provided`, non-`test` scope) MUST be shade-relocated.**

### Why Shading is Mandatory

Keycloak is a large application with many internal dependencies. Different Keycloak versions bundle different versions of common libraries:

- **Guava**: Version varies across Keycloak releases → `NoSuchMethodError` at runtime
- **Jackson**: Keycloak provides 2.17.x, we need 2.19.x for TOML support → Method signature mismatches
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
- Extension tested with: **Keycloak 26.0.0** (latest stable)
- Shading enables: **Same JAR works on both** without recompilation

### Implementation

See `pom.xml` → `maven-shade-plugin` → `<relocations>` section. Every runtime dependency has a corresponding relocation entry.

**DO NOT** add new runtime dependencies without adding a matching `<relocation>` entry.

---

## Runtime Dependencies

### Messaging & Streaming

| Library | Version | Purpose |
|---------|---------|---------|
| `kafka-clients` | 4.1.1 | Apache Kafka producer |
| `amqp-client` | 5.33.1 | RabbitMQ AMQP 0-9-1 client |
| `org.eclipse.paho.client.mqttv3` | 1.2.5 | MQTT 3.1.1 client |
| `org.eclipse.paho.mqttv5.client` | 1.2.5 | MQTT 5.0 client |
| `qpid-jms-client` | 2.9.0 | AMQP 1.0 JMS client |
| `pooled-jms` | 3.2.2 | JMS connection pooling |
| `pulsar-client` | 3.3.3 | Apache Pulsar producer |
| `lettuce-core` | 6.5.3.RELEASE | Redis Pub/Sub and Streams client |
| `jnats` | 2.20.5 | NATS and JetStream client |
| `activemq-stomp` | 6.1.6 | STOMP protocol client |
| `jeromq` | 0.6.0 | ZeroMQ (pure Java) |
| `Java-WebSocket` | 1.6.0 | WebSocket client |
| `signalr` | 10.0.2 | ASP.NET SignalR hub client |
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
| `azure-storage-queue` | 12.25.0 | Azure Storage Queue client |
| `azure-messaging-webpubsub` | 1.5.4 | Azure Web PubSub client |
| `azure-messaging-eventhubs` | 5.21.3 | Azure Event Hubs client |
| `azure-messaging-servicebus` | 7.17.17 | Azure Service Bus client |
| `azure-messaging-eventgrid` | 4.31.5 | Azure Event Grid client |
| `azure-identity` | 1.15.4 | Managed Identity / Default Azure Credential |
| `azure-core-http-netty` | 1.15.12 | Netty HTTP transport for Azure SDK |

### Google Cloud SDK

| Library | Version | Purpose |
|---------|---------|---------|
| `google-api-services-pubsub` | v1-rev20250414-2.0.0 | GCP Pub/Sub REST client |
| `grpc-google-cloud-tasks-v2` | 2.84.0 | GCP Cloud Tasks gRPC stubs |
| `grpc-okhttp` | 1.79.0 | gRPC OkHttp transport |
| `grpc-auth` | 1.79.0 | gRPC authentication |
| `google-auth-library-oauth2-http` | 1.35.0 | Google OAuth2 credentials |

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

### HTTP, OAuth & Resilience

| Library | Version | Purpose |
|---------|---------|---------|
| `resilience4j-retry` | 2.3.0 | Retry with configurable wait duration |
| `oauth2-oidc-sdk` | 11.26 | OAuth 2.0 Client Credentials |
| `okhttp-tls` | 4.12.0 | TLS utilities |

### Utilities

| Library | Version | Purpose |
|---------|---------|---------|
| `commons-lang3` | 3.20.0 | String utilities |
| `commons-io` | 2.21.0 | File utilities |
| `commons-text` | 1.15.0 | Template interpolation (StringSubstitutor) |
| `commons-configuration2` | 2.13.0 | Configuration parsing |
| `commons-pool2` | 2.13.1 | Destination pooling |
| `reflections` | 0.10.2 | Component discovery |
| `glob` | 0.9.0 | Unix glob pattern matching |
| `guava` | 33.5.0-jre | Caching |
| `slf4j-jdk14` | 2.0.6 | SLF4J to JUL bridge |

### Security

| Library | Version | Purpose |
|---------|---------|---------|
| `bcprov-jdk18on` | 1.80 | Bouncy Castle crypto provider |
| `bcpkix-jdk18on` | 1.80 | Bouncy Castle PKIX/CMS |

### Code Generation

| Library | Version | Purpose |
|---------|---------|---------|
| `lombok` | 1.18.42 | Reduce boilerplate |

---

## Test Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| `junit-jupiter-api` | 5.12.2 | JUnit 5 API |
| `junit-jupiter-engine` | 5.12.2 | JUnit 5 engine |
| `junit-platform-engine` | 1.12.2 | JUnit Platform engine SPI |
| `junit-platform-commons` | 1.12.2 | JUnit Platform shared utilities |
| `assertj-core` | 3.27.7 | Fluent assertions |
| `mockito-core` | 5.21.0 | Mocking framework |
| `mockito-junit-jupiter` | 5.21.0 | Mockito JUnit 5 integration |
| `byte-buddy` | 1.15.11 | Mockito bytecode generation |
| `byte-buddy-agent` | 1.15.11 | Mockito Java agent |
| `mockwebserver` | 4.12.0 | HTTP mocking |
| `slf4j-simple` | 2.0.6 | Logging for tests |
| `testcontainers` | 1.21.4 | Container-based integration tests |
| `testcontainers-kafka` | 1.21.4 | Kafka container module |
| `testcontainers-rabbitmq` | 1.21.4 | RabbitMQ container module |
| `testcontainers-hivemq` | 1.21.4 | HiveMQ container module |
| `testcontainers-mockserver` | 1.21.4 | MockServer container module |
| `mockserver-client-java` | 5.15.0 | MockServer assertion client |
| `testcontainers-keycloak` | 3.7.0 | Keycloak container for E2E tests |
| `keycloak-admin-client` | _(managed)_ | Keycloak admin API for E2E tests |
| `maven-invoker` | 3.3.0 | Programmatic Maven invocation |
| `awaitility` | 4.2.2 | Async condition polling |

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

Versions are managed in `pom.xml`. Jackson versions are controlled via the `jackson-bom`, gRPC versions via the `grpc-bom`, and Keycloak-provided dependency versions via the `keycloak-parent` BOM.

To check for available updates:

```bash
mvn versions:display-dependency-updates
```


