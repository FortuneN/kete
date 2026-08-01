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



## Design Principles

### Destination Isolation

No destination may force architectural changes beyond its own boundaries. The shared architecture — base classes, pipeline, serializers, matchers, pooling, DI, config loading — exists to serve all destinations equally.

If a destination's requirements cannot be met within its own package (`Destination.java`, `DestinationConfig.java`, and destination-specific utilities), then that destination is not supported. We would sooner drop a destination than allow it to pollute the shared architecture.

**Rules:**

- No shared class modifications to accommodate a single destination
- No `if destination is X` branches in shared code — if you're writing conditional logic in a base class for one destination's quirk, the design is wrong
- Utility classes shared between related destinations are fine (e.g., a signing utility shared between two destinations from the same vendor), but they live in `utils/` and have zero knowledge of the destination classes that call them
- This applies to all layers: source code, config validation, serialization, test infrastructure, documentation tooling

**Why this matters:**

- KETE has 29 destinations (and counting). If one gets a shared-code exception, every future destination will argue "just this one thing." The shared code becomes a graveyard of special cases.
- Every shared-code change is a regression risk across ALL destinations. A tweak for one could break any of the others.
- The principle is already proven — destinations with complex auth, custom signing, and credential lifecycle all work today without touching shared code. No destination is different.
- This is the same constraint every serious plugin system enforces: VS Code extensions can't modify VS Code core, browser extensions can't modify the engine, Keycloak providers can't modify Keycloak internals. KETE destinations can't modify the KETE pipeline.
- "We'd sooner drop a destination" sounds harsh in isolation, but it's the only statement strong enough to prevent erosion. A softer version like "try to avoid" becomes "well, just this once" on the first hard case. If a destination genuinely can't work within its own boundary, that's a signal it doesn't belong — not a reason to weaken the architecture for everything else.



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
| Destination Pooling | Apache Commons Pool2 | Thread-safe client reuse |

### Virtual Threads

- Used for parallel destination delivery
- Executor created in `ProviderFactory.run()`
- Executor: `Executors.newVirtualThreadPerTaskExecutor()`



## Performance Optimizations

### 1. Matcher Result Caching

- Two caches in `Route.accept()`: `acceptRealmCache` and `acceptEventCache`
- Max 1,000 entries per cache per route
- Cache key: realm name or event type string
- O(1) lookup after first match

### 2. Template Result Caching

- Cache in TemplateUtils
- Max 10,000 entries
- Cache key: template + message properties
- Reduces string interpolation overhead

### 3. Serializer Singletons

- 10 of 13 serializers are SINGLETON scoped (TemplateSerializer, MultipartFormSerializer, and UrlEncodedFormSerializer are TRANSIENT)
- ObjectWriter instances are thread-safe
- One serializer instance shared across routes (except TemplateSerializer)

### 4. Destination Pooling

**All destinations use Apache Commons Pool2 for connection/client pooling.** This is a deliberate architectural decision to ensure consistency, maintainability, and predictable behavior across all destination types.

#### Why Standardized Pooling?

1. **Virtual Thread Compatibility**: ThreadLocal-based approaches cause unbounded connection growth with virtual threads (fire-and-forget threads don't trigger cleanup)
2. **Consistency**: One pooling pattern to understand, test, and debug across all destinations
3. **Predictable Resource Usage**: Fixed pool sizes prevent broker connection sprawl
4. **No Special Cases**: Even when underlying libraries have their own pooling (e.g., HttpClient, KafkaProducer), we wrap them in Commons Pool2 for uniformity

#### Pool Configuration

Pool sizes are configurable per route via `pool.*` properties:

```bash
kete.routes.myroute.destination.pool.min-idle=1     # Default: 1
kete.routes.myroute.destination.pool.max-idle=10    # Default: 10
kete.routes.myroute.destination.pool.max-total=20   # Default: 20
```

| Setting | Default | Configurable | Rationale |
|---------|---------|:------------:|----------|
| `pool.min-idle` | 1 |  | Warm pool for typical workloads |
| `pool.max-idle` | 10 |  | Limit idle connections |
| `pool.max-total` | 20 |  | Prevent broker overload |
| Block on Exhaustion | true |  | Wait rather than fail |
| `pool.max-wait-seconds` | -1 (infinite) |  | Wait indefinitely for available object |
| `pool.test-on-borrow` | true |  | Cull unhealthy instances at borrow (cheap `isHealthy()` state check) |
| `pool.test-on-return` | true |  | Cull instances whose connection died during send |

**Constraints:**
- `pool.min-idle` must be > 0
- `pool.max-idle` must be > 0
- `pool.max-total` must be > 0
- `pool.max-total` must be >= `pool.min-idle`

#### Pooled Objects by Destination

All 29 destinations use Apache Commons Pool2. Each destination pools its primary client/connection object:

| Destination(s) | Pooled Object | Notes |
|----------------|---------------|-------|
| MQTT 3 | `MqttClient` (Paho v3) | Individual client instances |
| MQTT 5 | `MqttClient` (Paho v5) | Individual client instances |
| AMQP 0.9.1 | `Channel` | Channels from shared Connection |
| AMQP 1.0 | `JMSContext` | JMS connections via Qpid |
| Kafka | `KafkaProducer` | Producer instances |
| HTTP | `HttpClient` | HTTP client instances |
| WebSocket, Socket.io, SignalR | Client session | Persistent connection instances |
| Redis Pub/Sub, Redis Stream | `StatefulRedisConnection` (Lettuce) | Redis client instances |
| NATS, NATS JetStream | `Connection` | NATS connections |
| Pulsar | `Producer` | Pulsar producer instances |
| STOMP | `StompSession` | STOMP WebSocket sessions |
| ZeroMQ | `ZMQ.Socket` | ZeroMQ socket instances |
| AWS (SNS, SQS, Kinesis, EventBridge) | SDK client | AWS service clients |
| Azure (Event Hubs, Service Bus, Event Grid, Storage Queue, Web PubSub) | SDK client | Azure service clients |
| GCP (Pub/Sub, Cloud Tasks) | SDK client | GCP service clients |
| gRPC | `ManagedChannel` | gRPC channel instances |
| SOAP | `HttpClient` | Java HTTP client instances |

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

- 6 LRU caches: BYTES, LOWERCASE, UPPERCASE, KEBAB, PASCAL, CAMEL
- Cached case transformations (lower, upper, kebab, pascal, camel) for all fields
- Cached byte[] conversions for headers
- Reduces repeated string operations



## Error Handling

### Initialization Errors

- Thrown immediately, prevents route activation
- Logged with full stack trace
- Keycloak continues without the failed route

### Message Delivery Errors

- **With retry** (default): Retry mechanism handles retries with configurable wait duration
- **Without retry**: Exception logged and swallowed
- Does not block other routes or events

### Connection Errors

| Destination | Recovery Strategy |
|-------------|-------------------|
| MQTT | Automatic reconnect enabled |
| RabbitMQ | Automatic recovery enabled |
| Kafka | Producer retries built-in |
| HTTP | Configurable route-level retry |



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
- `kindKebabCase()`, `kindPascalCase()`, `kindCamelCase()` - Cached kind case conversions
- `realmLowerCase()`, `realmUpperCase()` - Cached realm transformations
- `realmKebabCase()`, `realmPascalCase()`, `realmCamelCase()` - Cached realm case conversions
- `eventTypeLowerCase()`, `eventTypeUpperCase()` - Cached event type transformations
- `eventTypeKebabCase()`, `eventTypePascalCase()`, `eventTypeCamelCase()` - Cached event type case conversions
- `resourceTypeLowerCase()`, `resourceTypeUpperCase()` - Cached resource type transformations
- `resourceTypeKebabCase()`, `resourceTypePascalCase()`, `resourceTypeCamelCase()` - Cached resource type case conversions
- `operationTypeLowerCase()`, `operationTypeUpperCase()` - Cached operation type transformations
- `operationTypeKebabCase()`, `operationTypePascalCase()`, `operationTypeCamelCase()` - Cached operation type case conversions
- `resultLowerCase()`, `resultUpperCase()` - Cached result transformations
- `resultKebabCase()`, `resultPascalCase()`, `resultCamelCase()` - Cached result case conversions
- `kindBytes()`, `eventTypeBytes()`, `contentTypeBytes()` - UTF-8 byte arrays for headers

Case conversions assume `UPPER_UNDERSCORE` input (Keycloak's native format). For example, `LOGIN_ERROR` becomes `login-error` (kebab), `LoginError` (pascal), `loginError` (camel).

