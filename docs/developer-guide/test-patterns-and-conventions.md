# Test Patterns and Conventions

> **Analysis Date:** January 13, 2026  
> **Updated:** January 28, 2026 (Added ExecutorUtils tests, renamed serializer folders)
> **Total Unit Test Files Analyzed:** 247  
> **Framework:** JUnit 5 (Jupiter) + AssertJ + Mockito



##  CRITICAL RULE: ONE METHOD, ONE TEST FILE

**ABSOLUTE REQUIREMENT:** Each test file MUST test exactly ONE method (with permuted input and context setups fo vaious scenarios).

###  CORRECT Examples:
- `getSslContextTests.java` - Tests ONLY the `getSslContext()` method
- `buildDefaultTests.java` - Tests ONLY the `buildDefault()` static method
- `buildTests.java` - Tests ONLY the `build()` method of a builder class
- `closeTests.java` - Tests ONLY the `close()` method
- `serializeTests.java` - Tests ONLY the `serialize()` method

###  INCORRECT Examples (NEVER DO THIS):
- `sslContextTests.java` - Too generic, doesn't specify method name
- `builderTests.java` - Too generic, builder is a method not a concept
- `edgeCasesTests.java` - Too generic, edge cases belong in method-specific files
- `utilityMethodsTests.java` - Testing multiple methods in one file
- `helperTests.java` - Too vague, no specific method

###  Rule Application:
1. **Find the method name** in the source code
2. **Create test file:** `{methodName}Tests.java` (camelCase)
3. **All tests in that file** test ONLY that method
4. **Different scenarios** for the same method go in the SAME file


###  DO NOT TEST: Constants, Abstract Classes, or Lombok-Generated Code

**RULE:** The following elements should NOT have dedicated test files:

| Element | Reason |
|---------|--------|
| **Static constants/fields** (e.g., `MIN_POOL_SIZE`, `MAX_POOL_SIZE`) | Values are self-evident; no behavior to test |
| **Abstract classes directly** | Test concrete implementations instead; each impl may have different behavior |
| **Lombok-generated constructors** (`@NoArgsConstructor`, `@AllArgsConstructor`, `@RequiredArgsConstructor`) | Auto-generated code with no custom logic |
| **Lombok-generated getters/setters** (`@Data`, `@Getter`, `@Setter`) | Auto-generated code with no custom logic |

**EXCEPTION:** Test methods that are manually written and contain validation or business logic:
```java
// This constructor SHOULD be tested - it has validation logic
public Provider(EventListenerTransaction transaction) {
    this.transaction = ValidationUtils.requireNonNull(transaction, "transaction is required");
}

// This setter SHOULD be tested - it has validation logic
public void setConfiguration(MapConfiguration configuration) {
    this.configuration = ValidationUtils.requireNonNull(configuration, "configuration is required");
    this.pattern = ValidationUtils.requireNonBlank(configuration.getString(PATTERN, ""), "pattern is required").trim();
}

// This getter SHOULD be tested - it has custom logic
@Override
public String getId() { return Constants.ID; }
```



##  CRITICAL RULE: INHERITED METHODS IN IMPLEMENTATION TESTS

**ABSOLUTE REQUIREMENT:** Inherited methods MUST be tested as part of each implementation's test folder, NOT in a separate parent-level test file.

###  CORRECT Examples:
```
matchers/globmatcher/acceptTests.java          ← Tests accept() inherited from Matcher
matchers/globmatcher/setConfigurationTests.java ← Tests setConfiguration() (manually written with validation)
matchers/listmatcher/acceptTests.java          ← Same inherited method, tested per implementation
```

###  INCORRECT Examples (NEVER DO THIS):
```
matchers/acceptTests.java                      ← WRONG: Testing inherited method at parent level
serializers/getContentTypeTests.java           ← WRONG: Testing all serializers in one file
destinations/setConfigurationTests.java        ← WRONG: Parent-level test for inherited method
```

###  Rule Application:
1. **Identify inherited methods** in base class (e.g., `Matcher.accept()`, `Serializer.getContentType()`)
2. **Create test file in each implementation folder** (e.g., `globmatcher/acceptTests.java`)
3. **Test the inherited behavior** using the concrkete implementation
4. **Each implementation gets its own test file** for the inherited method
5. **No parent-level test files** for methods that are inherited by implementations

###  Rationale:
- Coverage is reported per-class, so inherited methods must be exercised through each subclass
- Each implementation may have different setup/configuration requirements
- Ensures inherited behavior works correctly with each implementation's specific state



## 1. Directory Structure

### 1.1 Root Test Directory

```
src/tests/unit-tests/java/io/github/fortunen/kete/
```

### 1.2 Package Organization Pattern

**RULE:** Test packages mirror the main source class location, with an additional folder named after the class being tested (in lowercase).

```
Main Source:                                    Test Location:
src/main/.../ProviderFactory.java       →       src/tests/unit-tests/.../providerfactory/
src/main/.../Provider.java              →       src/tests/unit-tests/.../provider/
src/main/.../Configuration.java         →       src/tests/unit-tests/.../configuration/configuration/
src/main/.../Route.java                 →       src/tests/unit-tests/.../routes/route/
src/main/.../utils/ValidationUtils.java →       src/tests/unit-tests/.../utils/validationutils/
src/main/.../utils/RouteUtils.java      →       src/tests/unit-tests/.../routes/routeutils/
src/main/.../destinations/HttpDestination.java → src/tests/unit-tests/.../destinations/httpdestination/
src/main/.../serializers/JsonSerializer.java   → src/tests/unit-tests/.../serializers/jsonserializer/
src/main/.../matchers/GlobMatcher.java          → src/tests/unit-tests/.../matchers/globmatcher/
```

### 1.3 Folder Naming Rules

| Element | Naming Pattern |
|---------|----------------|
| Top-level category folders | lowercase plural (e.g., `destinations/`, `serializers/`, `matchers/`, `utils/`, `routes/`) |
| Class-under-test folder | lowercase class name (e.g., `httpdestination/`, `jsonserializer/`, `globmatcher/`) |
| Utility class folder | lowercase class name (e.g., `validationutils/`, `templateutils/`, `retryutils/`) |

### 1.4 Complete Folder Structure

```
src/tests/unit-tests/java/io/github/fortunen/kete/
├── configuration/
│   ├── configuration/
│   │   └── constructorTests.java
│   └── configurationutils/
│       ├── constructorTests.java
│       └── getConfigurationTests.java
├── destinations/
│   ├── amqp091destination/
│   ├── amqp1destination/
│   ├── httpdestination/
│   ├── kafkadestination/
│   ├── mqtt3destination/
│   └── mqtt5destination/
├── matchers/
│   ├── globmatcher/
│   ├── listmatcher/
│   ├── regexmatcher/
│   └── sqlmatcher/
├── provider/
├── providerfactory/
├── retries/
│   └── retryutils/
├── routes/
│   ├── route/
│   └── routeutils/
├── serializers/
│   ├── cborserializer/
│   ├── csvserializer/
│   ├── jsonserializer/
│   ├── propertiesserializer/
│   ├── smileserializer/
│   ├── tomlserializer/
│   ├── xmlserializer/
│   └── yamlserializer/
└── utils/
    ├── certificateutils/
    ├── configurationutils/
    ├── destinationutils/
    ├── executorutils/
    ├── fileutils/
    ├── iocutils/
    ├── matcherutils/
    ├── metricsutils/
    ├── retryutils/
    ├── routeutils/
    ├── serializerutils/
    ├── templateutils/
    └── validationutils/
```



## 2. File Naming

### 2.1 Pattern

**RULE:** `{methodName}Tests.java` (camelCase, suffix `Tests`)

### 2.2 Examples

| Method Being Tested | File Name |
|---------------------|-----------|
| `close()` | `closeTests.java` |
| `constructor` | `constructorTests.java` |
| `serialize()` | `serializeTests.java` |
| `accept()` | `acceptTests.java` |
| `setConfiguration()` | `setConfigurationTests.java` |
| `getConfiguration()` | `getConfigurationTests.java` |
| `createEventExecutor()` | `createEventExecutorTests.java` |
| `shutdownExecutor()` | `shutdownExecutorTests.java` |
| `getSslContext()` | `getSslContextTests.java` |
| `requireNonNull()` | `requireNonNullTests.java` |
| `tryParseInt()` | `tryParseIntTests.java` |
| `isEmpty()` for Array | `isEmpty_ArrayTests.java` |
| `isEmpty()` for Collection | `isEmpty_CollectionTests.java` |
| `requireGreaterThan()` for int | `requireGreaterThan_intTests.java` |
| `requireGreaterThan()` for long | `requireGreaterThan_longTests.java` |

### 2.3 Overloaded Method Naming

**RULE:** For overloaded methods, append `_{paramketerType}` or `_{distinguisher}` to the file name.

```
isEmpty(Object[])      → isEmpty_ArrayTests.java
isEmpty(byte[])        → isEmpty_ByteArrayTests.java
isEmpty(Collection<?>)  → isEmpty_CollectionTests.java
isEmpty(Map<?,?>)      → isEmpty_MapTests.java
isEmpty(String)        → isEmpty_StringTests.java
requireInRange(int)    → requireInRange_intTests.java
requireInRange(long)   → requireInRange_longTests.java
```



## 3. Package Declaration

### 3.1 Pattern

**RULE:** Package name matches the folder structure exactly, with class-under-test folder name matching folder case.

### 3.2 Standard Examples

```java
// For providerfactory/closeTests.java
package io.github.fortunen.kete.providerfactory;

// For routes/route/acceptTests.java
package io.github.fortunen.kete.routes.route;

// For utils/validationutils/requireNonNullTests.java
package io.github.fortunen.kete.utils.validationutils;

// For serializers/jsonserializer/serializeTests.java
package io.github.fortunen.kete.serializers.jsonserializer;

// For destinations/httpdestination/setConfigurationTests.java
package io.github.fortunen.kete.destinations.httpdestination;
```

### 3.3 Utility Class Package Convention

**RULE:** All utility test packages use lowercase subfolder names matching the convention:

```java
// For retries/retryutils/getRetryTests.java
package io.github.fortunen.kete.unittests.retries.retryutils;

// For routes/routeutils/getRouteTests.java  
package io.github.fortunen.kete.unittests.routes.routeutils;

// For matchers/matcherutils/getMatchersTests.java
package io.github.fortunen.kete.unittests.matchers.matcherutils;

// For utils/tlsutils/getSslContextTests.java
package io.github.fortunen.kete.unittests.utils.tlsutils;
```

**NOTE:** Always use lowercase for package names to maintain consistency across the codebase.




## 4. Imports

### 4.1 Static Imports (Always First)

**RULE:** Use static imports with wildcards for test utilities. Order: AssertJ → Mockito.

```java
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
```

### 4.2 Common Static Import Combinations

```java
// Minimum for simple tests
import static org.assertj.core.api.Assertions.assertThat;

// For exception testing
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

// For exception-free testing
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

// For exception type + message testing
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// For mocking
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;

// For matchers
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
```

### 4.3 Regular Imports

**RULE:** Standard imports follow static imports. Group by: project classes → external libraries → JDK.

```java
// Project imports
import io.github.fortunen.kete.ProviderFactory;
import io.github.fortunen.kete.utils.ValidationUtils;

// External library imports
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;

// JDK imports
import java.util.HashMap;
import java.util.Map;
```



## 5. Class Declaration

### 5.1 Class Visibility

**RULE:** Class visibility is MIXED. Both patterns exist:

```java
// Package-private (no modifier) - COMMON
class closeTests {

// Public - COMMON  
public class serializeTests {

public class requireNonNullTests {

public class acceptTests {
```

**OBSERVATION:** No clear pattern. Both are acceptable.

### 5.2 Class Name

**RULE:** Class name matches the file name exactly (camelCase starting lowercase + `Tests`).

```java
class closeTests { }
class constructorTests { }
public class serializeTests { }
public class requireNonNullTests { }
public class acceptTests { }
```

### 5.3 No Class Annotations

**RULE:** Test classes have NO annotations. No `@ToString`, no `@DisplayName`, no `@TestInstance`.



## 6. Test Method Declaration

### 6.1 Visibility

**RULE:** Method visibility is MIXED. Both patterns exist:

```java
// Package-private (no modifier) - COMMON
@Test
void shouldShutdownEventExecutorOnClose() {

// Public - COMMON
@Test
public void shouldSerializeEventToJson() {
```

**OBSERVATION:** Package-private tends to appear in `providerfactory/`, `provider/`, `configuration/`. Public tends to appear in `utils/`, `serializers/`, `filters/`, `destinations/`.

### 6.2 Method Naming Pattern

**RULE:** `should{ExpectedBehavior}[When{Condition}]`

### 6.3 Examples by Category

**Success Cases:**
```java
shouldCreateInstanceWithNoArgConstructor()
shouldInitializeContainersAfterConstruction()
shouldReturnValueWhenNotNull()
shouldParsePositiveInteger()
shouldMatchExactString()
shouldSetBasicConfiguration()
shouldSerializeEventToJson()
shouldAcceptWhenFiltersEmpty()
shouldSendEvent()
shouldShutdownEventExecutorOnClose()
```

**Failure/Edge Cases:**
```java
shouldThrowWhenConfigurationIsNull()
shouldThrowWhenUrlMissing()
shouldThrowWhenNameIsNull()
shouldThrowWhenNameIsEmpty()
shouldThrowWhenNameIsBlank()
shouldReturnEmptyForNull()
shouldReturnEmptyForEmptyString()
shouldReturnEmptyForBlankString()
shouldReturnEmptyForInvalidString()
shouldNotMatchDifferentString()
shouldRejectWhenAnyFilterRejects()
shouldNotSendWhenEventTypeNull()
shouldNotSendWhenEventTypeEmpty()
shouldNotSendWhenEventBodyEmpty()
```

**Conditional Cases:**
```java
shouldInvertMatchWithNot()
shouldAllowMultipleCloseCalls()
shouldHandleEmptyPattern()
shouldHandleEmptyEventType()
shouldHandleEventHavingNullType()
shouldResizeThreadPoolWhenConfiguredDifferently()
shouldDisposeRouteOnInitializationFailure()
```



## 7. Test Method Structure (AAA Pattern)

### 7.1 Required Sections

**RULE:** Every test method MUST use AAA comments. This structure is MANDATORY and RELIGIOUSLY adhered to without exceptions.

### 7.2 Standard Structure

```java
@Test
void shouldDoSomething() {

    // arrange

    var instance = new ClassUnderTest();
    var input = "test-input";

    // act

    var result = instance.methodUnderTest(input);

    // assert

    assertThat(result).isNotNull();
}
```

### 7.3 Whitespace Rules (STRICT - NO EXCEPTIONS)

| Location | Rule | Mandatory |
|----------|------|:---------:|
| After method opening brace `{` | ONE blank line |  |
| BEFORE `// arrange` comment | ONE blank line (the one after `{`) |  |
| AFTER `// arrange` comment | ONE blank line |  |
| BEFORE `// act` comment | ONE blank line |  |
| AFTER `// act` comment | ONE blank line |  |
| BEFORE `// assert` comment | ONE blank line |  |
| AFTER `// assert` comment | ONE blank line |  |
| Before method closing brace `}` | ZERO blank lines |  |

### 7.4 Visual Template (Copy This Exactly)

```java
@Test
public void shouldDescribeExpectedBehavior() {
                                           // ← blank line after {
    // arrange
                                           // ← blank line after // arrange
    var instance = new ClassUnderTest();
    var input = "test-input";
                                           // ← blank line before // act
    // act
                                           // ← blank line after // act
    var result = instance.methodUnderTest(input);
                                           // ← blank line before // assert
    // assert
                                           // ← blank line after // assert
    assertThat(result).isNotNull();
}                                          // ← NO blank line before }
```

### 7.4 Section Variations

**Arrange Only (for constructor tests without method call):**
```java
@Test
void shouldCreateInstanceWithNoArgConstructor() {

    // act

    var instance = new ClassUnderTest();

    // assert

    assertThat(instance).isNotNull();
}
```

**Act + Assert Combined:**
```java
@Test
void shouldDisposeContainerOnClose() {

    // arrange

    var factory = new ProviderFactory();

    // act & assert

    assertThatCode(() -> factory.close()).doesNotThrowAnyException();
}
```

**Act & Assert for Exceptions:**
```java
@Test
public void shouldThrowWhenUrlMissing() {

    // arrange

    var destination = new HttpDestination(templateUtils, configUtils);
    var config = new MapConfiguration(new HashMap<>());

    // act

    var thrown = catchThrowable(() -> {
        destination.setConfiguration(config);
    });

    // assert

    assertThat(thrown).isInstanceOf(Exception.class);
}
```

### 7.5 Cleanup Section (Rare)

```java
@Test
void shouldCreateExecutorWithSpecifiedPoolSize() {

    // act

    ExecutorService executor = ExecutorUtils.createEventExecutor(5);

    // assert

    assertThat(executor).isNotNull();

    // cleanup

    executor.shutdownNow();
}
```



## 8. Variable Declarations

### 8.1 Primary Pattern: `var`

**RULE:** Use `var` for ALL local variable declarations when type is inferrable.

```java
var factory = new ProviderFactory();
var destination = mock(Destination.class);
var config = new MapConfiguration(new HashMap<>());
var result = instance.methodUnderTest();
var thrown = catchThrowable(() -> { ... });
```

### 8.2 Explicit Type Exceptions

**RULE:** Use explicit type when:
1. Type cannot be inferred (null assignment)
2. Explicit type provides clarity for complex generics
3. Array initialization

```java
String value = null;
MapConfiguration config = null;
ExecutorService executor = ExecutorUtils.createEventExecutor(5);
Event event = null;
```

### 8.3 Static Fields

**RULE:** Static fields use explicit types with `private static final`.

```java
private static final ObjectMapper MAPPER = new ObjectMapper();
private static final byte[] EMPTY_BYTES = new byte[0];
```



## 9. Assertions (AssertJ)

### 9.1 Basic Assertions

```java
// Not null
assertThat(result).isNotNull();

// Null
assertThat(result).isNull();

// Equality
assertThat(result).isEqualTo(expected);
assertThat(result).isNotEqualTo(unexpected);

// Same instance
assertThat(result).isSameAs(original);

// Boolean
assertThat(result).isTrue();
assertThat(result).isFalse();

// Empty collections/strings
assertThat(result).isEmpty();
assertThat(result).isNotEmpty();

// Size
assertThat(result).hasSize(5);
assertThat(result.length).isEqualTo(3);
```

### 9.2 String Assertions

```java
assertThat(result).isEqualTo("expected");
assertThat(result).startsWith("prefix");
assertThat(result).contains("substring");
assertThat(result.contains("text")).isTrue();
```

### 9.3 Collection Assertions

```java
assertThat(list).hasSize(3);
assertThat(array.length).isEqualTo(0);
assertThat(result).isNotNull();
assertThat(result.length).isEqualTo(0);
```

### 9.4 Optional Assertions

```java
assertThat(ValidationUtils.tryParseInt("123")).hasValue(123);
assertThat(ValidationUtils.tryParseInt("abc")).isEmpty();
```

### 9.5 Exception Assertions

**Pattern 1: `catchThrowable` + assert (MOST COMMON)**
```java
var thrown = catchThrowable(() -> {
    instance.methodThatThrows(badInput);
});

assertThat(thrown).isInstanceOf(IllegalStateException.class);
assertThat(thrown.getMessage()).isEqualTo("expected message");
assertThat(thrown.getMessage()).contains("partial text");
```

**Pattern 2: `assertThatThrownBy` (for concise exception + message)**
```java
assertThatThrownBy(() -> factory.onEvent(null))
    .isInstanceOf(IllegalStateException.class)
    .hasMessageContaining("providerEvent is required");
```

**Pattern 3: `assertThatCode` for no-exception verification**
```java
assertThatCode(() -> factory.close()).doesNotThrowAnyException();
```

### 9.6 Compound Boolean in Assertions

```java
assertThat(result.length > 0).isTrue();
assertThat(json.contains("\\n") || json.contains("\\t")).isTrue();
```



## 10. Mocking (Mockito)

### 10.1 Creating Mocks

**RULE:** Always use `mock()` method, never `@Mock` annotations.

```java
var session = mock(KeycloakSession.class);
var destination = mock(Destination.class);
var filter = mock(Filter.class);
```

### 10.2 Stubbing

```java
// Return value
when(session.realms()).thenReturn(realmProvider);
when(filter.accept(anyString())).thenReturn(true);

// Chain stubbing
when(configUtils.getConfiguration(any())).thenReturn(configuration);
when(configuration.isMetricsEnabled()).thenReturn(true);

// Throw exception
doThrow(new RuntimeException("init failed")).when(route).initialize();
```

### 10.3 Verification

```java
// Verify called once
verify(destination).sendMessage(any(EventMessage.class));

// Verify called specific times
verify(destination, times(1)).sendMessage(any());
verify(mockTransaction, times(3)).addEvent(any(Event.class));

// Verify never called
verify(destination, never()).sendMessage(any(EventMessage.class));

// Verify with argument matcher
verify(destination, times(1)).sendMessage(argThat(m ->
    "eventId".equals(m.eventId()) &&
    "LOGIN".equals(m.eventType())));

verify(mockTransaction, times(1)).addEvent(ArgumentMatchers.argThat(arg ->
    arg != null && "test-id".equals(arg.getId())
));
```

### 10.4 Static Mocking

```java
try (var mockedUtils = mockStatic(KeycloakModelUtils.class)) {

    mockedUtils.when(() -> KeycloakModelUtils.runJobInTransaction(any(), any()))
        .thenAnswer(invocation -> null);

    // test code here

    mockedUtils.verify(() -> KeycloakModelUtils.runJobInTransaction(sessionFactory, factory));
}
```



## 11. Test Data Creation

### 11.1 Inline Object Creation

**RULE:** Create test data inline in the arrange section. No shared fixtures.

```java
// Simple objects
var event = new Event();
event.setType(EventType.LOGIN);
event.setRealmId("test-realm");

// Maps for configuration
var config = new MapConfiguration(Map.of(
    "url", "http://localhost:8080/events",
    "method", "POST"
));

// Or with HashMap for mutable maps
var map = new HashMap<String, Object>();
map.put(RetryUtils.ENABLED, true);
var config = new MapConfiguration(map);
```

### 11.2 Helper Methods

**RULE:** Private helper methods within test class for complex or repeated setup.

```java
private EventMessage createMessage(String eventId, String realm, boolean isAdminEvent, 
        String eventType, String contentType, byte[] eventBody, 
        String resourceType, String operationType) {
    return EventMessage.get()
        .eventId(eventId != null ? eventId : "")
        .realm(realm != null ? realm : "")
        .isAdminEvent(isAdminEvent)
        .eventType(eventType != null ? eventType : "")
        .contentType(contentType != null ? contentType : "")
        .eventBody(eventBody != null ? eventBody : EMPTY_BYTES)
        .resourceType(resourceType != null ? resourceType : "")
        .operationType(operationType != null ? operationType : "");
}
```

### 11.3 Private Inner Classes

**RULE:** Use private static inner classes for test-specific types.

```java
private static class ComplexObject {
    String data;
    int number;

    ComplexObject(String data, int number) {
        this.data = data;
        this.number = number;
    }
}

private static class CustomCheckedException extends Exception {
    public CustomCheckedException(String message) {
        super(message);
    }
}

private static class TestJsonProcessingException extends JsonProcessingException {
    protected TestJsonProcessingException(String msg) {
        super(msg);
    }
}
```



## 12. Test Organization Within File

### 12.1 Logical Grouping

**RULE:** Group related tests. Use comment dividers for sections in large files.

```java
// =========================================================================
// ID
// =========================================================================

@Test
public void shouldHaveCorrectId() { ... }

// =========================================================================
// Header Constants
// =========================================================================

@Test
public void shouldHaveCorrectEventIdHeader() { ... }

@Test
public void shouldHaveCorrectEventTypeHeader() { ... }
```

### 12.2 Test Order

**PATTERN:** Happy path first, then edge cases, then error cases.

1. Basic success cases
2. Variations of success cases
3. Empty/null handling (returns default)
4. Error cases (throws exception)
5. Edge cases



## 13. Comments

### 13.1 Allowed Comments

| Type | Example | Allowed |
|------|---------|---------|
| AAA section markers | `// arrange` |  YES (REQUIRED) |
| Combined section | `// act & assert` |  YES |
| Section dividers | `// =========` |  YES |
| Cleanup section | `// cleanup` |  YES |
| Brief clarification in assert | `// assert - Jackson serializes null to "null"` |  YES |

### 13.2 Prohibited Comments

| Type | Allowed |
|------|---------|
| Method-level Javadoc |  NO |
| Inline explanatory comments |  NO |
| TODO comments |  NO |
| Implementation detail comments |  NO |



## 14. Exception Testing Patterns

### 14.1 Null Paramketer Tests

```java
@Test
public void shouldThrowWhenNameNull() {

    // arrange

    var utils = new SomeUtils();

    // act

    var thrown = catchThrowable(() -> {
        utils.method(null, validParam);
    });

    // assert

    assertThat(thrown).isInstanceOf(IllegalStateException.class);
}
```

### 14.2 Empty/Blank String Tests

```java
@Test
public void shouldThrowWhenNameEmpty() {
    // ... 
    utils.method("", validParam);
    // ...
}

@Test
public void shouldThrowWhenNameBlank() {
    // ...
    utils.method("   ", validParam);
    // ...
}
```

### 14.3 Exception Message Verification

```java
// Exact message
assertThat(thrown.getMessage()).isEqualTo("name is required");

// Contains
assertThat(thrown.getMessage()).contains("required");
assertThatThrownBy(() -> ...).hasMessageContaining("is required");
```



## 15. Complete Test File Template

```java
package io.github.fortunen.kete.{category}.{classname};

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.fortunen.kete.{ClassUnderTest};
import org.junit.jupiter.api.Test;

public class {methodName}Tests {

    @Test
    public void shouldSucceedWithValidInput() {

        // arrange

        var instance = new ClassUnderTest();
        var input = "valid-input";

        // act

        var result = instance.methodUnderTest(input);

        // assert

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("expected");
    }

    @Test
    public void shouldThrowWhenInputNull() {

        // arrange

        var instance = new ClassUnderTest();

        // act

        var thrown = catchThrowable(() -> {
            instance.methodUnderTest(null);
        });

        // assert

        assertThat(thrown).isInstanceOf(IllegalStateException.class);
        assertThat(thrown.getMessage()).contains("input is required");
    }

    @Test
    public void shouldReturnDefaultWhenInputEmpty() {

        // arrange

        var instance = new ClassUnderTest();

        // act

        var result = instance.methodUnderTest("");

        // assert

        assertThat(result).isEmpty();
    }
}
```



## 16. Anti-Patterns (NEVER USE)

| Anti-Pattern | Reason |
|--------------|--------|
| `@Mock` annotations | Use `mock()` method instead |
| `@InjectMocks` | Manual dependency injection |
| `@BeforeEach` / `@AfterEach` | Inline setup in each test (for unit tests) |
| `@DisplayName` | Method name is self-documenting |
| `@ToString` on test class | Not needed |
| `@Nested` classes | One file per method |
| JUnit 5 assertions | Use AssertJ |
| Shared mutable state | Each test independent |
| Test fixtures / builders | Inline data creation |
| `@ParamketerizedTest` | Separate explicit tests |
| Comments explaining code | Self-documenting tests |
| Testing constants/static fields | No utility - values are self-evident |
| Testing abstract classes directly | Test concrete implementations instead |
| Testing Lombok-generated constructors | Only test manually-written constructors with logic |
| Testing Lombok-generated getters/setters | Only test manually-written accessors with logic |



## 17. Checklist for New Test Files

- [ ] Package matches folder structure
- [ ] File name: `{methodName}Tests.java`
- [ ] Class name matches file name
- [ ] Static imports for AssertJ and Mockito
- [ ] Each test uses AAA comments
- [ ] Blank line after each section comment
- [ ] Use `var` for local variables
- [ ] Method name starts with `should`
- [ ] No `@BeforeEach` / `@AfterEach` (inline setup)
- [ ] No class-level annotations
- [ ] Use `catchThrowable` for exception testing
- [ ] Use `assertThat()` from AssertJ
- [ ] Use `mock()` not `@Mock`
- [ ] Test data created inline



## 18. File Count by Category

| Category | Files |
|----------|-------|
| utils/validationutils/ | 68 |
| serializers/ | 16 |
| matchers/ | 8 |
| providerfactory/ | 9 |
| destinations/ | 10 |
| routes/ | 7 |
| provider/ | 5 |
| configuration/ | 3 |
| retries/ | 2 |
| utils/ (other) | 12 |
| **Total** | **140** |



## 19. Testing Philosophy & Quality Standards

### 19.1 Core Principles

Tests serve as **living documentation** of the system. They must:

1. **Document Behavior**: Tests can be read as a book when in doubt about what the code is supposed to do
2. **Protect Against Regression**: Guard the current system against future corruption if maintainers are not careful
3. **Ensure Quality**: Verify the correctness of the current system
4. **Provide Maximum Confidence**: Cover edge cases not just for coverage metrics, but for real-world confidence

### 19.2 Test Utility Requirements

**RULE:** Every test MUST have actual utility. No contrived scenarios.

| Requirement | Description |
|-------------|-------------|
| **Real-world scenarios** | Test realistic usage patterns, not artificial constructs |
| **Meaningful assertions** | Assert outcomes that matter to the system's behavior |
| **Edge case exploration** | Cover boundary conditions that could cause failures in production |
| **Failure mode testing** | Verify graceful handling of error conditions |

### 19.3 Assertion Completeness

**RULE:** Assert the FULL state of the outcome. Partial assertions are insufficient.

```java
//  BAD - Partial assertion
assertThat(result).isNotNull();

//  GOOD - Complete state verification
assertThat(result).isNotNull();
assertThat(result.getId()).isEqualTo("expected-id");
assertThat(result.getName()).isEqualTo("expected-name");
assertThat(result.isEnabled()).isTrue();
assertThat(result.getItems()).hasSize(3);
assertThat(result.getItems()).containsExactly("item1", "item2", "item3");
```

### 19.4 Exception Message Assertion

**RULE:** EXACT exception messages MUST be asserted. This documents the API contract.

```java
//  BAD - Only checking exception type
assertThat(thrown).isInstanceOf(IllegalStateException.class);

//  BAD - Partial message check
assertThat(thrown.getMessage()).contains("required");

//  GOOD - Exact message assertion
assertThat(thrown)
    .isInstanceOf(IllegalStateException.class)
    .hasMessage("url is required");
```

### 19.5 AssertJ Expressiveness

**WHY AssertJ:** Chosen for its expressiveness (inspired by Shouldly in C#). Leverage the fluent API for clarity.

```java
// Leverage AssertJ's expressive assertions
assertThat(result)
    .isNotNull()
    .isInstanceOf(Configuration.class)
    .extracting(Configuration::isMetricsEnabled)
    .isEqualTo(false);

assertThat(routes)
    .hasSize(3)
    .extracting(Route::getName)
    .containsExactlyInAnyOrder("route-1", "route-2", "route-3");

assertThat(exception)
    .isInstanceOf(IllegalStateException.class)
    .hasMessage("bootstrap.servers is required")
    .hasNoCause();
```



## 20. Serializer Testing Requirements

### 20.1 Round-Trip Verification

**RULE:** Serializer tests MUST parse the result to create a copy that is 100% compared to the input data.

```java
@Test
public void shouldSerializeAndDeserializeEventWithAllFields() {

    // arrange

    var serializer = new JsonSerializer();
    var event = new Event();
    event.setId("test-id");
    event.setType(EventType.LOGIN);
    event.setRealmId("test-realm");
    event.setUserId("test-user");
    event.setClientId("test-client");
    event.setSessionId("test-session");
    event.setIpAddress("192.168.1.1");
    event.setTime(1704067200000L);
    event.setDetails(Map.of("key1", "value1", "key2", "value2"));

    // act

    var serialized = serializer.serialize(event);

    // assert - Parse back and verify Complete equality

    var parsed = MAPPER.readTree(serialized);
    assertThat(parsed.get("id").asText()).isEqualTo("test-id");
    assertThat(parsed.get("type").asText()).isEqualTo("LOGIN");
    assertThat(parsed.get("realmId").asText()).isEqualTo("test-realm");
    assertThat(parsed.get("userId").asText()).isEqualTo("test-user");
    assertThat(parsed.get("clientId").asText()).isEqualTo("test-client");
    assertThat(parsed.get("sessionId").asText()).isEqualTo("test-session");
    assertThat(parsed.get("ipAddress").asText()).isEqualTo("192.168.1.1");
    assertThat(parsed.get("time").asLong()).isEqualTo(1704067200000L);
    assertThat(parsed.get("details").get("key1").asText()).isEqualTo("value1");
    assertThat(parsed.get("details").get("key2").asText()).isEqualTo("value2");
}
```

### 20.2 Edge Cases for Serializers

| Edge Case | Must Test |
|-----------|-----------|
| All fields populated |  |
| All fields null |  |
| Empty strings |  |
| Unicode characters |  |
| Special characters (newlines, tabs, quotes) |  |
| Very long strings |  |
| Nested structures |  |
| Null event/adminEvent object |  |



## 21. Destination Testing Requirements

### 21.1 Unit Test Scope for Destinations

**UNDERSTANDING:** Integration tests achieve maximum effect for destinations, but unit tests MUST cover:

| Scenario | Unit Testable | Approach |
|----------|:-------------:|----------|
| Configuration validation |  | Test setConfiguration with invalid/missing values |
| Constructor behavior |  | Verify dependencies are set |
| Connection failure handling |  | Mock connection factories to throw |
| Message send when disconnected |  | Mock clients in error state |
| Resource cleanup on close |  | Verify close methods called on mocks |
| Retry behavior |  | Mock failures, verify retry invocation |

### 21.2 Unconnected Destination Behavior

**RULE:** Test behavior when destinations cannot connect (no RabbitMQ, Kafka, etc.)

```java
@Test
public void shouldThrowWhenBrokerUnreachable() {

    // arrange

    var destination = new KafkaDestination(templateUtils, configUtils);
    var config = new MapConfiguration(Map.of(
        "bootstrap.servers", "unreachable:9092",
        "topic", "test-topic"
    ));
    destination.setConfiguration(config);

    // act

    var thrown = catchThrowable(() -> destination.initialize());

    // assert

    assertThat(thrown)
        .isInstanceOf(KafkaException.class)
        .hasMessageContaining("Failed to connect");
}
```

### 21.3 HTTP Destination - WireMock Testing

**RULE:** Use MockWebServer or WireMock for full HTTP destination testing in unit tests.

```java
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

public class sendMessageTests {

    private MockWebServer mockServer;

    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockServer.shutdown();
    }

    @Test
    public void shouldSendPostRequestWithEventBody() throws Exception {

        // arrange

        mockServer.enqueue(new MockResponse().setResponseCode(200));

        var destination = new HttpDestination(templateUtils, configUtils);
        var config = new MapConfiguration(Map.of(
            "url", mockServer.url("/events").toString(),
            "method", "POST"
        ));
        destination.setConfiguration(config);
        destination.initialize();

        var message = EventMessage.get()
            .eventId("test-id")
            .eventType("LOGIN")
            .contentType("application/json")
            .eventBody("{\"type\":\"LOGIN\"}".getBytes());

        // act

        destination.sendMessage(message);

        // assert

        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/events");
        assertThat(request.getBody().readUtf8()).isEqualTo("{\"type\":\"LOGIN\"}");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(request.getHeader("EventId")).isEqualTo("test-id");
        assertThat(request.getHeader("EventType")).isEqualTo("LOGIN");
    }

    @Test
    public void shouldHandleServerError() throws Exception {

        // arrange

        mockServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Error"));

        var destination = new HttpDestination(templateUtils, configUtils);
        // ... setup ...

        // act

        var thrown = catchThrowable(() -> destination.sendMessage(message));

        // assert

        assertThat(thrown)
            .isInstanceOf(HttpResponseException.class)
            .hasMessage("HTTP 500: Internal Error");
    }
}
```

**NOTE:** For HTTP destinations, `@BeforeEach` and `@AfterEach` ARE allowed for MockWebServer lifecycle management.

### 21.4 Integration & E2E Test Policy

**MANDATORY RULE:** Tests requiring containers or external services are **expensive**. Strict limits apply per destination.

#### Integration Tests — Exactly 3 Per Destination

Each destination MUST have exactly **3 integration send tests** in a single `sendTests.java` file:

| Test | Purpose |
|------|---------|
| `shouldSend_NonTls` | Plain text connection (no TLS) |
| `shouldSend_Tls` | TLS — server auth only, client trusts server certificate |
| `shouldSend_mTls` | Mutual TLS — both client and server authenticate each other |

**File structure per destination:**

```
integrationtests/<destination>/
    TestBase.java     ← MockWebServer lifecycle, TLS helpers, configureDestination()
    sendTests.java    ← Exactly 3 tests: shouldSend_NonTls, shouldSend_Tls, shouldSend_mTls
```

**No other integration test files.** No `initializeTests`, `closeTests`, or extra send tests.

#### TLS Test Pattern

```java
@Test
public void shouldSend_Tls() throws Exception {

    // arrange

    var tls = TlsMaterial.builder()
        .withEnabled(true)
        .withWriteFiles(true)
        .withTrustStorePassword("changeit")
        .withKeyStorePassword("changeit")
        .withKeyPassword("changeit")
        .withServerHostNames(new String[] { "localhost", "127.0.0.1" })
        .build();

    startMockServerWithTls(tls);
    // ... enqueue responses, configure destination with tls.trust-store properties ...

    // act

    destination.send(message);

    // assert — verify request reached server
}
```

#### mTLS Test Pattern

Same as TLS, but additionally configure `tls.key-store.*` properties and call `startMockServerWithMtls(tls)` (which calls `mockServer.requireClientAuth()`).

#### Nginx Proxy Fallback

If the destination broker/server does **NOT** natively support TLS/mTLS, use an **nginx reverse proxy** container to terminate TLS and still run all 3 tests. This ensures every destination has consistent TLS coverage regardless of native support.

#### E2E Tests — Exactly 1 Per Destination

Each destination has exactly **1 E2E test** that verifies the full pipeline: Keycloak → KETE plugin → destination broker → message received.

#### Reference Implementation

See `integrationtests/httpdestination/` for the canonical implementation of this policy.



## 22. Edge Case Testing Requirements

### 22.1 Mandatory Edge Cases

Every method under test MUST have tests for:

| Category | Cases |
|----------|-------|
| **Null handling** | null input, null in collections, null nested objects |
| **Empty values** | empty string `""`, empty collections, empty arrays |
| **Blank values** | whitespace-only strings `"   "` |
| **Boundary values** | 0, -1, Integer.MAX_VALUE, Integer.MIN_VALUE |
| **Invalid formats** | malformed URLs, invalid JSON, unparseable numbers |
| **Concurrent access** | thread safety (where applicable) |
| **Resource exhaustion** | very large inputs, many items |

### 22.2 Example: Comprehensive Validation Testing

```java
// Test NULL
@Test
public void shouldThrowWhenValueNull() { ... }

// Test EMPTY
@Test
public void shouldThrowWhenValueEmpty() { ... }

// Test BLANK  
@Test
public void shouldThrowWhenValueBlank() { ... }

// Test VALID edge case
@Test
public void shouldAcceptSingleCharacterValue() { ... }

// Test BOUNDARY
@Test
public void shouldHandleMaxLengthValue() { ... }

// Test UNICODE
@Test
public void shouldHandleUnicodeCharacters() { ... }

// Test SPECIAL CHARS
@Test
public void shouldHandleSpecialCharacters() { ... }
```



## 23. Test as Documentation

### 23.1 Reading Tests Like a Book

Tests MUST be written so that a reader can understand the system's expected behavior without reading source code.

```java
@Test
public void shouldRejectEventWhenAllFiltersReject() {
    // This test documents: When multiple matchers are configured,
    // ALL must accept for the event to pass. If ANY rejects, the event is rejected.

    // arrange

    var rejectFilter1 = mock(Filter.class);
    var rejectFilter2 = mock(Filter.class);
    when(rejectFilter1.accept(anyString())).thenReturn(false);
    when(rejectFilter2.accept(anyString())).thenReturn(false);

    var route = new Route();
    route.setFilters(new Filter[] { rejectFilter1, rejectFilter2 });

    // act

    var accepted = route.accept("LOGIN");

    // assert

    assertThat(accepted)
        .as("Event should be rejected when any filter rejects")
        .isFalse();
}
```

### 23.2 Test Naming as Documentation

Method names describe behavior, not implementation:

```java
//  BAD - Describes implementation
shouldCallValidateMethod()
shouldSetFieldToNull()

//  GOOD - Describes behavior
shouldRejectInvalidUrl()
shouldDefaultToTenSecondsWhenTimeoutNotSpecified()
shouldRetryThreeTimesBeforeFailingPermanently()
```



## 24. Regression Protection

### 24.1 Purpose

Tests exist to **protect against future corruption**. Every test should:

1. Verify a specific behavior that MUST NOT change
2. Fail loudly if a maintainer inadvertently breaks the contract
3. Provide clear error messages showing what was expected vs. what happened

### 24.2 Contract Verification

```java
@Test
public void shouldMaintainBackwardCompatibleDefaults() {
    // This test protects the default configuration contract.
    // Changing these defaults would break existing deployments.

    // act

    var config = new Configuration();

    // assert - These are the documented defaults. DO NOT CHANGE.

    assertThat(config.isMetricsEnabled())
        .as("Metrics must be disabled by default")
        .isFalse();

    assertThat(config.getRoutes())
        .as("Routes must default to empty array")
        .isEmpty();
}
```



## 25. Checklist for Comprehensive Tests

### 25.1 Per-Test Checklist

- [ ] Test has actual utility (not contrived)
- [ ] Blank line after method opening `{`
- [ ] `// arrange` with blank line before and after
- [ ] `// act` with blank line before and after
- [ ] `// assert` with blank line before and after
- [ ] No blank line before closing `}`
- [ ] Full state of outcome is asserted
- [ ] Exact exception messages are verified
- [ ] AssertJ fluent API is leveraged for expressiveness

### 25.2 Per-Class Checklist

- [ ] Happy path tested
- [ ] Null inputs tested
- [ ] Empty inputs tested
- [ ] Blank string inputs tested (where applicable)
- [ ] Boundary values tested
- [ ] Error conditions tested with exact messages
- [ ] Tests read as documentation of expected behavior

### 25.3 Serializer-Specific Checklist

- [ ] Round-trip verification (serialize → parse → compare)
- [ ] All fields populated test
- [ ] All fields null test
- [ ] Unicode characters test
- [ ] Special characters test

### 25.4 Destination-Specific Checklist

- [ ] Configuration validation tests
- [ ] Unconnected/unreachable broker tests
- [ ] Connection failure handling tests
- [ ] HTTP: MockWebServer tests for full request/response verification
