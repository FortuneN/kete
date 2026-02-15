# Testing Guide

Complete guide to testing Keycloak Events to Everywhere.



## Test Types

### Unit Tests

**Location:** `src/test/java/io/github/fortunen/kete/unittests/`  
**Framework:** JUnit 5 + AssertJ + Mockito  
**Purpose:** Test individual components in isolation

**Run:**
```bash
mvn test
```

### Integration Tests

**Purpose:** Test extension with real message brokers  
**Uses:** Testcontainers for Kafka, RabbitMQ, MQTT, etc.

**Run:**
```bash
mvn verify
```

### End-to-End Tests

**Location:** `src/test/java/io/github/fortunen/kete/endtoendtests/`  
**Purpose:** Test full event flow from serialization to destination delivery
**Uses:** Testcontainers with actual brokers

**Run:**
```bash
mvn test -Dtest="io.github.fortunen.kete.endtoendtests.*"
```



## Running Tests

### All Tests

```bash
# Unit + Integration
mvn clean verify

# With coverage
mvn clean test jacoco:report
```

### Specific Test Class

```bash
mvn test -Dtest=io.github.fortunen.kete.provider.onEventTests
mvn test -Dtest=io.github.fortunen.kete.destinations.kafkadestination.sendTests
```

### Specific Test Method

```bash
mvn test -Dtest=io.github.fortunen.kete.provider.onEventTests#shouldProcessEventSuccessfully
```

### Skip Tests

```bash
mvn clean package -DskipTests
```



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
│   ├── runTests.java               # Tests ProviderFactory.run()
│   └── closeTests.java             # Tests ProviderFactory.close()
└── ...
```

### Test File Naming

**Pattern:** `{methodName}Tests.java`

**Examples:**
- `serializeTests.java` - Tests `serialize()` method
- `acceptTests.java` - Tests `accept()` method
- `initializeTests.java` - Tests `initialize()` method

### Overloaded Methods

Append parameter type to distinguish:

```
isEmpty_ArrayTests.java         # Tests isEmpty(Object[])
isEmpty_CollectionTests.java    # Tests isEmpty(Collection<?>)
isEmpty_StringTests.java        # Tests isEmpty(String)
```



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

### Whitespace Rules

| Location | Rule |
|----------|------|
| After method opening `{` | ONE blank line |
| After `// arrange` comment | ONE blank line |
| Before `// act` comment | ONE blank line |
| After `// act` comment | ONE blank line |
| Before `// assert` comment | ONE blank line |
| After `// assert` comment | ONE blank line |
| Before method closing `}` | ZERO blank lines |

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



## Test Configuration

### testcontainers.properties

**Location:** `src/test/resources/testcontainers.properties`

```properties
# Testcontainers configuration
testcontainers.reuse.enable=true
testcontainers.labels.project=kete
```

**Properties:**
- `reuse.enable=true` - Reuse containers across test runs
- Faster test execution
- Containers persist between runs



## Test Environment

### Start Test Infrastructure

```powershell
# Start Kafka and RabbitMQ
.\docker-infra.ps1 start

# Start Keycloak
.\docker-run.ps1

# Run tests
mvn test
```

### Clean Test Environment

```powershell
# Stop all
.\docker-infra.ps1 stop
docker stop keycloak-kete

# Clean volumes
.\docker-infra.ps1 clean
```



## Coverage Reports

### Generate Coverage

```bash
mvn clean test jacoco:report
```

### View Reports

```bash
# Open in browser
start target/site/jacoco/index.html       # Windows
open target/site/jacoco/index.html        # macOS
xdg-open target/site/jacoco/index.html    # Linux
```

### Coverage Files

```
target/
├── jacoco.exec                # Coverage data
└── site/
    └── jacoco/
        ├── index.html        # Main report
        ├── jacoco.xml        # XML format
        └── jacoco.csv        # CSV format
```



## Test Reports

### Surefire Reports

**Location:** `target/surefire-reports/`

```
surefire-reports/
├── TEST-*.xml                 # JUnit XML
└── *.txt                      # Text reports
```

### View Test Results

```powershell
# All test results
Get-ChildItem target\surefire-reports\*.txt | ForEach-Object { Get-Content $_ }

# Failed tests only
Get-Content target\surefire-reports\*.txt | Select-String "FAILURE"
```



## Writing Tests

### Unit Test Example

```java
@Test
void testConfiguration() {
    Configuration config = new Configuration();
    
    // Test destination names
    Set<String> names = config.getDestinationNames();
    assertNotNull(names);
    assertTrue(names.contains("kafka-1"));
}
```

### Integration Test Example

```java
@Testcontainers
class IntegrationTest {
    
    @Container
    static KeycloakContainer keycloak = new KeycloakContainer()
        .withProviders(Path.of("target/kete.jar"));
    
    @Test
    void testExtensionLoaded() {
        // Test extension functionality
    }
}
```

### Playwright UI Test Example

```java
@Test
void testAdminConsole() {
    page.navigate("http://localhost:7070/admin");
    page.fill("#username", "admin");
    page.fill("#password", "admin");
    page.click("button[type='submit']");
    
    // Verify extension appears
    assertTrue(page.isVisible("text=events-to-everywhere"));
}
```



## Test Data

### Mock Events

```java
Event event = new Event();
event.setType("LOGIN");
event.setRealmId("master");
event.setUserId("user-123");
event.setTime(System.currentTimeMillis());

Map<String, String> details = new HashMap<>();
details.put("username", "testuser");
event.setDetails(details);
```

### Mock Admin Events

```java
AdminEvent adminEvent = new AdminEvent();
adminEvent.setOperationType("CREATE");
adminEvent.setResourceType("USER");
adminEvent.setRealmId("master");
```



## Debugging Tests

### Debug in IDE

**IntelliJ IDEA:**
1. Right-click test class
2. Select "Debug 'TestClass'"
3. Set breakpoints as needed

**VS Code:**
```json
{
    "type": "java",
    "request": "launch",
    "mainClass": "org.junit.platform.console.ConsoleLauncher",
    "args": [
        "--select-class", "io.github.fortunen.kete.ConfigurationTest"
    ]
}
```

### Debug with Maven

```bash
mvn test -Dmaven.surefire.debug
```

Connect debugger to port 5005.

### Verbose Output

```bash
mvn test -X  # Debug output
mvn test -e  # Error details
```



## Continuous Testing

### Watch Mode

```bash
# Using Maven
mvn test -Dcontinuous

# Using IntelliJ
# Enable "Run tests automatically"
```

### Fast Feedback

```bash
# Run only modified tests
mvn test -Dsurefire.failIfNoSpecifiedTests=false

# Parallel execution
mvn test -DforkCount=4
```



## Test Troubleshooting

### Tests Fail - Containers Not Starting

**Check:**
```bash
docker ps -a
docker logs <container-name>
```

**Solution:**
```bash
# Clean and restart
.\docker-infra.ps1 clean
.\docker-infra.ps1 start
```

### Tests Fail - Port Conflicts

**Check:**
```powershell
Get-NetTCPConnection -LocalPort 7070
Get-NetTCPConnection -LocalPort 9092
```

**Solution:**
Stop conflicting services or change test ports.

### Tests Timeout

**Increase timeout:**
```java
@Test
@Timeout(value = 5, unit = TimeUnit.MINUTES)
void testLongRunning() {
    // ...
}
```

### Flaky Tests

**Common causes:**
- Timing issues
- Shared state
- Resource contention

**Solutions:**
- Add proper waits
- Isolate test data
- Use test containers



## CI/CD Integration

### GitHub Actions

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run Tests
        run: mvn clean verify
      - name: Upload Coverage
        uses: codecov/codecov-action@v3
```

### GitLab CI

```yaml
test:
  stage: test
  script:
    - mvn clean verify
  artifacts:
    reports:
      junit: target/surefire-reports/TEST-*.xml
    paths:
      - target/site/jacoco/
```
