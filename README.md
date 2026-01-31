# **KETE**

> **K**eycloak **E**vents **T**o **E**verywhere is a flexible, high-performance Keycloak extension that streams matched events to various destinations, in various formats.

## Use Cases

- Synchronization: Keep user directories, Databases, CRMs, and other systems in sync with Keycloak
- Security Monitoring: Stream login attempts, failed authentications, and admin actions to SIEM systems
- Audit & Compliance: Maintain immutable audit logs in message queues or event streams
- User Analytics: Track user behavior and authentication patterns
- Event-Driven Architecture: Trigger downstream services based on Keycloak events
- Multi-Destination Routing: Send different event types to different systems simultaneously

## Quick Start (5 minutes)

### Step 1: [Download](https://raw.githubusercontent.com/FortuneN/kete/develop/quick-start/docker-compose.yml) or create docker-compose.yml

```yaml
services:

  rabbitmq:
    image: ghcr.io/fortunen/kete:quick-start-rabbitmq
    ports:
      - 5672:5672
      - 15672:15672
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 5s
      timeout: 5s
      retries: 30

  keycloak:
    image: ghcr.io/fortunen/kete:quick-start-keycloak
    ports:
      - 8080:8080
    environment:
      kete.routes.quick-start.destination.kind: amqp-0.9.1
      kete.routes.quick-start.destination.host: rabbitmq
      kete.routes.quick-start.destination.username: admin
      kete.routes.quick-start.destination.password: admin
      kete.routes.quick-start.destination.exchange: amq.direct
      kete.routes.quick-start.destination.routing-key: keycloak-events
    depends_on:
      rabbitmq:
        condition: service_healthy
```

### Step 2: Start the stack

```bash
docker-compose up -d
```

### Step 3: See events flowing

1. Open Keycloak: [http://localhost:8080](http://localhost:8080) (admin/admin)
2. Do something in Keycloak (log in/out, create a user, ...)
3. Open RabbitMQ: [http://localhost:15672/#/queues/%2F/keycloak-events](http://localhost:15672/#/queues/%2F/keycloak-events) (admin/admin)
4. See events arriving!

## Releases

[See releases →](https://github.com/FortuneN/kete/releases)

## Documentation

[User Guide →](https://fortunen.github.io/kete/user-guide/overview)

[Developer Guide →](https://fortunen.github.io/kete/developer-guide/overview)

## License

[Apache License 2.0 →](https://www.apache.org/licenses/LICENSE-2.0)

## Issues

[Create new issue →](https://github.com/FortuneN/kete/issues/new)

## Credits

| Library | Description |
|---------|-------------|
| [Keycloak](https://www.keycloak.org/) | Open source identity and access management |
| [Lombok](https://projectlombok.org/) | Boilerplate reduction for Java |
| [Apache Commons](https://commons.apache.org/) | Configuration2, Lang3, Text, IO, Pool2 utilities |
| [Apache Kafka Client](https://kafka.apache.org/) | Kafka producer library |
| [RabbitMQ Client](https://www.rabbitmq.com/java-client.html) | AMQP 0-9-1 client |
| [Eclipse Paho](https://www.eclipse.org/paho/) | MQTT 3.1.1 and MQTT 5.0 clients |
| [Apache Qpid JMS](https://qpid.apache.org/components/jms/) | AMQP 1.0 JMS client |
| [Apache ActiveMQ](https://activemq.apache.org/) | STOMP protocol client |
| [Pooled JMS](https://github.com/messaginghub/pooled-jms) | JMS connection pooling |
| [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) | WebSocket client library |
| [OkHttp](https://square.github.io/okhttp/) | HTTP client with TLS support |
| [Nimbus OAuth SDK](https://connect2id.com/products/nimbus-oauth-openid-connect-sdk) | OAuth 2.0 client credentials |
| [Resilience4j](https://resilience4j.readme.io/) | Retry patterns with exponential backoff |
| [Jackson](https://github.com/FasterXML/jackson) | JSON, XML, YAML, CSV, CBOR, TOML, Smile, Properties |
| [hrakaroo/glob](https://github.com/hrakaroo/glob-library-java) | High-performance glob and SQL LIKE patterns |
| [Bouncy Castle](https://www.bouncycastle.org/) | TLS/SSL cryptography provider |
| [Reflections](https://github.com/ronmamo/reflections) | Runtime component discovery |
| [Google Guava](https://github.com/google/guava) | Cached matcher results |
| [SLF4J](https://www.slf4j.org/) | Logging facade |
| [JUnit 5](https://junit.org/junit5/) | Testing framework |
| [Mockito](https://site.mockito.org/) | Mocking framework for tests |
| [AssertJ](https://assertj.github.io/doc/) | Fluent assertions for tests |
| [Testcontainers](https://testcontainers.com/) | Docker-based integration testing |

## Please support the project

| Platform | Type | Link |
|----------|------|------|
| **GitHub** | Free | [Give the project a star](https://github.com/FortuneN/kete) |
| **PayPal** | One-time | [Donate using PayPal](https://paypal.me/FortuneNgwenya) |
| **GitHub Sponsors** | Recurring | [Sponsor on GitHub](https://github.com/sponsors/FortuneN) |
| **Buy Me a Coffee** | One-time | [Donate using Buy Me a Coffee](https://www.buymeacoffee.com/FortuneN) |
| **Ko-fi** | One-time | [Donate using Ko-fi](https://ko-fi.com/FortuneN) |
| **Liberapay** | Recurring | [Donate using Liberapay](https://liberapay.com/FortuneN) |
