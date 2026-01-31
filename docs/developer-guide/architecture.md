# Architecture

## Overview

KETE follows a pipeline architecture for processing Keycloak events:

```
Keycloak Event → Provider → Routes[] → (Matchers → Serializer → Destination)
```

### Core Flow

1. **Keycloak generates an Event or AdminEvent**
2. **ProviderFactory receives the event** via EventListenerTransaction
3. **On transaction commit**, events are processed in parallel (virtual threads)
4. **Each route evaluates its matchers** against the event type
5. **Matching events are serialized** using the route's serializer
6. **Serialized messages are sent** to the route's destination

### Dependency Injection

KETE uses a lightweight, custom dependency injection system (`IocUtils`) instead of heavyweight frameworks like Spring or CDI:

**Why Custom DI?**
- **Simplicity**: Our needs are straightforward—scan for `@Component`, instantiate singletons
- **No Framework Overhead**: Keycloak already loads the JVM; adding Spring/CDI would be excessive
- **Fast Startup**: Minimal reflection, no XML parsing, no proxy generation
- **Keycloak Native**: Works seamlessly as a Keycloak provider extension without conflicts

The implementation is ~200 lines of code that:
1. Scans `io.github.fortunen.kete` package for `@Component` annotations
2. Registers SINGLETON and TRANSIENT scopes
3. Instantiates components with constructor injection

This approach keeps the plugin lightweight while still providing clean component management.



## Event Processing

### Transaction Support

Events are wrapped in Keycloak's `EventListenerTransaction`:

- Events are queued during the Keycloak transaction
- On commit: Consumer/BiConsumer callbacks are invoked
- On rollback: Events are discarded

This ensures events are only processed for **successful operations**.

### Route Matching Algorithm

1. Check if event realm matches route realm
2. Check if route has matchers and if event type is accepted
3. For matching routes: serialize once per serializer, send to all destinations
4. Use virtual threads for parallel destination delivery
5. Apply retry if configured



## Threading Model

### Thread Stages

| Stage | Thread Type | Description |
|-------|-------------|-------------|
| Event Reception | Keycloak transaction thread | Event received from Keycloak |
| Transaction Commit | Same thread | Triggers event processing |
| Event Processing | Virtual thread per route | Parallel destination delivery |
| Destination Pooling | ThreadLocal | Thread-safe client reuse |

### Virtual Threads

- Used for parallel destination delivery
- Executor created in `ProviderFactory.run()`
- Executor: `Executors.newVirtualThreadPerTaskExecutor()`



## Performance Optimizations

### 1. Matcher Result Caching

- Cache in `Route.accept()`
- Max 1,000 entries per route
- Cache key: eventType string
- O(1) lookup after first match

### 2. Template Result Caching

- Cache in TemplateUtils
- Max 10,000 entries
- Cache key: template + message properties
- Reduces string interpolation overhead

### 3. Serializer Singletons

- All serializers are SINGLETON scoped
- ObjectWriter instances are thread-safe
- One serializer instance shared across routes

### 4. Destination Pooling

**All destinations use Apache Commons Pool2 for connection/client pooling.** This is a deliberate architectural decision to ensure consistency, maintainability, and predictable behavior across all destination types.

#### Why Standardized Pooling?

1. **Virtual Thread Compatibility**: ThreadLocal-based approaches cause unbounded connection growth with virtual threads (fire-and-forget threads don't trigger cleanup)
2. **Consistency**: One pooling pattern to understand, test, and debug across all destinations
3. **Predictable Resource Usage**: Fixed pool sizes prevent broker connection sprawl
4. **No Special Cases**: Even when underlying libraries have their own pooling (e.g., HttpClient, KafkaProducer), we wrap them in Commons Pool2 for uniformity

#### Pool Configuration

Pool sizes are configurable per route via `min-pool-size` and `max-pool-size` properties:

```bash
kete.routes.myroute.destination.min-pool-size=5   # Default: 5
kete.routes.myroute.destination.max-pool-size=20  # Default: 20
```

| Setting | Default | Configurable | Rationale |
|---------|---------|:------------:|-----------|
| `min-pool-size` | 5 |  | Warm pool for typical workloads |
| `max-pool-size` | 20 |  | Prevent broker overload |
| Block on Exhaustion | true |  | Wait rather than fail |
| Borrow Timeout | 30 seconds |  | Fail fast if pool starved |
| Test on Borrow | true |  | Validate before use |
| Test on Return | true |  | Validate after use |

**Constraints:**
- `min-pool-size` must be > 0
- `max-pool-size` must be > 0
- `max-pool-size` must be >= `min-pool-size`

#### Pooled Objects by Destination

| Destination | Pooled Object | Notes |
|-------------|---------------|-------|
| MQTT 3 | `MqttClient` (Paho v3) | Individual client instances |
| MQTT 5 | `MqttClient` (Paho v5) | Individual client instances |
| AMQP 0.9.1 | `Channel` | Channels from shared Connection |
| AMQP 1.0 | `JMSContext` | JMS connections via Qpid |
| Kafka | `KafkaProducer` | Producer instances |
| HTTP | `HttpClient` | HTTP client instances |

#### Usage Pattern

```java
// Borrow from pool
Client client = null;
try {
    client = clientPool.borrowObject();
    client.send(message);
} finally {
    if (client != null) {
        clientPool.returnObject(client);
    }
}
```

#### Lifecycle

- **Pool created**: In `initialize()` after configuration is validated
- **Test connection**: Borrow and return one object to verify connectivity
- **Pool closed**: In `close()` which closes all pooled objects

### 5. EventMessage Caching

- Cached lowercase/uppercase transformations
- Cached byte[] conversions for headers
- Reduces repeated string operations



## Error Handling

### Initialization Errors

- Thrown immediately, prevents route activation
- Logged with full stack trace
- Keycloak continues without the failed route

### Message Delivery Errors

- **With retry**: Retry mechanism handles retries with exponential backoff
- **Without retry**: Exception logged and swallowed
- Does not block other routes or events

### Connection Errors

| Destination | Recovery Strategy |
|-------------|-------------------|
| MQTT | Automatic reconnect enabled |
| RabbitMQ | Automatic recovery enabled |
| Kafka | Producer retries built-in |
| HTTP | Configurable retry with backoff |



## Component Lifecycle

### Initialization Order

1. `init(Scope config)` - Basic initialization
2. `postInit(KeycloakSessionFactory)` - Register for PostMigrationEvent
3. `onEvent(PostMigrationEvent)` - Triggers run() after DB migration
4. `run(KeycloakSession)` - Parse config, initialize routes

### Shutdown Order

1. `close()` - Called on Keycloak shutdown
2. Virtual thread executor shutdown
3. All destinations closed (connections released)



## Configuration Loading

Configuration is loaded from **two sources** and merged:

1. **Keycloak SPI Configuration** (Scope) - XML/properties configuration
   - Quarkus: `conf/keycloak.conf` or CLI arguments with `--spi-events-listener-kete-*`
   - WildFly: `standalone.xml` under `<spi name="eventsListener"><provider name="kete">`
2. **Environment Variables** - Override SPI config with `kete.*` prefix

Environment variables take precedence, allowing base config in XML with per-deployment overrides via environment.

See [Configuration Reference](configuration.md) for details.



## Data Flow

### EventMessage Record

The immutable `EventMessage` record carries event data through the pipeline:

```java
record EventMessage(
    String realm,           // Keycloak realm name
    String eventId,         // Unique event identifier
    byte[] eventBody,       // Serialized event content
    String eventType,       // Event type (e.g., LOGIN, LOGOUT)
    String contentType,     // MIME type from serializer
    String resourceType,    // Admin event resource type
    String kind,            // "EVENT" or "ADMIN_EVENT"
    String operationType,   // Admin event operation (e.g., CREATE, UPDATE)
    String result           // "SUCCESS" or "ERROR"
)
```

### Helper Methods

EventMessage provides cached transformations for performance:

- `kindLowerCase()`, `kindUpperCase()` - Cached kind transformations
- `realmLowerCase()`, `realmUpperCase()` - Cached realm transformations
- `eventTypeLowerCase()`, `eventTypeUpperCase()` - Cached event type transformations
- `resourceTypeLowerCase()`, `resourceTypeUpperCase()` - Cached resource type transformations
- `operationTypeLowerCase()`, `operationTypeUpperCase()` - Cached operation type transformations
- `resultLowerCase()`, `resultUpperCase()` - Cached result transformations
- `eventTypeBytes()`, `contentTypeBytes()` - UTF-8 byte arrays for headers

