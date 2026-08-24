# Component Scopes and Dependency Injection

## Overview

This project uses a lightweight **custom IoC** system with the Reflections library. The `@Component` annotation marks classes for automatic registration and lifecycle management.

## Component Annotation

```java
@Component(name = "component-name", scope = Component.TRANSIENT)
public class MyComponent {
    // ...
}
```

### Attributes

- **`name`**: Unique identifier for the component (kebab-case recommended)
- **`scope`**: Lifecycle management strategy

## Component Scopes

Inspired by .NET Core DI concepts (Transient/Scoped/Singleton):

### `TRANSIENT` Scope

**Definition**: A **new instance** is created **every time** the component is requested from the container.

**Analogy (.NET Core)**: Similar to `services.AddTransient<T>()`

**Use Cases**:
- Stateless services
- Request-scoped operations
- Operations that should not share state
- All 29 destinations, the 4 matchers and the 11 certificate loaders
- The `template`, `multipart-form` and `url-encoded-form` serializers (the other 10 serializers are SINGLETON)

**Lifecycle**:
```
Request 1: Container.get(Component) → New Instance A created
Request 2: Container.get(Component) → New Instance B created
Request 3: Container.get(Component) → New Instance C created
```

**Important**: 
-  The container never disposes instances; pooled destinations are closed by the destination pool (`destroyObject` → `close()`) when invalidated, evicted or when the pool shuts down
-  Multiple calls create multiple independent instances
-  Each instance has its own state
-  Does NOT mean "temporary" or "disposable"

**Example**:
```java
@Component(name = "http", scope = Component.TRANSIENT)
public class HttpDestination extends Destination {
    // New instance created for each realm/destination config
    private HttpClient httpClient;
    private String url;
}
```

### `SINGLETON` Scope

**Definition**: A **single shared instance** is created once and reused for all requests.

**Analogy (.NET Core)**: Similar to `services.AddSingleton<T>()`

**Use Cases**:
- Stateless, thread-safe services shared by all routes (the singleton serializers)
- Expensive, immutable resources (e.g. Jackson `ObjectMapper`/`ObjectWriter`)

**Lifecycle**:
```
Request 1: Container.get(Component) → Instance A created
Request 2: Container.get(Component) → Same Instance A returned
Request 3: Container.get(Component) → Same Instance A returned
```

**Example** (`JsonSerializer`):
```java
@Component(name = "json", scope = Component.SINGLETON)
public class JsonSerializer extends Serializer {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String contentType = "application/json";
}
```

### `SCOPED` Scope *(Not Currently Implemented)*

**Definition**: One instance per logical scope (e.g., per HTTP request, per Keycloak session).

**Analogy (.NET Core)**: Similar to `services.AddScoped<T>()`

**Future Use Cases**:
- Per-session state management
- Request-specific context
- Transaction-scoped resources

## Why TRANSIENT for Destinations?

Each **destination** in our system is configured independently:

```bash
# Destination 1: HTTP with OAuth
kete.routes.api1.destination.kind=http
kete.routes.api1.destination.url=https://api1.com/events
kete.routes.api1.destination.oauth.enabled=true
kete.routes.api1.destination.oauth.token-url=https://auth.com/token

# Destination 2: HTTP with API Key
kete.routes.api2.destination.kind=http
kete.routes.api2.destination.url=https://api2.com/webhooks
kete.routes.api2.destination.headers.X-API-Key=secret
```

With **TRANSIENT** scope:
- `api1` gets its own HttpDestination instance (with OAuth client, cached tokens)
- `api2` gets a separate HttpDestination instance (with custom headers, no OAuth)
- Each maintains independent state (tokens, connections, retry counters)

With **SINGLETON** scope (wrong for destinations):
-  Both would share the same HttpDestination instance
-  Configuration conflicts (url, headers, OAuth settings)
-  State leaks between destinations

## Why SINGLETON for Serializers?

Serializers are **stateless** and **thread-safe**:

```java
@Component(name = "json", scope = Component.SINGLETON)
public class JsonSerializer extends Serializer {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectWriter EVENT_WRITER = MAPPER.writerFor(Event.class);
    
    @Override
    public byte[] serialize(Event event) {
        return EVENT_WRITER.writeValueAsBytes(event);  // Thread-safe
    }
}
```

With **SINGLETON** scope:
- One instance shared across all routes
- ObjectMapper/ObjectWriter are thread-safe and expensive to create
- Reduces memory footprint
- Jackson serializers are immutable after configuration

## Common Patterns

The examples below are illustrative shapes, not classes that exist in KETE.

### Pattern 1: Stateless Service (TRANSIENT)

```java
@Component(name = "validator", scope = Component.TRANSIENT)
public class EventValidator {
    public boolean validate(Event event) {
        // No internal state, safe to reuse or create new
        return event != null && event.getType() != null;
    }
}
```

### Pattern 2: Stateful Service (TRANSIENT)

```java
@Component(name = "rabbitmq", scope = Component.TRANSIENT)
public class RabbitMqDestination extends Destination {
    private Connection connection;      // State: unique per destination
    private Channel channel;             // State: unique per destination
    private String cachedAccessToken;    // State: unique per destination
    
    @Override
    protected void doInitialize() {
        // Each instance initializes with its own config (the `config` field)
        this.connection = config.getConnectionFactory().newConnection();
        this.channel = connection.createChannel();
    }
}
```

### Pattern 3: Global Resource (SINGLETON)

```java
@Component(name = "metrics", scope = Component.SINGLETON)
public class MetricsCollector {
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    
    public void increment(String metric) {
        // Global shared state across all components
        counters.computeIfAbsent(metric, k -> new AtomicLong()).incrementAndGet();
    }
}
```

## Thread Safety

### TRANSIENT Components
-  Each instance is independent
-  No shared state between instances
-  Still need thread-safe if multiple threads use the same instance

### SINGLETON Components
-  Shared state across all threads
-  **MUST** be thread-safe (use `synchronized`, `ConcurrentHashMap`, `AtomicLong`, etc.)
-  Immutable state is always thread-safe

## Comparison with Other DI Frameworks

| Framework | Our TRANSIENT | Our SINGLETON | Our SCOPED |
|-----------|---------------|---------------|------------|
| .NET Core | `AddTransient` | `AddSingleton` | `AddScoped` |
| Spring | `@Prototype` | `@Singleton` | `@RequestScope` |
| Guice | Default (no scope) | `@Singleton` | `@RequestScoped` |
| CDI | `@Dependent` | `@ApplicationScoped` | `@RequestScoped` |

## Example: Full System

```java
// Serializers: Shared across all routes (stateless, thread-safe)
@Component(name = "json", scope = Component.SINGLETON)
public class JsonSerializer extends Serializer { }

@Component(name = "xml", scope = Component.SINGLETON)
public class XmlSerializer extends Serializer { }

// Matchers: Unique per configuration (each has own pattern)
@Component(name = "glob")
public class GlobMatcher extends Matcher { }

// Destinations: Unique per configuration (each has own connection)
@Component(name = "http")
public class HttpDestination extends Destination { }

@Component(name = "kafka")
public class KafkaDestination extends Destination { }
```

**Container behavior** (`IocUtils.get(name, type)`):
```java
// SINGLETON: Same instance returned
var s1 = IocUtils.get("json", Serializer.class);  // Instance A
var s2 = IocUtils.get("json", Serializer.class);  // Same Instance A
assert s1 == s2;

// TRANSIENT: New instance each time
var d1 = IocUtils.get("http", Destination.class);  // Instance B
var d2 = IocUtils.get("http", Destination.class);  // Instance C
assert d1 != d2;
```

## Debugging

### Check Component Scope

```java
var annotation = MyComponent.class.getAnnotation(Component.class);
System.out.println("Scope: " + annotation.scope());  // "transient" (default) or "singleton"
```

Component creation is not logged; `IocUtils` scans `io.github.fortunen.kete` with the Reflections library once and instantiates components through their public no-args constructor.

## Related Documentation

- [Architecture Overview](overview.md) - System design and DI container setup
- [Development Guide](development.md) - Creating new components
