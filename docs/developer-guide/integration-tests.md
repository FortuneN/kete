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

Located in: `src/tests/integration-tests/.../keycloak/CleanKeycloakBootTests.java`

Verifies basic Keycloak container functionality:
- Container starts successfully
- Admin client can connect
- Realms are accessible

### Provider Integration Tests

Located in: `src/tests/integration-tests/.../provider/EventListenerProviderIntegrationTests.java`

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

**CRITICAL**: When tests require custom configuration files for containers (broker configs, certificates, etc.), **ALWAYS use `withFileSystemBind()` with `BindMode.READ_ONLY`**. NEVER use `withCopyToContainer()`.

```java
// Step 1: Create temp directory
var tempDir = Files.createTempDirectory("test-config-");
tempDir.toFile().deleteOnExit();

// Step 2: Prepare configuration files
var configPath = tempDir.resolve("broker.xml");
Files.writeString(configPath, brokerXmlContent);
configPath.toFile().deleteOnExit();

// Step 3: Mount with READ_ONLY
@Container
static GenericContainer<?> broker = new GenericContainer<>(imageName)
    .withFileSystemBind(configPath.toString(), "/etc/broker.xml", BindMode.READ_ONLY);
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

### Why Mount Instead of Copy?

**ALWAYS use `withFileSystemBind()` for mounting files into containers. NEVER use `withCopyToContainer()`.**

| Aspect | File Mounting (`withFileSystemBind`) | File Copying (`withCopyToContainer`) |
|--------|--------------------------------------|--------------------------------------|
| **Security** | Supports `READ_ONLY` mode, preventing container writes to host | No read-only option, container can modify host files |
| **Transparency** | Files remain on host filesystem, easy to inspect | Files copied into container image layer |
| **Cleanup** | Automatic with `deleteOnExit()` | Requires manual cleanup or layer management |
| **Performance** | Direct filesystem access | Requires image layer creation |
| **Best Practice** |  **RECOMMENDED** |  **DEPRECATED in this codebase** |

### Standard Mounting Pattern

Use this pattern consistently across all TestBase classes:

```java
// Step 1: Create temporary directory
var tempDir = Files.createTempDirectory("container-config-");
tempDir.toFile().deleteOnExit();

// Step 2: Create/copy files to temp directory
var configPath = tempDir.resolve("config.xml");
Files.writeString(configPath, configContent);
configPath.toFile().deleteOnExit();

var keystorePath = tempDir.resolve("keystore.jks");
Files.copy(Path.of(sourceKeystorePath), keystorePath);
keystorePath.toFile().deleteOnExit();

// Step 3: Mount files with READ_ONLY mode
container = new GenericContainer<>(imageName)
    .withFileSystemBind(configPath.toString(), "/container/path/config.xml", BindMode.READ_ONLY)
    .withFileSystemBind(keystorePath.toString(), "/container/path/keystore.jks", BindMode.READ_ONLY);
```

### Complete Example: AMQP1 with TLS

From `io.github.fortunen.kete.integrationtests.amqp1destination.TestBase`:

```java
private void startActiveMqArtemisWithTls(TlsMaterial tls, boolean requireClientAuth) throws Exception {
    
    // Create broker configuration XML
    var brokerXml = createArtemisBrokerXml(
        tls.getKeyStorePassword(),
        tls.getTrustStorePassword(),
        requireClientAuth
    );

    // Create temp directory for all config files
    var tempDir = Files.createTempDirectory("artemis-tls-");
    tempDir.toFile().deleteOnExit();

    // Write broker.xml configuration
    var brokerXmlPath = tempDir.resolve("broker.xml");
    Files.writeString(brokerXmlPath, brokerXml);
    brokerXmlPath.toFile().deleteOnExit();

    // Copy keystore and truststore to temp directory
    var keyStorePath = tempDir.resolve("keystore.jks");
    Files.copy(Path.of(tls.getServerKeyStoreFilePath()), keyStorePath);
    keyStorePath.toFile().deleteOnExit();

    var trustStorePath = tempDir.resolve("truststore.jks");
    Files.copy(Path.of(tls.getTrustStoreFilePath()), trustStorePath);
    trustStorePath.toFile().deleteOnExit();

    // Mount all files with READ_ONLY mode
    container = new GenericContainer<>(DockerImageName.parse("apache/activemq-artemis:2.40.0-alpine"))
        .withEnv("ARTEMIS_USER", DEFAULT_USERNAME)
        .withEnv("ARTEMIS_PASSWORD", DEFAULT_PASSWORD)
        .withEnv("ANONYMOUS_LOGIN", "true")
        .withFileSystemBind(brokerXmlPath.toString(), 
            "/var/lib/artemis-instance/etc-override/broker.xml", 
            BindMode.READ_ONLY)
        .withFileSystemBind(keyStorePath.toString(), 
            "/var/lib/artemis-instance/etc-override/keystore.jks", 
            BindMode.READ_ONLY)
        .withFileSystemBind(trustStorePath.toString(), 
            "/var/lib/artemis-instance/etc-override/truststore.jks", 
            BindMode.READ_ONLY)
        .withExposedPorts(AMQP_PORT, AMQPS_PORT, 8161)
        .waitingFor(Wait.forLogMessage(".*AMQ221007.*", 1))
        .withStartupTimeout(Duration.ofMinutes(10));

    container.start();
}
```

### Inline Temporary File Pattern

For simple single-file configurations in E2E tests:

```java
// Create temp file for mosquitto config
var tempConfigPath = Files.createTempFile("mosquitto-", ".conf");
Files.writeString(tempConfigPath, "listener 1883\nallow_anonymous true\n");
tempConfigPath.toFile().deleteOnExit();

mosquitto = new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:2.0"))
    .withNetwork(createNetwork())
    .withNetworkAliases("mosquitto")
    .withExposedPorts(MQTT_PORT)
    .withCommand("mosquitto", "-c", "/mosquitto-no-auth.conf")
    .withFileSystemBind(tempConfigPath.toString(), 
        "/mosquitto-no-auth.conf", 
        BindMode.READ_ONLY);
```

### Key Implementation Details

1. **Always use `BindMode.READ_ONLY`**: Prevents containers from modifying host files
2. **Call `deleteOnExit()` on both directory and files**: Ensures cleanup after JVM shutdown
3. **Use absolute paths**: `toString()` converts `Path` to absolute string path
4. **Group related files in one temp directory**: Easier management and cleanup
5. **Mount early in container builder chain**: Before `withExposedPorts()`, `withCommand()`, etc.

### Pattern Variations by Use Case

#### Multiple Configuration Files (STOMP, AMQP1, WebSocket)
```java
var tempDir = Files.createTempDirectory("prefix-");
tempDir.toFile().deleteOnExit();

var file1 = tempDir.resolve("config.xml");
var file2 = tempDir.resolve("keystore.jks");
var file3 = tempDir.resolve("truststore.jks");

// Write/copy files...
// Mount all with withFileSystemBind()
```

#### Single Config File (MQTT E2E)
```java
var tempConfigPath = Files.createTempFile("mosquitto-", ".conf");
Files.writeString(tempConfigPath, configContent);
tempConfigPath.toFile().deleteOnExit();

container.withFileSystemBind(tempConfigPath.toString(), "/path", BindMode.READ_ONLY);
```

#### Dynamic Configuration from TLS Material (Pulsar, AMQP1, STOMP)
```java
var brokerConfig = createBrokerConfig(
    tls.getKeyStorePassword(),
    tls.getTrustStorePassword(),
    requireClientAuth
);
Files.writeString(hostBrokerConf, brokerConfig);
```

### Common Pitfalls to Avoid

|  Don't | ✅ Do |
|--------|-------|
| `withCopyToContainer(Transferable, "/path")` | `withFileSystemBind(hostPath, "/path", BindMode.READ_ONLY)` |
| Skip `deleteOnExit()` calls | Always call `deleteOnExit()` on files and directories |
| Use relative paths | Always use absolute paths (`.toString()` on `Path`) |
| Mount without `BindMode` parameter | Always specify `BindMode.READ_ONLY` |
| Create files in random locations | Use `Files.createTempDirectory()` for organization |

### Why This Matters

- **Security**: READ_ONLY mounts prevent containers from tampering with host files
- **Reliability**: Consistent pattern across all test classes reduces bugs
- **Cross-platform**: Works identically on Windows, Linux, macOS
- **Testcontainers Best Practice**: Aligned with Testcontainers recommended patterns
- **Code Review**: Easy to verify correct implementation



## Troubleshooting

### Container Fails to Start

- Check Docker is running
- Check for port conflicts
- Increase container startup timeout:
  ```java
  keycloak.withStartupTimeout(Duration.ofMinutes(10));
  ```

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

- **File not found in container**: Verify absolute path is used (`.toString()` on Path)
- **Permission denied**: Check file permissions on host, ensure READ_ONLY is appropriate
- **Container can't read file**: Ensure file exists before `container.start()`
- **Temp files persist**: Verify `deleteOnExit()` called on both files and directories



## Related Documentation

- [Testing Reference](testing.md)
- [Test Patterns and Conventions](test-patterns-and-conventions.md)
