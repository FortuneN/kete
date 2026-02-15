# Integration Tests

## Table of Contents

- [Overview](#overview)
- [Running Integration Tests](#running-integration-tests)
- [Debugging Integration Tests](#debugging-integration-tests)
- [Test Categories](#test-categories)
- [Writing New Integration Tests](#writing-new-integration-tests)
- [Container File Mounting Best Practices](#container-file-mounting-best-practices)
- [Troubleshooting](#troubleshooting)



## Overview

Integration tests verify that the kete provider works correctly when deployed inside a real Keycloak instance. These tests use [Testcontainers](https://www.testcontainers.org/) to spin up Docker containers for Keycloak and supporting infrastructure (MockServer for HTTP destinations, etc.).

### Prerequisites

- Docker must be running
- Maven must be installed
- Project must be compiled (`mvn compile`)



## Running Integration Tests

### Run All Tests (Unit + Integration)

```bash
mvn test
```

### Run Only Integration Tests

```bash
mvn test -Dtest="*IntegrationTests"
```

### Run Only Unit Tests

```bash
mvn test -Dtest="!*IntegrationTests"
```

### Run Tests with Coverage

```bash
mvn clean test jacoco:report
```



## Debugging Integration Tests

There are several approaches to debug your EventListener provider code during integration tests.

### Option 1: Remote Debugging (Attach Debugger to Keycloak Container)

The Keycloak Testcontainer supports remote debugging. Add `.withDebugFixedPort(5005, true)` to suspend Keycloak until you attach a debugger:

```java
@Container
static KeycloakContainer keycloak = new KeycloakContainer(TestUtils.KEYCLOAK_IMAGE)
    .withDefaultProviderClasses()
    .withDebugFixedPort(5005, true)  // Enable debug on port 5005, suspend until attached
    // ... other configuration
```

**Steps:**

1. Modify the test to add `.withDebugFixedPort(5005, true)`
2. Run the test - it will pause waiting for debugger
3. In VS Code, create a debug configuration:
   ```json
   {
     "type": "java",
     "name": "Attach to Keycloak Container",
     "request": "attach",
     "hostName": "localhost",
     "port": 5005
   }
   ```
4. Start the debug session - Keycloak will resume and hit your breakpoints

### Option 2: Debug Without Suspend

Use `.withDebug()` to enable debugging without suspending:

```java
@Container
static KeycloakContainer keycloak = new KeycloakContainer(TestUtils.KEYCLOAK_IMAGE)
    .withDefaultProviderClasses()
    .withDebug()  // Debug port on random port, no suspend
```

Get the debug port with `keycloak.getMappedPort(5005)`.

### Option 3: Embedded Undertow (Keycloak Arquillian)

For deep debugging where you need to run Keycloak in the same JVM, you can use Keycloak's Arquillian testsuite with embedded Undertow:

```bash
mvn -f testsuite/integration-arquillian/pom.xml test
```

This runs Keycloak on embedded Undertow in the same JVM as your tests.



## Test Categories

### Keycloak Boot Tests

Located in: `src/test/java/.../integrationtests/keycloak/CleanKeycloakBootTests.java`

Verifies basic Keycloak container functionality:
- Container starts successfully
- Admin client can connect
- Realms are accessible

### Provider Integration Tests

Located in: `src/test/java/.../integrationtests/provider/EventListenerProviderIntegrationTests.java`

Verifies the kete provider:
- Provider loads into Keycloak
- Events are forwarded to destinations
- Serialization works correctly
- Admin events are captured



## Writing New Integration Tests

### Basic Template

```java
@Testcontainers
class MyIntegrationTests {

    static Network network = Network.newNetwork();

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer(TestUtils.KEYCLOAK_IMAGE)
        .withNetwork(network)
        .withDefaultProviderClasses()
        .withEnv("enabled", "true")
        .withEnv("kete.routes.my-route.realm-matchers.filter", "list:test")
        .withEnv("kete.routes.my-route.destination.kind", "http")
        .withEnv("kete.routes.my-route.destination.url", "http://destination:8080")
        .withEnv("kete.routes.my-route.serializer.kind", "json");

    @Test
    void myTest() {
        // Use adminClient to interact with Keycloak
        // Verify events are forwarded to destinations
    }
}
```

### Mounting Configuration Files

**CRITICAL**: When tests require custom configuration files for containers (broker configs, certificates, etc.), **ALWAYS use in-memory `Transferable.of()` with 0777 permissions**. NEVER use `withFileSystemBind()` or `withCopyToContainer()` without permissions.

```java
// Step 1: Read file content into memory
var brokerXmlBytes = Files.readAllBytes(Path.of(brokerXmlPath));

// Step 2: Copy to container memory with full permissions
@Container
static GenericContainer<?> broker = new GenericContainer<>(imageName)
    .withCopyToContainer(Transferable.of(brokerXmlBytes, 0777), "/etc/broker.xml");
```

See [Container File Mounting Best Practices](#container-file-mounting-best-practices) for complete details.

### Key Configuration Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `enabled` | Enable the provider | `true` |
| `kete.routes.<name>.realm-matchers.<name>` | Realm filter | `list:master` |
| `kete.routes.<name>.destination.kind` | Destination kind | `http`, `kafka`, `amqp-0.9.1` |
| `kete.routes.<name>.destination.url` | Destination URL | `http://server:8080/events` |
| `kete.routes.<name>.serializer.kind` | Serialization format | `json`, `xml`, `yaml` |

### Adding Destination Containers

For Kafka:
```java
@Container
static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
    .withNetwork(network)
    .withNetworkAliases("kafka");
```

For RabbitMQ:
```java
@Container
static RabbitMQContainer rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.12-management"))
    .withNetwork(network)
    .withNetworkAliases("rabbitmq");
```



## Container File Mounting Best Practices

### Why In-Memory Transfer with Full Permissions?

**ALWAYS use `withCopyToContainer(Transferable.of(bytes, 0777))` for mounting files into containers. NEVER use `withFileSystemBind()`.**

| Aspect | In-Memory Transfer (`Transferable.of()`) | File System Bind (`withFileSystemBind`) |
|--------|------------------------------------------|----------------------------------------|
| **GitHub Actions** | ✅ Works reliably in CI/CD | ❌ Fails due to filesystem limitations |
| **Permissions** | Full control with 0777 parameter | Unpredictable based on host OS |
| **Cross-platform** | Identical behavior on all OS | Different behavior Windows/Linux/macOS |
| **Performance** | Fast in-memory copy | Filesystem mount overhead |
| **Best Practice** |  **REQUIRED in this codebase** |  **FORBIDDEN** |

### Standard In-Memory Transfer Pattern

Use this pattern consistently across all TestBase classes:

```java
// Step 1: Read file content into byte array
var configBytes = Files.readAllBytes(Path.of(sourceConfigPath));
var keystoreBytes = Files.readAllBytes(Path.of(sourceKeystorePath));
var truststoreBytes = Files.readAllBytes(Path.of(sourceTruststorePath));

// Step 2: Copy to container memory with 0777 permissions
container = new GenericContainer<>(imageName)
    .withCopyToContainer(Transferable.of(configBytes, 0777), "/container/path/config.xml")
    .withCopyToContainer(Transferable.of(keystoreBytes, 0777), "/container/path/keystore.jks")
    .withCopyToContainer(Transferable.of(truststoreBytes, 0777), "/container/path/truststore.jks");
```

### Complete Example: AMQP1 with TLS

From `io.github.fortunen.kete.integrationtests.amqp1destination.TestBase`:

```java
private void startActiveMqArtemisWithTls(TlsMaterial tls, boolean requireClientAuth) throws Exception {
    
    // Create broker configuration XML as string
    var brokerXml = createArtemisBrokerXml(
        tls.getKeyStorePassword(),
        tls.getTrustStorePassword(),
        requireClientAuth
    );

    // Read certificate files into memory
    var keystoreBytes = Files.readAllBytes(Path.of(tls.getServerKeyStoreFilePath()));
    var truststoreBytes = Files.readAllBytes(Path.of(tls.getTrustStoreFilePath()));

    // Copy all files to container memory with 0777 permissions
    container = new GenericContainer<>(DockerImageName.parse("apache/activemq-artemis:2.40.0-alpine"))
        .withEnv("ARTEMIS_USER", DEFAULT_USERNAME)
        .withEnv("ARTEMIS_PASSWORD", DEFAULT_PASSWORD)
        .withEnv("ANONYMOUS_LOGIN", "true")
        .withCopyToContainer(
            Transferable.of(brokerXml.getBytes(StandardCharsets.UTF_8), 0777),
            "/var/lib/artemis-instance/etc-override/broker.xml")
        .withCopyToContainer(
            Transferable.of(keystoreBytes, 0777),
            "/var/lib/artemis-instance/etc-override/keystore.jks")
        .withCopyToContainer(
            Transferable.of(truststoreBytes, 0777),
            "/var/lib/artemis-instance/etc-override/truststore.jks")
        .withExposedPorts(AMQP_PORT, AMQPS_PORT, 8161);

    container.start();
}
```

### Inline Content Pattern

For configuration generated as strings (XML, YAML, TOML, properties):

```java
// Create config content as string
var mosquittoConf = """
    listener 1883
    allow_anonymous true
    """;

mosquitto = new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:2.0"))
    .withNetwork(createNetwork())
    .withNetworkAliases("mosquitto")
    .withExposedPorts(MQTT_PORT)
    .withCommand("mosquitto", "-c", "/mosquitto-no-auth.conf")
    .withCopyToContainer(
        Transferable.of(mosquittoConf.getBytes(StandardCharsets.UTF_8), 0777),
        "/mosquitto-no-auth.conf");
```

### Key Implementation Details

1. **Always specify 0777 permissions**: `Transferable.of(content, 0777)` ensures maximum compatibility
2. **Read files into memory**: Use `Files.readAllBytes(Path.of(path))` for binary files
3. **Convert strings to bytes**: Use `.getBytes(StandardCharsets.UTF_8)` for text content
4. **Import Transferable**: `import org.testcontainers.utility.MountableFile.Transferable;`
5. **No cleanup needed**: In-memory content is garbage collected automatically

### Pattern Variations by Use Case

#### Multiple Configuration Files (STOMP, AMQP1, WebSocket)
```java
var activeMqXml = createActiveMqConfig();
var keyStoreBytes = Files.readAllBytes(Path.of(tls.getServerKeyStoreFilePath()));
var trustStoreBytes = Files.readAllBytes(Path.of(tls.getTrustStoreFilePath()));

container
    .withCopyToContainer(Transferable.of(activeMqXml.getBytes(UTF_8), 0777), "/conf/activemq.xml")
    .withCopyToContainer(Transferable.of(keyStoreBytes, 0777), "/conf/keystore.jks")
    .withCopyToContainer(Transferable.of(trustStoreBytes, 0777), "/conf/truststore.jks");
```

#### Single Config File (MQTT E2E)
```java
var mosquittoConf = "listener 1883\nallow_anonymous true\n";

container.withCopyToContainer(
    Transferable.of(mosquittoConf.getBytes(UTF_8), 0777),
    "/mosquitto-no-auth.conf");
```

#### TLS Certificates (Pulsar, NATS, Redis)
```java
var certBytes = Files.readAllBytes(Path.of(tls.getServerCertificatePemFilePath()));
var keyBytes = Files.readAllBytes(Path.of(tls.getServerPrivateKeyPemFilePath()));
var caBytes = Files.readAllBytes(Path.of(tls.getCaCertificatePemFilePath()));

container
    .withCopyToContainer(Transferable.of(certBytes, 0777), "/certs/server.crt")
    .withCopyToContainer(Transferable.of(keyBytes, 0777), "/certs/server.key")
    .withCopyToContainer(Transferable.of(caBytes, 0777), "/certs/ca.crt");
```

### Common Pitfalls to Avoid

|  Don't | ✅ Do |
|--------|-------|
| `withFileSystemBind(hostPath, "/path", BindMode.READ_ONLY)` | `withCopyToContainer(Transferable.of(bytes, 0777), "/path")` |
| `Transferable.of(content)` without permissions | `Transferable.of(content, 0777)` |
| `withCopyToContainer(Transferable.of(bytes), "/path")` | `withCopyToContainer(Transferable.of(bytes, 0777), "/path")` |
| Create temp files on disk | Read directly into memory with `Files.readAllBytes()` |
| Use `BindMode.READ_ONLY` | Always use 0777 permissions for maximum compatibility |

### Why This Matters

- **GitHub Actions Compatibility**: Eliminates filesystem mounting issues in CI/CD
- **Cross-platform**: Identical behavior on Windows, Linux, macOS
- **Permissions**: 0777 ensures containers can read/write/execute without issues
- **Simplicity**: No temp file cleanup needed, automatic garbage collection
- **Reliability**: Consistent pattern across all test classes reduces bugs



## Troubleshooting

### Container Readiness Checks (CRITICAL)

**MANDATORY RULE**: Never use Testcontainers' built-in wait strategies (`waitingFor()`, `withStartupTimeout()`, `WaitStrategy`, `Wait.forLogMessage()`, `Wait.forHttp()`, etc.). These are fragile and inconsistent across container images.

Instead, use **Awaitility-based readiness probes** with the highest-level client available:

#### Readiness Check Hierarchy (in order of preference)

1. **SDK/native client** (best) — Proves the service protocol is working, not just the port:
   ```java
   // MQTT — Paho client connect
   var client = new MqttClient("tcp://127.0.0.1:" + mappedPort, "probe", new MemoryPersistence());
   var options = new MqttConnectOptions();
   options.setConnectionTimeout(5);
   client.connect(options);
   client.disconnect();
   client.close();
   
   // Kafka — AdminClient
   adminClient.describeCluster().clusterId().get(5, TimeUnit.SECONDS);
   
   // Azure Event Hubs — SDK consumer
   consumerClient.getPartitionIds();
   
   // AMQP 1.0 — Qpid JMS
   var factory = new JmsConnectionFactory("amqp://127.0.0.1:" + mappedPort);
   var connection = factory.createConnection();
   connection.start();
   ```

2. **HTTP client** (second) — For services with HTTP/admin endpoints:
   ```java
   // Pulsar admin endpoint
   waitForHttpReady(container, 8080, "/admin/v2/clusters");
   
   // Azurite
   waitForHttpReady(azurite, 10000, "/");
   ```

3. **Socket** (last resort only) — Only when no higher-level option exists:
   ```java
   // Nginx TLS proxy — no higher-level option available
   new Socket("127.0.0.1", mappedPort).close();
   ```

#### Readiness Probe Pattern

All readiness checks follow the same Awaitility pattern:

```java
await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
    try {
        // SDK/HTTP/Socket probe here
        return true;
    } catch (Exception e) {
        return false;
    }
});
```

### Container Lifecycle Rules

- **Always call `.start()`** before `.getMappedPort()` when managing containers manually (not using `@Container` annotation)
- **Never set `kete.enabled=true`** in test env vars — `true` is the default. Only set it to `false` when testing the disabled case.

### Container Fails to Start

- Check Docker is running
- Check for port conflicts
- Check container logs for error details

### Provider Not Loading

- Ensure `mvn compile` was run before tests
- Check the provider is correctly registered in `META-INF/services/`
- Look at Keycloak container logs:
  ```java
  System.out.println(keycloak.getLogs());
  ```

### Events Not Forwarding

- Verify the realm has the `kete` event listener enabled
- Check destination container is reachable (network aliases)
- Enable debug logging in the provider

### File Mounting Issues

- **File not accessible in container**: Ensure using `Transferable.of(bytes, 0777)` with full permissions
- **Permission denied**: Always use 0777 permissions parameter
- **GitHub Actions failures**: Never use `withFileSystemBind()`, always use `Transferable.of()`
- **Missing permissions parameter**: Verify all `Transferable.of()` calls include `0777` as second parameter



## Related Documentation

- [Testing Reference](testing.md)
- [Test Patterns and Conventions](test-patterns-and-conventions.md)
