# Integration Tests

## Table of Contents

- [Overview](#overview)
- [Running Integration Tests](#running-integration-tests)
- [Test Layout](#test-layout)
- [Writing New Integration Tests](#writing-new-integration-tests)
- [Container File Mounting Best Practices](#container-file-mounting-best-practices)
- [Container Readiness Checks](#container-readiness-checks)
- [Debugging](#debugging)
- [Troubleshooting](#troubleshooting)



## Overview

Integration tests exercise each `Destination` implementation **directly** — the test creates the destination config and instance in the JVM, points it at a real broker, emulator or target server started with [Testcontainers](https://www.testcontainers.org/), sends an `EventMessage`, and reads the message back through the destination system's own API. Keycloak is not involved; the full Keycloak → KETE → broker pipeline is covered by the [end-to-end tests](testing.md#end-to-end-tests).

### Prerequisites

- Docker must be running (containers are pulled on first use)
- Maven 3.9+ and JDK 21



## Running Integration Tests

```bash
# All integration tests (stops at the first failure)
.\run-integration-tests.ps1
# equivalent to
mvn clean test -Dtest="io.github.fortunen.kete.integrationtests.**" -Dsurefire.skipAfterFailureCount=1

# One destination
mvn test -Dtest="io.github.fortunen.kete.integrationtests.kafkadestination.**"

# Unit tests only
mvn test -Dtest="io.github.fortunen.kete.unittests.**"
```

There is no Failsafe plugin and no Surefire include filter: a bare `mvn test` runs unit, integration **and** end-to-end tests. Always follow the Docker output (`docker ps`, `docker logs <container>`) while container tests run instead of waiting for a timeout.



## Test Layout

One package per destination under `src/test/java/io/github/fortunen/kete/integrationtests/`:

```
integrationtests/<destination>destination/
    TestBase.java        ← container lifecycle, TLS/nginx helpers, configureDestination(), verification helpers
    sendTests.java       ← shouldSend_NonTls, shouldSend_Tls, shouldSend_mTls
    isHealthyTests.java  ← broker-outage resilience (probe-bearing destinations only)
```

- **`sendTests.java`** — exactly three send tests per destination: plain, TLS (server authentication) and mTLS (mutual authentication). Destinations whose protocol has no TLS (ZeroMQ uses CurveZMQ) replace the TLS pair with protocol-specific variants (`shouldSend_PushPull`, `shouldSend_PublishSubscribe`, `shouldSend_PublishSubscribeWithEnvelope`, `shouldSend_CurveSecurity`, `shouldSend_ConnectMode`).
- **`isHealthyTests.java`** — present for the eight destinations with a live connection probe (`amqp091`, `mqtt3`, `mqtt5`, `nats`, `natsjetstream`, `pulsar`, `redispubsub`, `redisstream`): pauses/stops the broker, asserts `isHealthy()` turns false and the pool recovers after the broker returns.
- No other integration files (`initializeTests`, `closeTests`, extra send variants) — container tests are expensive.

### Real emulator over mocks

If an official emulator exists for a service it must be used (Azurite for Azure Storage Queue, `google/cloud-sdk:emulators` for GCP Pub/Sub, LocalStack for AWS, …). MockWebServer/MockServer is acceptable only when it *is* the target (HTTP, SOAP, Cloud Tasks target URL). Delivery is always verified by reading the message back through the destination system's native API, never through request recording alone.

### Nginx TLS proxy for emulators

Emulators rarely speak TLS. For the TLS and mTLS tests an `nginx:1.27-alpine` reverse proxy is placed in front of the emulator on a shared Docker network; nginx terminates TLS (and requires a client certificate for mTLS) and forwards plain HTTP to the emulator. The NonTls test talks to the emulator directly. Reference implementations: `integrationtests/gcppubsubdestination/`, `integrationtests/gcpcloudtasksdestination/`, `integrationtests/grpcdestination/`.



## Writing New Integration Tests

### Skeleton

```java
public class sendTests extends TestBase {

    @Test
    public void shouldSend_NonTls() throws Exception {

        // arrange

        startBroker();                                   // TestBase: starts the container, waits for readiness
        var config = configureDestination(Map.of(        // TestBase: builds MyDestinationConfig from a map
            "host", "127.0.0.1",
            "port", String.valueOf(getMappedPort())));
        var destination = new MyDestination();
        destination.setConfig(config);
        destination.initialize();                        // always initialize before send
        var message = new EventMessage("test-realm", "evt-001", "{}".getBytes(UTF_8), "LOGIN", "application/json", null, Constants.EVENT, null, Constants.SUCCESS);

        // act

        destination.send(message);

        // assert — read the message back with the broker's own client

        assertThat(consumeOne()).isEqualTo("{}");

        // cleanup

        destination.close();
    }
}
```

`TestBase` typically exposes `startWithServerOnlyTLS(TlsMaterial)` / `startWithClientAndServerTLS(TlsMaterial)` for the TLS variants; the test certificates come from `TlsMaterial.builder().withServerHostNames(new String[] { "localhost", "127.0.0.1", ... })`, which generates a CA, server and client certificate in memory and writes them to temp files.

### Mounting Configuration Files

**CRITICAL**: When tests require custom configuration files for containers (broker configs, certificates, etc.), **ALWAYS use in-memory `Transferable.of()` with 0777 permissions**. NEVER use `withFileSystemBind()` or `withCopyToContainer()` without permissions.

```java
// Step 1: Read file content into memory
var brokerXmlBytes = Files.readAllBytes(Path.of(brokerXmlPath));

// Step 2: Copy to container memory with full permissions
container = new GenericContainer<>(imageName)
    .withCopyToContainer(Transferable.of(brokerXmlBytes, 0777), "/etc/broker.xml");
```

See [Container File Mounting Best Practices](#container-file-mounting-best-practices) for complete details.

### Images used

Pinned image tags are the convention (no `:latest`). Examples in use: `rabbitmq:3.13-management`, `apache/kafka:3.8.0`, `hivemq/hivemq-ce:2024.3`, `eclipse-mosquitto:2.0`, `redis:7-alpine`, `apache/activemq-artemis:2.40.0-alpine`, `nginx:1.27-alpine`, `mcr.microsoft.com/azure-storage/azurite`, `google/cloud-sdk:emulators`.



## Container File Mounting Best Practices

### Why In-Memory Transfer with Full Permissions?

**ALWAYS use `withCopyToContainer(Transferable.of(bytes, 0777))` for mounting files into containers. NEVER use `withFileSystemBind()`.**

| Aspect | In-Memory Transfer (`Transferable.of()`) | File System Bind (`withFileSystemBind`) |
|--------|------------------------------------------|----------------------------------------|
| **GitHub Actions** | ✅ Works reliably in CI/CD | ❌ Fails due to filesystem limitations |
| **Permissions** | Full control with 0777 parameter | Unpredictable based on host OS |
| **Cross-platform** | Identical behavior on all OS | Different behavior Windows/Linux/macOS |
| **Performance** | Fast in-memory copy | Filesystem mount overhead |
| **Best Practice** | **REQUIRED in this codebase** | **FORBIDDEN** |

### Standard In-Memory Transfer Pattern

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
4. **Import Transferable**: `import org.testcontainers.images.builder.Transferable;`
5. **No cleanup needed**: In-memory content is garbage collected automatically

### Common Pitfalls to Avoid

| Don't | Do |
|-------|----|
| `withFileSystemBind(hostPath, "/path", BindMode.READ_ONLY)` | `withCopyToContainer(Transferable.of(bytes, 0777), "/path")` |
| `Transferable.of(content)` without permissions | `Transferable.of(content, 0777)` |
| Create temp files on disk for container input | Read directly into memory with `Files.readAllBytes()` |



## Container Readiness Checks

Testcontainers' built-in wait strategies (`waitingFor()`, `withStartupTimeout()`, `Wait.forLogMessage()`, `Wait.forHttp()`) are avoided because they are fragile and inconsistent across images. Readiness is established with **Awaitility probes** using the highest-level client available. The only exceptions are the MQTT and Pulsar broker-outage tests, which watch a broker log line to detect the restart before probing.

### Readiness Check Hierarchy (in order of preference)

1. **SDK/native client** (best) — proves the service protocol is working, not just the port:
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

   // AMQP 0.9.1 — RabbitMQ client
   try (var connection = factory.newConnection()) { }
   ```

2. **HTTP client** (second) — for services with HTTP/admin endpoints:
   ```java
   var response = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/admin/v2/clusters")).GET().build(), HttpResponse.BodyHandlers.discarding());
   return response.statusCode() == 200;
   ```

3. **Socket** (last resort only) — only when no higher-level option exists (nginx TLS proxies):
   ```java
   new Socket("127.0.0.1", mappedPort).close();
   ```

### Readiness Probe Pattern

```java
await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
    try {
        // SDK/HTTP/Socket probe here
        return true;
    } catch (Exception e) {
        return false;
    }
});
```

### Container Lifecycle Rules

- **Always call `.start()`** before `.getMappedPort()` when managing containers manually (not using `@Container`)
- **Always call `destination.initialize()`** before `destination.send()` — initialization opens the connection and surfaces configuration errors early
- **Never set `kete.enabled=true`** in test configuration — `true` is the default; only set `false` when testing the disabled case



## Debugging

Integration tests run in the Maven JVM, so a normal IDE debugger works: run the test class in debug mode or use `mvn test -Dmaven.surefire.debug -Dtest=...` and attach to port 5005. Container logs are available with `docker logs <container>` while the test runs, or programmatically with `container.getLogs()`.

For debugging the extension *inside Keycloak*, use the end-to-end tests: `KeycloakContainer` from `testcontainers-keycloak` supports `.withDebugFixedPort(5005, true)` (suspend until a debugger attaches) on the container created by `EndToEndTestBase.createKeycloakContainer(...)`.



## Troubleshooting

### Container Fails to Start

- Check Docker is running and has enough memory (outage tests cap broker memory)
- Check container logs for error details

### Readiness Probe Times Out

- First run pulls the image; re-run once it is cached
- Confirm the probe targets the mapped port of a started container

### Events Not Verified

- Confirm the verification client reads from the same topic/queue/subject the destination was configured with (templated names substitute `${realmLowerCase}` etc.)
- Check the destination was initialized before sending

### File Mounting Issues

- **File not accessible in container**: ensure `Transferable.of(bytes, 0777)` is used
- **GitHub Actions failures**: never use `withFileSystemBind()`



## Related Documentation

- [Testing Guide](testing.md)
- [Test Patterns and Conventions](test-patterns-and-conventions.md)
