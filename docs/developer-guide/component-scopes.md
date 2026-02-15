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
- Most destinations (all 27 are TRANSIENT)\n- TemplateSerializer (the only TRANSIENT serializer; the other 8 are SINGLETON)

**Lifecycle**:
```
Request 1: Container.get(Component) → New Instance A created
Request 2: Container.get(Component) → New Instance B created
Request 3: Container.get(Component) → New Instance C created
```

**Important**: 
-  The instance is **NOT** disposed after use - it remains until GC
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
- Global shared state
- Expensive resources (connection pools, caches)
- Configuration managers
- Logger instances

**Lifecycle**:
```
Request 1: Container.get(Component) → Instance A created
Request 2: Container.get(Component) → Same Instance A returned
Request 3: Container.get(Component) → Same Instance A returned
```

**Example**:
```java
@Component(name = "logger", scope = Component.SINGLETON)
public class GlobalLogger {
    // Single shared instance across all components
    private static final Map<String, Level> logLevels = new ConcurrentHashMap<>();
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

## Why SINGLETON for Logger?

Loggers are:
- Stateless (log messages don't require instance state)
- Expensive to create (but lightweight to use)
- Shared across all components

With **SINGLETON** scope:
- All components share one logger instance
- Reduces memory footprint
- Consistent logging configuration

## Common Patterns

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
    public void initialize(Configuration config) {
        // Each instance initializes with its own config
        this.connection = factory.newConnection();
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

**Container behavior**:
```java
// SINGLETON: Same instance returned
JsonSerializer s1 = container.get(JsonSerializer.class);  // Instance A
JsonSerializer s2 = container.get(JsonSerializer.class);  // Same Instance A
assert s1 == s2;  //  Same SINGLETON instance

// TRANSIENT: New instance each time
HttpDestination d1 = container.get(HttpDestination.class);  // Instance B
HttpDestination d2 = container.get(HttpDestination.class);  // Instance C
assert d1 != d2;  //  Different TRANSIENT instances
```

## Debugging

### Check Component Scope

```java
Class<?> componentClass = MyComponent.class;
Component annotation = componentClass.getAnnotation(Component.class);
String scope = annotation.scope();
System.out.println("Scope: " + scope);  // "TRANSIENT" or "SINGLETON"
```

### Verify Instance Creation

```java
// Enable debug logging
logger.setLevel(Level.FINE);

// Component creation will be logged
Component instance = container.get("component-name");
// Log: "Created new TRANSIENT instance of component-name"
```

## Related Documentation

- [Architecture Overview](overview.md) - System design and DI container setup
- [Development Guide](development.md) - Creating new components
