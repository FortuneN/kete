# Integration Tests

## Table of Contents

- [Overview](#overview)
- [Running Integration Tests](#running-integration-tests)
- [Debugging Integration Tests](#debugging-integration-tests)
- [Test Categories](#test-categories)
- [Writing New Integration Tests](#writing-new-integration-tests)



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



## Troubleshooting

### Container Fails to Start

- Check Docker is running
- Check for port conflicts
- Increase container startup timeout:
  ```java
  keycloak.withStartupTimeout(Duration.ofMinutes(5));
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



## Related Documentation

- [Testing Reference](testing.md)
