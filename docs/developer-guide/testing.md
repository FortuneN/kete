# Testing Guide

Complete guide to testing Keycloak Events to Everywhere.



## Test Types

All tests live under `src/test/java/io/github/fortunen/kete/` and are run by Maven Surefire. There is no Failsafe plugin and no include filter, so a bare `mvn test` runs **all three categories** (unit, integration and end-to-end — the latter two need Docker). Use the `-Dtest` package filters below, or the `run-*.ps1` scripts that wrap them.

### Unit Tests

**Location:** `src/test/java/io/github/fortunen/kete/unittests/`  
**Framework:** JUnit 5 + AssertJ + Mockito  
**Purpose:** Test individual components in isolation — zero I/O (no containers, sockets, servers or Docker)

**Run:**
```bash
mvn test -Dtest="io.github.fortunen.kete.unittests.**"
# or
.\run-unit-tests.ps1
```

### Integration Tests

**Location:** `src/test/java/io/github/fortunen/kete/integrationtests/<destination>destination/`  
**Purpose:** Exercise each `Destination` class directly against a real broker, emulator or target server started with Testcontainers (no Keycloak involved)  
**Files:** `TestBase.java` (container lifecycle, TLS/nginx helpers, verification helpers) + `sendTests.java` (`shouldSend_NonTls`, `shouldSend_Tls`, `shouldSend_mTls`), plus `isHealthyTests.java` for the probe-bearing destinations (broker-outage resilience)

**Run:**
```bash
mvn test -Dtest="io.github.fortunen.kete.integrationtests.**"
# or
.\run-integration-tests.ps1
```

See [Integration Tests](integration-tests.md) for the conventions.

### End-to-End Tests

**Location:** `src/test/java/io/github/fortunen/kete/endtoendtests/`  
**Purpose:** Full pipeline — the shaded `target/kete.jar` is deployed into a real Keycloak container (`quay.io/keycloak/keycloak:26.0.0` by default; `-Dkeycloak.version=26.7.2` selects another release), a user login is triggered (password grant through the Keycloak admin-client library), and the event is read back from the destination container  
**Uses:** `KeycloakContainer` (`testcontainers-keycloak`) + broker/emulator containers on a shared network; the base class builds the jar with `mvn package -DskipTests` at the start of every end-to-end JVM (about a minute)

**Run:**
```bash
mvn test -Dtest="io.github.fortunen.kete.endtoendtests.**"
# or
.\run-end-to-end-tests.ps1
```



## Running Tests

### All Tests

```bash
# Unit → Integration → E2E, stopping at the first failing category
.\run-all-tests.ps1
```

### Specific Test Class

```bash
mvn test -Dtest=io.github.fortunen.kete.unittests.provider.onEventTests
mvn test -Dtest=io.github.fortunen.kete.unittests.destinations.kafkadestination.sendTests
```

### Specific Test Method

```bash
mvn test -Dtest="io.github.fortunen.kete.unittests.provider.onEventTests#should*"
```

### Skip Tests

```bash
mvn clean package -DskipTests
```

All scripts pass `-Dsurefire.skipAfterFailureCount=1`, so a run stops at the first failure.



## Test Organization

### One Method Per File Pattern

Each test file tests exactly ONE method from the source class:

```
src/test/java/io/github/fortunen/kete/unittests/
├── provider/
│   ├── constructorTests.java       # Tests Provider constructor
│   ├── onEventTests.java           # Tests Provider.onEvent()
│   ├── onAdminEventTests.java      # Tests Provider.onAdminEvent()
│   └── closeTests.java             # Tests Provider.close()
├── providerfactory/
│   ├── initTests.java              # Tests ProviderFactory.init()
│   ├── postInitTests.java          # Tests ProviderFactory.postInit()
│   └── closeTests.java             # Tests ProviderFactory.close()
└── ...
```

### Test File Naming

**Pattern:** `{methodName}Tests.java`

**Examples:**
- `serializeTests.java` - Tests `serialize()` method
- `acceptRealmTests.java` - Tests `acceptRealm()` method
- `initializeTests.java` - Tests `initialize()` method

### Overloaded Methods

Append parameter type to distinguish:

```
isEmpty_ArrayTests.java         # Tests isEmpty(Object[])
isEmpty_CollectionTests.java    # Tests isEmpty(Collection<?>)
isEmpty_StringTests.java        # Tests isEmpty(String)
```

The full rule set (AAA layout, whitespace, naming, assertions, mocking) is in [Test Patterns and Conventions](test-patterns-and-conventions.md).



## Test Structure (AAA Pattern)

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

### Exception Testing

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



## Test Environment

Integration and E2E tests manage their own infrastructure with Testcontainers: every container is started by the test (`TestBase` / `EndToEndTestBase`), exposed on a random mapped port and removed by Ryuk afterwards. The only prerequisite is a running Docker daemon; nothing needs to be started or cleaned up by hand, and no fixed host ports are used.

Container readiness is verified with Awaitility probes using the highest-level client available (SDK client → HTTP → socket) rather than Testcontainers wait strategies — see [Integration Tests](integration-tests.md#container-readiness-checks).



## Coverage Reports

JaCoCo is bound to the `test` phase, so every test run produces a report (XML, CSV and HTML):

```bash
mvn test -Dtest="io.github.fortunen.kete.unittests.**"
```

```
target/
├── jacoco.exec                # Coverage data
└── site/
    └── jacoco/
        ├── index.html        # Main report
        ├── jacoco.xml        # XML format
        └── jacoco.csv        # CSV format (source of the README coverage badge)
```

`run-coverage-badge.ps1` turns the execution data into `coverage-badge.json` — locally from the single `target/jacoco.exec` (called by `run-on-develop-push.ps1`), in CI from the three shard files merged together.



## Test Reports

### Surefire Reports

**Location:** `target/surefire-reports/`

```
surefire-reports/
├── TEST-*.xml                 # JUnit XML
└── *.txt                      # Text reports
```

```powershell
# Failed tests only
Get-Content target\surefire-reports\*.txt | Select-String "FAILURE"
```



## Writing Tests

### Unit Test Example (destination, zero I/O)

```java
@Test
public void shouldPublishToSubject() {

    // arrange

    var connection = mock(Connection.class);
    var destination = new NatsDestination();
    destination.setConnection(connection);
    destination.setSubject("keycloak.events");
    destination.setSubjectTemplated(false);
    destination.setCustomHeadersEntrySet(Set.of());
    var message = new EventMessage("master", "evt-1", "{}".getBytes(UTF_8), "LOGIN", "application/json", null, Constants.EVENT, null, Constants.SUCCESS);

    // act

    destination.doSend(message);

    // assert

    verify(connection).publish(eq("keycloak.events"), any(Headers.class), eq("{}".getBytes(UTF_8)));
}
```

### E2E Test Skeleton

```java
public class MyDestinationE2ETests extends EndToEndTestBase {

    @Test
    public void shouldDeliverLoginEvent() throws Exception {

        // arrange — start the broker on the shared network, then Keycloak with the route configured

        var broker = new GenericContainer<>("my/broker:1.0").withNetwork(createNetwork()).withNetworkAliases("broker");
        broker.start();
        var keycloak = createKeycloakContainer(Map.of(
            "kete.routes.e2e.destination.kind", "my-destination",
            "kete.routes.e2e.destination.host", "broker"));
        keycloak.start();

        // act

        triggerLoginEvent(keycloak);

        // assert — read the event back from the broker
    }
}
```

`EndToEndTestBase` creates the `KeycloakContainer` from `KEYCLOAK_IMAGE` with `withProviderLibsFrom(List.of(new File("target/kete.jar")))` and passes the route configuration as environment variables.



## Test Data

### Mock Events

```java
var event = new Event();
event.setType(EventType.LOGIN);
event.setRealmId("master");
event.setUserId("user-123");
event.setTime(System.currentTimeMillis());
event.setDetails(Map.of("username", "testuser"));
```

### Mock Admin Events

```java
var adminEvent = new AdminEvent();
adminEvent.setOperationType(OperationType.CREATE);
adminEvent.setResourceTypeAsString("USER");
adminEvent.setRealmId("master");
```



## Debugging Tests

### Debug in IDE

Right-click the test class and choose Debug; every test class is a plain JUnit 5 class.

### Debug with Maven

```bash
mvn test -Dmaven.surefire.debug -Dtest=io.github.fortunen.kete.unittests.provider.onEventTests
```

Connect a debugger to port 5005.

### Verbose Output

```bash
mvn test -X  # Debug output
mvn test -e  # Error details
```

When running container-based tests, follow the Docker output while they run (`docker ps`, `docker logs <container>`) rather than waiting for a timeout.



## Test Troubleshooting

### Tests Fail - Containers Not Starting

```bash
docker ps -a
docker logs <container-name>
```

Check that Docker is running and has enough memory; broker containers in the outage-resilience tests are memory-capped.

### Tests Timeout

Readiness probes wait up to several minutes for slow images (first pull). Re-run after the image is cached; do not add fixed sleeps.

### Flaky Tests

**Common causes:** timing assumptions, shared state between tests, host resource contention.

**Solutions:** use Awaitility probes for every wait, keep tests independent (no shared fixtures), and verify delivery through the destination's own API.



## CI/CD Integration

GitHub Actions runs the PowerShell pipelines (see `.github/workflows/`):

| Workflow | Trigger | Script | What runs |
|----------|---------|--------|-----------|
| `pull-request.yml` | PR to `develop` (a release pull request carries a tree the Develop workflow has already tested; `release.yml` re-checks that before publishing) | `run-unit-tests.ps1`, `run-integration-tests.ps1`, `run-end-to-end-tests.ps1` in parallel jobs, plus a `validate` job (package, `run-jar-check.ps1`, quick-start image, `mkdocs build --strict`); the final `Build & Test` job needs all of them |
| `develop.yml` | Push to `develop` | Same four parallel jobs; each test job uploads its `jacoco.exec`, and the final `Build & Test` job merges them with `run-coverage-badge.ps1` and uploads the `coverage-badge` artifact |
| `quick-starts.yml` | Weekly (Monday 03:00 UTC) and manual dispatch | `run-quick-starts.ps1` boots every local quick-start against the published `:latest` images, triggers a login and checks that the event reached the destination (stops at the first failing quick-start; optional `filter` input) |
| `release.yml` | Push to `release` | `run-on-release-push.ps1` | `Verify Develop Run` job (refuses to release unless a successful Develop run tested an identical tree; manual dispatch offers `skip-develop-check`) → versioned JAR → versioned Docker push → docs build → tag + GitHub Release (generated notes) → `:latest` tags → docs + coverage badge deploy (tests are not re-run) |

See [Scripts](scripts/overview.md) for details.

All workflows pin their actions to commit SHAs, pin the MkDocs packages, set `timeout-minutes`, and cancel superseded runs of the same branch or pull request (`concurrency`). `.github/dependabot.yml` keeps the Maven dependencies (minor and patch updates grouped; Keycloak major/minor bumps ignored because the compile target is pinned deliberately) and the pinned actions current.
