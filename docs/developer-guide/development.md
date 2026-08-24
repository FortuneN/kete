# Development Guide

## Table of Contents

- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Building](#building)
- [Testing](#testing)
- [Code Conventions](#code-conventions)
- [Extending the Extension](#extending-the-extension)
- [Debugging](#debugging)
- [Contributing](#contributing)



## Getting Started

### Prerequisites

- **Java**: JDK 21 or higher
- **Maven**: 3.9.0 or higher
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions
- **Docker**: For integration testing (optional)
- **Git**: For version control

### Clone the Repository

```bash
git clone https://github.com/FortuneN/kete.git
cd kete
```

### IDE Setup

#### IntelliJ IDEA

1. Open the project: `File → Open → Select pom.xml`
2. Wait for Maven import to Complete
3. Set JDK: `File → Project Structure → Project SDK → 21`
4. Enable annotation processing: `Settings → Build → Compiler → Annotation Processors → Enable`

#### VS Code

1. Install extensions:
   - Extension Pack for Java
   - Maven for Java
2. Open folder
3. Select Java 21 in status bar
4. Maven will auto-import dependencies

#### Eclipse

1. Import project: `File → Import → Maven → Existing Maven Projects`
2. Select project directory
3. Set JDK: `Properties → Java Build Path → Libraries → Add Library → JRE System Library`



## Project Structure

```
kete/
├── src/
│   ├── main/
│   │   ├── java/io/github/fortunen/kete/
│   │   │   ├── CertificateLoader.java            # Abstract certificate loader
│   │   │   ├── Component.java                    # DI annotation
│   │   │   ├── Configuration.java                # Config parser
│   │   │   ├── Constants.java                    # Constants
│   │   │   ├── ContentEncoding.java              # Abstract content encoding
│   │   │   ├── ContentTransferEncoding.java      # Abstract content transfer encoding
│   │   │   ├── Destination.java                  # Abstract destination
│   │   │   ├── DestinationConfig.java            # Destination config base
│   │   │   ├── DestinationPooledObjectFactory.java # Destination pooling
│   │   │   ├── EventMessage.java                 # Event data record
│   │   │   ├── Matcher.java                      # Abstract matcher
│   │   │   ├── MatchMode.java                    # Match mode enum
│   │   │   ├── NatsAuthMaterial.java             # NATS auth configuration
│   │   │   ├── OAuthMaterial.java                # OAuth configuration
│   │   │   ├── Provider.java                     # Event handler
│   │   │   ├── ProviderFactory.java              # Lifecycle mgmt
│   │   │   ├── Route.java                        # Route configuration
│   │   │   ├── Serializer.java                   # Abstract serializer
│   │   │   ├── SerializerRoutes.java             # Serializer-routes mapping
│   │   │   ├── TlsMaterial.java                  # TLS configuration
│   │   │   ├── certificateloaders/               # Certificate loader implementations
│   │   │   │   ├── DerFileBase64CertificateLoader.java
│   │   │   │   ├── DerFilePathCertificateLoader.java
│   │   │   │   ├── JksFileBase64CertificateLoader.java
│   │   │   │   ├── JksFilePathCertificateLoader.java
│   │   │   │   ├── PemFileBase64CertificateLoader.java
│   │   │   │   ├── PemFilePathCertificateLoader.java
│   │   │   │   ├── PemFileTextCertificateLoader.java
│   │   │   │   ├── Pkcs12FileBase64CertificateLoader.java
│   │   │   │   ├── Pkcs12FilePathCertificateLoader.java
│   │   │   │   ├── Pkcs7FileBase64CertificateLoader.java
│   │   │   │   └── Pkcs7FilePathCertificateLoader.java
│   │   │   ├── contentencodings/                 # gzip, deflate
│   │   │   ├── contenttransferencodings/         # base64
│   │   │   ├── destinations/                     # Destination implementations (29 destinations)
│   │   │   │   ├── amqp091/                      # AMQP 0-9-1 (RabbitMQ)
│   │   │   │   ├── amqp1/                        # AMQP 1.0 (Qpid JMS)
│   │   │   │   ├── awseventbridge/               # AWS EventBridge
│   │   │   │   ├── awskinesis/                   # AWS Kinesis
│   │   │   │   ├── awssns/                       # AWS SNS
│   │   │   │   ├── awssqs/                       # AWS SQS
│   │   │   │   ├── azureeventgrid/               # Azure Event Grid
│   │   │   │   ├── azureeventhubs/               # Azure Event Hubs
│   │   │   │   ├── azureservicebus/              # Azure Service Bus
│   │   │   │   ├── azurestoragequeue/            # Azure Storage Queue
│   │   │   │   ├── azurewebpubsub/               # Azure Web PubSub
│   │   │   │   ├── gcpcloudtasks/                # GCP Cloud Tasks
│   │   │   │   ├── gcppubsub/                    # GCP Pub/Sub
│   │   │   │   ├── grpc/                         # gRPC
│   │   │   │   ├── http/                         # HTTP webhook
│   │   │   │   ├── kafka/                        # Apache Kafka
│   │   │   │   ├── mqtt3/                        # MQTT 3.1.1
│   │   │   │   ├── mqtt5/                        # MQTT 5.0
│   │   │   │   ├── nats/                         # NATS
│   │   │   │   ├── natsjetstream/                # NATS JetStream
│   │   │   │   ├── pulsar/                       # Apache Pulsar
│   │   │   │   ├── redispubsub/                  # Redis Pub/Sub
│   │   │   │   ├── redisstream/                  # Redis Streams
│   │   │   │   ├── signalr/                      # SignalR
│   │   │   │   ├── soap/                         # SOAP
│   │   │   │   ├── socketio/                     # Socket.IO
│   │   │   │   ├── stomp/                        # STOMP
│   │   │   │   ├── websocket/                    # WebSocket
│   │   │   │   └── zeromq/                       # ZeroMQ
│   │   │   │   # Each subdirectory contains:
│   │   │   │   #   <Name>Destination.java        — send logic
│   │   │   │   #   <Name>DestinationConfig.java  — config parsing
│   │   │   ├── matchers/                         # Matcher implementations
│   │   │   │   ├── GlobMatcher.java
│   │   │   │   ├── ListMatcher.java
│   │   │   │   ├── RegexMatcher.java
│   │   │   │   └── SqlMatcher.java
│   │   │   ├── serializers/                      # Serializer implementations
│   │   │   │   ├── AvroSerializer.java
│   │   │   │   ├── CborSerializer.java
│   │   │   │   ├── CsvSerializer.java
│   │   │   │   ├── JsonSerializer.java
│   │   │   │   ├── MultipartFormSerializer.java
│   │   │   │   ├── PropertiesSerializer.java
│   │   │   │   ├── ProtobufSerializer.java
│   │   │   │   ├── SmileSerializer.java
│   │   │   │   ├── TemplateSerializer.java
│   │   │   │   ├── TomlSerializer.java
│   │   │   │   ├── UrlEncodedFormSerializer.java
│   │   │   │   ├── XmlSerializer.java
│   │   │   │   └── YamlSerializer.java
│   │   │   └── utils/                            # Utility classes
│   │   │       ├── AvroUtils.java
│   │   │       ├── AwsUtils.java
│   │   │       ├── AzureUtils.java
│   │   │       ├── Base64Utils.java
│   │   │       ├── CertificateUtils.java
│   │   │       ├── ConfigurationUtils.java
│   │   │       ├── DestinationUtils.java
│   │   │       ├── ExecutorUtils.java
│   │   │       ├── FileUtils.java
│   │   │       ├── GcpUtils.java
│   │   │       ├── IocUtils.java
│   │   │       ├── JsonUtils.java
│   │   │       ├── JwtUtils.java
│   │   │       ├── MatcherUtils.java
│   │   │       ├── MetricsUtils.java
│   │   │       ├── ProtobufUtils.java
│   │   │       ├── RetryUtils.java
│   │   │       ├── RouteUtils.java
│   │   │       ├── SerializerUtils.java
│   │   │       ├── TemplateUtils.java
│   │   │       └── ValidationUtils.java
│   │   └── resources/
│   │       └── META-INF/services/
│   │           ├── org.keycloak.events.EventListenerProviderFactory
│   │           └── kete/org/apache/qpid/jms/**  # relocated Qpid JMS service files
│   └── test/
│       ├── java/io/github/fortunen/kete/
│       │   ├── unittests/                        # Unit tests
│       │   │   ├── certificateloaders/
│       │   │   ├── contentencodings/
│       │   │   ├── contenttransferencodings/
│       │   │   ├── destinationconfig/
│       │   │   ├── destinationconfigs/
│       │   │   ├── destinationpooledobjectfactory/
│       │   │   ├── destinations/                 # zero-I/O destination tests (29)
│       │   │   ├── eventmessage/
│       │   │   ├── matchers/
│       │   │   ├── natsauthmaterial/
│       │   │   ├── oauthmaterial/
│       │   │   ├── provider/
│       │   │   ├── providerfactory/
│       │   │   ├── route/
│       │   │   ├── serializerroutes/
│       │   │   ├── serializers/
│       │   │   ├── tlsmaterial/
│       │   │   └── utils/
│       │   ├── integrationtests/                 # Integration tests (29 destinations)
│       │   │   ├── amqp091destination/
│       │   │   ├── amqp1destination/
│       │   │   ├── awseventbridgedestination/
│       │   │   ├── awskinesisdestination/
│       │   │   ├── awssnsdestination/
│       │   │   ├── awssqsdestination/
│       │   │   ├── azureeventgriddestination/
│       │   │   ├── azureeventhubsdestination/
│       │   │   ├── azureservicebusdestination/
│       │   │   ├── azurestoragequeuedestination/
│       │   │   ├── azurewebpubsubdestination/
│       │   │   ├── gcpcloudtasksdestination/
│       │   │   ├── gcppubsubdestination/
│       │   │   ├── grpcdestination/
│       │   │   ├── httpdestination/
│       │   │   ├── kafkadestination/
│       │   │   ├── mqtt3destination/
│       │   │   ├── mqtt5destination/
│       │   │   ├── natsdestination/
│       │   │   ├── natsjetstreamdestination/
│       │   │   ├── pulsardestination/
│       │   │   ├── redispubsubdestination/
│       │   │   ├── redisstreamdestination/
│       │   │   ├── signalrdestination/
│       │   │   ├── soapdestination/
│       │   │   ├── socketiodestination/
│       │   │   ├── stompdestination/
│       │   │   ├── websocketdestination/
│       │   │   └── zeromqdestination/
│       │   └── endtoendtests/                    # End-to-end tests (Keycloak container + broker)
│       └── resources/                            # (empty)
├── docs/                                         # Documentation (MkDocs)
├── quick-starts/                                 # 89 runnable quick-starts + $images/ (51 Dockerfiles)
├── schemas/                                      # avro/, json/, protobuf/ message schemas
├── stress-test/                                  # Stress-test harness
├── logo/                                         # Logo assets
├── .github/workflows/                            # CI pipelines (develop, pull-request, release)
├── run-*.ps1                                     # Build/test/release scripts (see Scripts)
├── mkdocs.yml                                    # Documentation site config
├── coverage-badge.json                           # Coverage badge data (updated on develop)
├── pom.xml                                       # Maven config
└── README.md                                     # Main documentation
```

### Key Files

| File | Purpose |
|------|---------|
| `pom.xml` | Maven dependencies and build configuration |
| `META-INF/services/...` | SPI registration for Keycloak |
| `Component.java` | Custom DI annotation for component discovery |
| `ProviderFactory.java` | Extension entry point and lifecycle management |
| `Provider.java` | Event processing logic |
| `Route.java` | Route configuration with matchers, serializer, destination |
| `DestinationConfig.java` | Base class for destination configurations |
| `TlsMaterial.java` | TLS/SSL configuration builder |



## Building

###  Dependency Shading (Critical)

**All runtime dependencies are shade-relocated** to prevent classpath conflicts with Keycloak's internal libraries.

**Why this matters:**
- Keycloak bundles Guava, Jackson, Netty, Apache Commons, etc.
- Different Keycloak versions use different library versions
- Without shading: `NoSuchMethodError`, `ClassNotFoundException` at runtime
- With shading: Extension works across Keycloak 25.x → 26.x+ without recompilation

**What is shaded:**
Every dependency without `<scope>provided</scope>` or `<scope>test</scope>` is relocated under `kete.*`

**See:** `pom.xml` → `maven-shade-plugin` → `<relocations>` section (98 relocations)

**NEVER:** Add runtime dependencies without corresponding `<relocation>` entries.

#### Kafka SASL/JAAS Classloader Workaround

The Kafka client uses JAAS (`javax.security.auth.login`) for SASL authentication (e.g., PLAIN, SCRAM, OAUTHBEARER). After shading, two problems arise:

**Problem 1 — JAAS class name resolution:**
Users configure `sasl.jaas.config` with standard class names like `org.apache.kafka.common.security.plain.PlainLoginModule`. After shading, that class lives at `kete.org.apache.kafka.common.security.plain.PlainLoginModule`. JAAS `LoginContext` does `Class.forName()` on the class name from the config — if it still says `org.apache.kafka...`, the class is not found.

**Solution:** `KafkaDestinationConfig.doInitialize()` automatically rewrites `org.apache.kafka.` → `kete.org.apache.kafka.` in the `sasl.jaas.config` value. Users always write standard class names; the rewrite is invisible.

**Problem 2 — Thread Context ClassLoader (TCCL):**
JAAS `LoginContext` uses `Thread.currentThread().getContextClassLoader()` to load the LoginModule class. In Keycloak, the TCCL is Keycloak's classloader, which cannot see classes inside the provider JAR. Even after rewriting the class name, `Class.forName("kete.org.apache.kafka...PlainLoginModule")` fails because the TCCL doesn't have visibility.

**Solution:** `KafkaDestination.doInitialize()` temporarily sets the TCCL to the provider JAR's classloader before creating `KafkaProducer` and `AdminClient`, then restores it in a `finally` block.

**Problem 3 — Shade plugin rewrites string constants:**
The Maven Shade Plugin rewrites ALL string literals matching relocation patterns — including the `"org.apache.kafka."` constant used for the rewrite comparison itself. At runtime, both the "before" and "after" constants would become `"kete.org.apache.kafka."`, making the rewrite a no-op.

**Solution:** The `KAFKA_PACKAGE_PREFIX` constant is constructed at runtime using `String.join(".", "org", "apache", "kafka") + "."` so the shade plugin cannot match and rewrite it.

!!! warning "If you touch this code"
    These three workarounds are tightly coupled. Changing one without understanding the others will break Kafka SASL authentication in Keycloak deployments. The unit tests in `kafkadestinationconfig/initializeTests.java` cover the config rewriting; the classloader fix is validated by the `kafka-azure-event-hubs-emulator` quickstart.

### Full Build

```bash
mvn clean package
```

**Output**: `target/kete.jar` (shaded JAR with all dependencies isolated)

### Skip Tests

```bash
mvn clean package -DskipTests
```

### Clean Build

```bash
mvn clean
```

### Build with Coverage

```bash
mvn clean test jacoco:report
```

**Output**: `target/site/jacoco/index.html`

### Verify Build

```bash
mvn verify
```

Runs all tests and creates the final JAR.



## Testing

### Unit Tests

Run all unit tests (a bare `mvn test` would also run the container-based integration and E2E tests):

```bash
.\run-unit-tests.ps1
# or
mvn test -Dtest="io.github.fortunen.kete.unittests.**"
```

Run specific test:

```bash
mvn test -Dtest=io.github.fortunen.kete.unittests.provider.onEventTests
```

Run tests in a package:

```bash
mvn test -Dtest="io.github.fortunen.kete.unittests.provider.**"
```

### Test Naming Convention

Tests are organized following the one-method-per-file pattern:

**Directory structure:** `{classname}/{methodName}Tests.java`

Examples:
- `provider/constructorTests.java` - Tests `Provider` constructor
- `provider/onEventTests.java` - Tests `Provider.onEvent()` method
- `providerfactory/closeTests.java` - Tests `ProviderFactory.close()` method

**Method naming:** `should{ExpectedBehavior}[When{Condition}]`

Examples:
- `shouldReturnTrueWhenEventMatches()`
- `shouldThrowWhenConfigurationIsNull()`
- `shouldSerializeEventToJson()`

### Test Structure (AAA Pattern)

All tests use the Arrange-Act-Assert pattern with required comments:

```java
@Test
public void shouldDoSomethingWhenConditionMet() {

    // arrange

    var instance = new ClassUnderTest();
    var input = "test-input";

    // act

    var result = instance.methodUnderTest(input);

    // assert

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo("expected");
}
```

### Mocking with Mockito

Use `mock()` method (not `@Mock` annotations):

```java
@Test
public void shouldSendMessageToDestination() {

    // arrange

    var destination = mock(Destination.class);
    when(destination.accept("LOGIN")).thenReturn(true);

    // act

    route.send(message);

    // assert

    verify(destination).sendMessage(any(EventMessage.class));
}
```

### Exception Testing

Use `catchThrowable()` with chained assertions:

```java
@Test
public void shouldThrowWhenConfigurationIsNull() {

    // arrange

    var instance = new ClassUnderTest();

    // act

    var thrown = catchThrowable(() -> instance.initialize(null));

    // assert

    assertThat(thrown)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("configuration is required");
}
```

### Integration Tests

Integration tests use Testcontainers for Kafka, RabbitMQ, etc.:

```bash
# Requires Docker
.\run-integration-tests.ps1
# or
mvn test -Dtest="io.github.fortunen.kete.integrationtests.**"
```

Tests automatically start containers, run tests, and clean up. End-to-end tests (`.\run-end-to-end-tests.ps1`) additionally deploy the shaded jar into a Keycloak container.

### Coverage Reports

```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html  # macOS/Linux
start target/site/jacoco/index.html # Windows
```

**Current Coverage**: see the badge in the README (`coverage-badge.json`, regenerated by the develop pipeline)



## Code Conventions

### Java Style

- **Indentation**: Tabs, no spaces
- **Line Length**: None
- **Braces**: K&R style (opening brace on same line)
- **Naming**:
  - Classes: `PascalCase`
  - Methods: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Packages: `lowercase`
- **No fully-qualified class names inline**: Always use import statements. Write `new MqttClient(...)` not `new org.eclipse.paho.client.mqttv3.MqttClient(...)`. The only exception is when two classes share the same simple name and disambiguation is required.
- **Use `var`**: Prefer `var` for local variable declarations when type is inferrable.

### Code Example

```java
@Slf4j
@Data
@NoArgsConstructor(force = true)
public class MyComponent {

    private static final String CONSTANT_VALUE = "value";

    private String configuration;

    public void initialize() {
        ValidationUtils.requireNonBlank(configuration, "configuration is required");
    }

    public void processEvent(Event event) {
        try {
            var type = ValidationUtils.requireNonNull(event, "event is required").getType();
            // Process event
        } catch (Exception exception) {
            log.warn("Failed to process event", exception);
        }
    }
}
```

### Documentation

- **No JavaDoc**: Code should be self-documenting through clear naming
- **Inline Comments**: Minimal - only for truly complex logic
- **README**: Keep README.md up to date
- **Developer Docs**: Update docs/ when adding features



## Extending the Extension

### Adding a New Destination

See [Extending KETE](extending.md) for comprehensive details.

**Quick Steps**:

1. Create config class extending `DestinationConfig`
2. Create destination class extending `Destination<TConfig>`
3. Add `@Component(name = "xxx")` annotation
4. Implement `doInitialize()` and `doSend(EventMessage)`
5. Add tests following the [test patterns](test-patterns-and-conventions.md)
6. Add destination documentation page (see below)

#### Destination Documentation Checklist

Every new destination **requires** a documentation page at `docs/user-guide/destinations/<kind>.md` and updates to several cross-reference pages.

**Destination page sections** (strict order):

| # | Section | Notes |
|---|---------|-------|
| 1 | `# <Name> Destination` | Page title |
| 2 | One-liner description | "Stream Keycloak events to `<system>`." |
| 3 | Kind/Protocol table | `destination.kind` value + Protocol name |
| 4 | `## Compatible Systems` | Table of brokers/services with notes |
| 5 | `## Example Configurations` | Tabbed examples (`=== "Tab Name"` syntax, min 2 tabs) |
| 6 | `## Features` | Bullet list of capabilities |
| 7 | `## Configuration Properties` | Sub-sections: Required → Optional → Templating → Headers → Auth → TLS |
| 8 | `## Configuration Examples` | Numbered: `### Example 1: <Title>`, `### Example 2: <Title>`, etc. |
| 9 | `## Quick Starts` | Table linking to quickstart folders |
| 10 | `## See Also` | Links to Serializers, Matchers, Event Types, Certificate Loaders |

**Cross-reference pages to update:**

| Page | What to Update |
|------|---------------|
| `mkdocs.yml` | Add nav entry under `Destinations:` |
| `destinations/overview.md` | Available Destinations table, Cloud Services Compatibility table (if cloud), Message Headers table, Quick Examples section |
| `destinations/support-matrix.md` | Quick Reference Matrix (add column + row), "By Protocol" section, Decision Guide tree, Performance Considerations table, Available Quickstarts table |

**Reference pages**: `kafka.md` (complex destination), `http.md` (OAuth, headers), `pulsar.md` (auth methods), `zeromq.md` (limitations section)

### Adding a New Serializer

See [Extending KETE](extending.md) for comprehensive details.

**Quick Steps**:

1. Create class extending `Serializer`
2. Add `@Component(name = "xxx", scope = Component.SINGLETON)`
3. Set `contentType` via a field initializer (e.g. `private String contentType = "application/json";`)
4. Implement `serialize(Event)` and `serialize(AdminEvent)`
5. Add tests

### Adding a New Matcher

See [Extending KETE](extending.md) for comprehensive details.

**Quick Steps**:

1. Create class extending `Matcher`
2. Add `@Component(name = "xxx")` annotation
3. Implement `initialize()` and `matches(String)`
4. Add tests



## Debugging

### Local Debugging with IDE

1. Build the extension:
```bash
mvn clean package
```

2. Copy to local Keycloak:
```bash
cp target/kete.jar ~/keycloak-26.0.7/providers/
```

3. Start Keycloak in debug mode:
```bash
export KC_LOG_LEVEL=DEBUG
export JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
~/keycloak-26.0.7/bin/kc.sh start-dev
```

4. Attach debugger:
   - IntelliJ: `Run → Attach to Process → Select Keycloak`
   - VS Code: Add configuration:

```json
{
    "type": "java",
    "name": "Attach to Keycloak",
    "request": "attach",
    "hostName": "localhost",
    "port": 5005
}
```

5. Set breakpoints in your code

6. Trigger events in Keycloak (login, logout, etc.)

### Docker Debugging

1. Build Docker image with debug enabled:

```dockerfile
FROM quay.io/keycloak/keycloak:26.0.7
COPY target/kete.jar /opt/keycloak/providers/
ENV JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
RUN /opt/keycloak/bin/kc.sh build
```

2. Run with port exposed:
```bash
docker run -p 8080:8080 -p 5005:5005 keycloak-debug start-dev
```

3. Attach debugger to `localhost:5005`

### Logging

Enable detailed logging:

```bash
export KC_LOG_LEVEL=INFO,io.github.fortunen.kete:DEBUG
```

View logs:

```bash
# Docker
docker logs -f keycloak

# Standalone
tail -f $KEYCLOAK_HOME/data/log/keycloak.log
```

### Common Debug Scenarios

#### Extension not loading

1. Check `providers/` directory has JAR
2. Check logs for SPI registration
3. Verify `META-INF/services` file is in JAR:
```bash
jar tf target/kete.jar | grep META-INF
```

#### Events not streaming

1. Check `enabled` is not set to `false` (defaults to `true`)
2. Check realm has listener registered:
   - Admin Console → Realm → Events → Event Listeners
3. Check destination configuration is valid
4. Check destination connection logs

#### Serialization errors

1. Add breakpoint in `Serializer.serialize()`
2. Check event structure
3. Verify Jackson configuration



## Contributing

### Development Workflow

1. **Fork** the repository on GitHub

2. **Clone** your fork:
```bash
git clone https://github.com/YOUR_USERNAME/kete.git
cd kete
```

3. **Create a feature branch**:
```bash
git checkout -b feature/my-new-feature
```

4. **Make changes** following code conventions

5. **Write tests** for new code

6. **Run tests locally**:
```bash
mvn clean test
```

7. **Run coverage check**:
```bash
mvn clean test jacoco:report
# Aim for >80% coverage
```

8. **Commit with descriptive message**:
```bash
git commit -m "Add SQS destination support"
```

9. **Push to your fork**:
```bash
git push origin feature/my-new-feature
```

10. **Create Pull Request** on GitHub

### Pull Request Guidelines

- **One feature per PR**: Keep PRs focused
- **Tests required**: All new code must have tests
- **Documentation**: Update docs for user-facing changes
- **Code style**: Follow existing conventions
- **Clean history**: Squash commits if needed
- **Descriptive title**: "Add SQS destination" not "Update"

### Commit Message Format

```
[Type] Short description

Longer description if needed explaining what and why.

Fixes #123
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `test`: Test additions/changes
- `refactor`: Code refactoring
- `perf`: Performance improvement
- `chore`: Build/tooling changes

### Code Review Process

1. The pull-request workflow runs `run-on-pull-request-push.ps1` (all tests, quick-start image build, `mkdocs build --strict`)
2. Maintainer reviews code
3. Address feedback
4. Approval → Merge

### Getting Help

- **Issues**: Open GitHub issue for bugs/features
- **Discussions**: Use GitHub discussions for questions
- **Documentation**: Check existing docs first



## Useful Maven Commands

```bash
# Build without tests
mvn clean package -DskipTests

# Run single test class
mvn test -Dtest=ProviderTest

# Run tests matching pattern
mvn test -Dtest=*Provider*

# Show dependency tree
mvn dependency:tree

# Update dependencies
mvn versions:display-dependency-updates

# Format code (if formatter configured)
mvn formatter:format

# Check for outdated plugins
mvn versions:display-plugin-updates

# Install to local Maven repo
mvn clean install
```



## Troubleshooting

### Build Fails

**Problem**: `JAVA_HOME not set`

**Solution**:
```bash
export JAVA_HOME=/path/to/jdk-21
# or on Windows
set JAVA_HOME=C:\Program Files\Java\jdk-21
```

**Problem**: `Maven version too old`

**Solution**: Upgrade to Maven 3.9+
```bash
mvn --version  # Check version
# Download from https://maven.apache.org/download.cgi
```

### Tests Fail

**Problem**: Docker not running for Testcontainers

**Solution**: Start Docker daemon
```bash
docker ps  # Verify Docker is running
```

**Problem**: Port conflicts in tests

**Solution**: Stop services using ports 9092 (Kafka), 5672 (RabbitMQ)

### IDE Issues

**Problem**: "Cannot resolve symbol" errors

**Solution**: Reimport Maven project
- IntelliJ: `Maven → Reload Project`
- Eclipse: `Right-click project → Maven → Update Project`
- VS Code: `Cmd+Shift+P → Java: Clean Language Server Workspace`



## Resources

- [Keycloak SPI Documentation](https://www.keycloak.org/docs/latest/server_development/)
- [Reflections Library](https://github.com/ronmamo/reflections)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/tutorials.html)
- [Jackson Documentation](https://github.com/FasterXML/jackson-docs)
