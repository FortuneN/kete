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
- **BouncyCastle**: Keycloak 26.5.0 has post-quantum crypto incompatibility with older BC versions

### What Happens Without Shading

```
java.lang.NoSuchMethodError: com.google.common.base.Preconditions.checkArgument(...)
java.lang.ClassNotFoundException: org.apache.commons.pool2.impl.GenericObjectPoolConfig
java.lang.NoClassDefFoundError: org/bouncycastle/asn1/misc/MiscObjectIdentifiers
```

### How Shading Solves This

Maven Shade Plugin relocates all our dependencies under `io.github.fortunen.kete.shaded.*`:

```
Original:  com.google.guava.common.base.Preconditions
Relocated: io.github.fortunen.kete.shaded.google.common.base.Preconditions
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

### Messaging

| Library | Version | Purpose |
|---------|---------|---------|
| `kafka-clients` | 4.1.1 | Apache Kafka producer |
| `amqp-client` | 5.25.0 | RabbitMQ AMQP 0-9-1 |
| `org.eclipse.paho.client.mqttv3` | 1.2.5 | MQTT 3.1.1 |
| `org.eclipse.paho.mqttv5.client` | 1.2.5 | MQTT 5.0 |
| `qpid-jms-client` | 2.9.0 | AMQP 1.0 (Azure Service Bus, ActiveMQ) |
| `pooled-jms` | 3.1.7 | JMS connection pooling |

### Serialization (Jackson)

| Library | Version | Purpose |
|---------|---------|---------|
| `jackson-databind` | 2.19.2 | JSON (provided by Keycloak) |
| `jackson-dataformat-xml` | 2.19.2 | XML |
| `jackson-dataformat-yaml` | 2.19.2 | YAML |
| `jackson-dataformat-csv` | 2.19.2 | CSV |
| `jackson-dataformat-toml` | 2.19.2 | TOML |
| `jackson-dataformat-smile` | 2.19.2 | Smile (binary JSON) |
| `jackson-dataformat-cbor` | 2.19.2 | CBOR (binary) |
| `jackson-dataformat-properties` | 2.19.2 | Java Properties |

### HTTP & Resilience

| Library | Version | Purpose |
|---------|---------|---------|
| `resilience4j-retry` | 2.3.0 | Retry with exponential backoff |
| `oauth2-oidc-sdk` | 11.26 | OAuth 2.0 Client Credentials |
| `okhttp-tls` | 4.12.0 | TLS utilities |

### Utilities

| Library | Version | Purpose |
|---------|---------|---------|
| `commons-lang3` | 3.20.0 | String utilities |
| `commons-io` | 2.18.0 | File utilities |
| `commons-text` | 1.15.0 | Template interpolation |
| `commons-configuration2` | 2.13.0 | Configuration parsing |
| `commons-pool2` | 2.12.1 | Destination pooling |
| `reflections` | 0.10.2 | Component discovery |
| `glob` | 0.9.0 | Unix glob pattern matching |
| `guava` | 33.4.8 | Caching (Caffeine alternative) |

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
| `assertj-core` | 3.27.3 | Fluent assertions |
| `mockito-core` | 5.21.0 | Mocking framework |
| `mockito-junit-jupiter` | 5.21.0 | Mockito JUnit 5 integration |
| `mockwebserver` | 4.12.0 | HTTP mocking |
| `testcontainers` | 1.21.4 | Container-based integration tests |

---

## Provided by Keycloak

These are NOT bundled in the JAR—Keycloak provides them at runtime:

| Library | Purpose |
|---------|---------|
| `keycloak-core` | Core Keycloak types |
| `keycloak-server-spi` | Event listener SPI |
| `keycloak-server-spi-private` | Internal SPI |
| `keycloak-services` | Keycloak services |
| `jackson-databind` | JSON serialization |

---

## Dependency Highlights

### Resilience4j

Provides retry with exponential backoff for HTTP destinations:

```java
Retry retry = Retry.of("http-destination", RetryConfig.custom()
    .maxAttempts(3)
    .waitDuration(Duration.ofSeconds(1))
    .build());
```

### Nimbus OAuth 2.0 SDK

Full RFC 6749 compliant OAuth 2.0 implementation:

- Client Credentials grant
- Token caching with expiry tracking
- Automatic token refresh

### Apache Commons Pool2

Destination pooling for all destinations:

- Bounded pool sizes
- Validation on borrow/return
- Configurable wait timeout

### Reflections

Lightweight component discovery:

```java
new Reflections("io.github.fortunen.kete")
    .getTypesAnnotatedWith(Component.class)
```

### Eclipse Paho

MQTT client supporting both 3.1.1 and 5.0 protocols:

- QoS 0, 1, 2
- Automatic reconnection
- TLS/SSL support

### Apache Qpid JMS

AMQP 1.0 client compatible with:

- Azure Service Bus
- Apache ActiveMQ Artemis
- Apache Qpid Broker-J

---

## Version Management

Versions are managed in `pom.xml`. Some dependencies use Red Hat-patched versions for compatibility with Keycloak's runtime.

To update dependencies:

```bash
mvn versions:display-dependency-updates
```


