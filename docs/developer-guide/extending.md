# Extending KETE

Add custom destinations, serializers, matchers, and certificate loaders to KETE.

---

## Adding a New Destination

### 1. Create the Config Class

Each destination needs a configuration class extending `DestinationConfig`:

```java
package io.github.fortunen.kete.destinations;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import io.github.fortunen.kete.DestinationConfig;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MyDestinationConfig extends DestinationConfig {
    
    private String host;
    private int port = 8080;
    private String topic;
    
    // Lombok generates getters/setters
}
```

### 2. Create the Destination Class

```java
package io.github.fortunen.kete.destinations;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Component(name = "my-destination")
public class MyDestination extends Destination<MyDestinationConfig> {

    private MyClient client;
    
    @Override
    protected void doInitialize() {
        // config is already set and validated
        client = new MyClient(config.getHost(), config.getPort());
        client.connect();
        
        // TLS is available via config.getTls() if configured
        if (config.getTls().isEnabled()) {
            var sslContext = config.getTls().getKeyStoreAndTrustStoreSSLContext();
            client.enableTls(sslContext);
        }
    }
    
    @Override
    protected void doSend(EventMessage message) {
        client.publish(config.getTopic(), message.eventBody());
    }
    
    @Override
    public void close() {
        if (client != null) {
            client.disconnect();
        }
    }
}
```

### 3. Key Points

| Aspect | Details |
|--------|---------|
| `@Component(name = "xxx")` | The name becomes `destination.kind` value |
| Scope | Destinations are TRANSIENT by default (new instance per route) |
| `config` field | Automatically populated before `doInitialize()` is called |
| `doInitialize()` | Called once at startup, set up connections here |
| `doSend(EventMessage)` | Called for each event, send the message here |
| `close()` | Called on shutdown, clean up resources |

### 4. Use the Destination

```bash
kete.routes.my-route.destination.kind=my-destination
kete.routes.my-route.destination.host=example.com
kete.routes.my-route.destination.port=9090
kete.routes.my-route.destination.topic=events
```

---

## Adding a New Serializer

### 1. Create the Class

```java
package io.github.fortunen.kete.serializers;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Serializer;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

@Component(name = "my-format", scope = Component.SINGLETON)
public class MySerializer extends Serializer {

    private String contentType = "application/my-format";
    
    @Override
    public byte[] serialize(Event event) {
        return formatEvent(event).getBytes();
    }
    
    @Override
    public byte[] serialize(AdminEvent adminEvent) {
        return formatAdminEvent(adminEvent).getBytes();
    }
    
    private String formatEvent(Event event) {
        // Your serialization logic
        return "...";
    }
    
    private String formatAdminEvent(AdminEvent adminEvent) {
        // Your serialization logic
        return "...";
    }
}
```

### 2. Key Points

| Aspect | Details |
|--------|---------|
| Scope | SINGLETON (one instance shared across all routes) |
| `contentType` | Set the MIME type via field initialization |
| Thread-safe | Serializers must be thread-safe |

### 3. Use the Serializer

```bash
kete.routes.my-route.serializer.kind=my-format
```

---

## Adding a New Matcher

### 1. Create the Class

```java
package io.github.fortunen.kete.matchers;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Matcher;

@Component(name = "my-matcher")
public class MyMatcher extends Matcher {

    private MyPatternEngine engine;
    
    @Override
    public void initialize() {
        // pattern field is inherited, contains the user's pattern
        this.engine = MyPatternEngine.compile(pattern);
    }
    
    @Override
    public boolean matches(String eventType) {
        return engine.matches(eventType);
    }
}
```

### 2. Key Points

| Aspect | Details |
|--------|---------|
| `pattern` field | Inherited, contains the user's pattern |
| `not` field | Inherited, indicates if result should be negated |
| `accept()` method | Already implemented, calls `matches()` XOR `not` |
| Matching | Case-insensitive by convention |

### 3. Use the Matcher

```bash
kete.routes.my-route.event-matchers.filter=my-matcher:PATTERN_HERE
kete.routes.my-route.event-matchers.exclude=my-matcher:not:PATTERN_HERE
```

---

## Adding a New Certificate Loader

### 1. Create the Class

```java
package io.github.fortunen.kete.certificateloaders;

import io.github.fortunen.kete.CertificateLoader;
import io.github.fortunen.kete.Component;
import java.security.KeyStore;

@Component(name = "my-loader")
public class MyCertificateLoader extends CertificateLoader {

    @Override
    public void initialize() {
        // Validate configuration properties
    }
    
    @Override
    public void loadKeyStore(KeyStore keyStore, char[] password) throws Exception {
        // Load certificates/keys into the keyStore
        String source = configuration.getString("source");
        // ... parse and add entries to keyStore
    }
}
```

### 2. Key Points

| Aspect | Details |
|--------|---------|
| `configuration` | Access loader-specific properties |
| `keyStore` parameter | Pre-initialized, add entries to it |
| `password` parameter | Use for private key entries |

### 3. Use the Loader

```bash
kete.routes.my-route.destination.tls.trust-store.loader.kind=my-loader
kete.routes.my-route.destination.tls.trust-store.loader.source=/path/to/source
```

---

## Component Discovery

Components are discovered automatically at startup:

1. `IocUtils` scans the `io.github.fortunen.kete` package
2. Classes annotated with `@Component` are registered
3. SINGLETON components are instantiated immediately
4. TRANSIENT components are instantiated on demand

No additional registration is required—just add the `@Component` annotation.

---

## EventMessage Reference

The `EventMessage` record carries event data through the pipeline:

```java
public record EventMessage(
    String realm,           // Keycloak realm name
    String eventId,         // Unique event identifier
    byte[] eventBody,       // Serialized event content
    String eventType,       // Event type (e.g., LOGIN, LOGOUT)
    String contentType,     // MIME type from serializer
    String resourceType,    // Admin event resource type
    String kind,            // "EVENT" or "ADMIN_EVENT"
    String operationType,   // Admin event operation
    String result           // Event result (e.g., SUCCESS, ERROR)
) {
    // Helper methods
    String kindLowerCase();         // "event" or "admin_event"
    String kindUpperCase();         // "EVENT" or "ADMIN_EVENT"
    String eventIdLowerCase();      // Lowercase event ID
    String eventIdUpperCase();      // Uppercase event ID
    String realmLowerCase();        // Cached lowercase realm
    String realmUpperCase();        // Cached uppercase realm
    String eventTypeLowerCase();    // Cached lowercase event type
    String eventTypeUpperCase();    // Cached uppercase event type
    String resourceTypeLowerCase(); // Cached lowercase resource type
    String resourceTypeUpperCase(); // Cached uppercase resource type
    String operationTypeLowerCase();// Cached lowercase operation type
    String operationTypeUpperCase();// Cached uppercase operation type
    String resultLowerCase();       // Cached lowercase result
    String resultUpperCase();       // Cached uppercase result
    byte[] eventTypeBytes();        // UTF-8 bytes for headers
    byte[] contentTypeBytes();      // UTF-8 bytes for headers
}
```

---

## Testing New Components

### Unit Tests

Follow the project's [test patterns](test-patterns-and-conventions.md):

```java
package io.github.fortunen.kete.destinations.mydestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;

public class doInitializeTests {

    @Test
    public void shouldConnectSuccessfully() {
    
        // arrange
        
        var config = new MyDestinationConfig();
        config.setHost("localhost");
        config.setPort(8080);
        
        var destination = new MyDestination();
        destination.setConfig(config);
        
        // act
        
        destination.initialize();
        
        // assert
        
        assertThat(destination.getClient()).isNotNull();
        
        // cleanup
        
        destination.close();
    }
    
    @Test
    public void shouldThrowWhenHostMissing() {
    
        // arrange
        
        var config = new MyDestinationConfig();
        config.setPort(8080);
        // host not set
        
        var destination = new MyDestination();
        destination.setConfig(config);
        
        // act
        
        var thrown = catchThrowable(() -> destination.initialize());
        
        // assert
        
        assertThat(thrown)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("host is required");
    }
}
```

### Integration Tests

Use Testcontainers for integration testing:

```java
@Testcontainers
class MyDestinationIntegrationTests {

    @Container
    static GenericContainer<?> myService = new GenericContainer<>("my-service:latest")
        .withExposedPorts(8080);
    
    @Test
    void shouldSendToRealService() {
        var config = new MyDestinationConfig();
        config.setHost(myService.getHost());
        config.setPort(myService.getMappedPort(8080));
        
        var destination = new MyDestination();
        destination.setConfig(config);
        destination.initialize();
        
        // ... test with real service
        
        destination.close();
    }
}
```

---

## See Also

- [Component Scopes](component-scopes.md) — SINGLETON vs TRANSIENT
- [Architecture](architecture.md) — System design overview
- [Test Patterns](test-patterns-and-conventions.md) — Testing conventions

