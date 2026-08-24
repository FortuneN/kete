# How KETE Works

Visual guide to KETE's event processing. For detailed technical information, see [Architecture](architecture.md).

---

## The Big Picture

KETE plugs into Keycloak's Event Listener SPI. When something happens in Keycloak (login, user created, etc.), Keycloak fires an event. KETE catches that event and routes it to configured destinations.

```mermaid
flowchart LR
    KC[" Keycloak"] -->|event| KETE[" KETE"]
    KETE -->|route 1| D1["Kafka"]
    KETE -->|route 2| D2["HTTP"]
    KETE -->|route 3| D3["RabbitMQ"]
```

---

## Event Processing Flow

### User Events (LOGIN, LOGOUT, etc.)

```mermaid
flowchart TD
    A["User action in Keycloak"] --> B["Keycloak generates Event"]
    B --> C["KETE Provider.onEvent()"]
    C --> D["Queue in EventListenerTransaction"]
    D --> E{"Transaction commits?"}
    E -->|Yes| F["Process event"]
    E -->|No| G["Discard event"]
    F --> H["For each route"]
    H --> I{"Realm matches?"}
    I -->|No| J["Skip route"]
    I -->|Yes| K{"Event type matches?"}
    K -->|No| J
    K -->|Yes| L["Serialize event"]
    L --> M["Send to destination"]
```

### Admin Events (USER_CREATE, REALM_UPDATE, etc.)

Same flow but uses `onEvent(AdminEvent, boolean)` and the event type is `RESOURCETYPE_OPERATIONTYPE` (e.g. `USER_CREATE`).

---

## Component Lifecycle

```mermaid
flowchart TD
    subgraph Startup
        A["Keycloak starts"] --> B["ProviderFactory.init()"]
        B --> C["ProviderFactory.postInit()"]
        C --> C2["Store session factory & self-register"]
        C2 --> D["PostMigrationEvent triggers run()"]
        D --> E["Scan for @Component classes"]
        E --> F["Parse configuration"]
        F --> G["Initialize routes"]
        G --> H["Initialize destinations"]
        H --> I["Register event listener on realms"]
    end
    
    subgraph Runtime
        I["Event received"] --> J["Provider created"]
        J --> K["Event processed"]
        K --> L["Provider discarded"]
    end
    
    subgraph Shutdown
        M["Keycloak stops"] --> N["ProviderFactory.close()"]
        N --> O["Shutdown executor (wait for in-flight deliveries)"]
        O --> P["Close all routes and destinations"]
    end
    
    Startup --> Runtime
    Runtime --> Shutdown
```

---

## Route Matching

Each route has realm matchers and event matchers:

```mermaid
flowchart LR
    E["Event"] --> R{"Realm<br/>matches?"}
    R -->|No| X["Skip"]
    R -->|Yes| M{"Event type<br/>matches?"}
    M -->|No| X
    M -->|Yes| S["Serialize"]
    S --> D["Send to destination"]
```

### Match Modes

- **ANY** (default): Accept if ANY matcher matches (OR)
- **ALL**: Accept only if ALL matchers match (AND)

---

## Destination Pooling

All destinations use Apache Commons Pool2:

```mermaid
flowchart LR
    subgraph Pool["Destination Pool"]
        C1["Client 1"]
        C2["Client 2"]
        C3["Client 3"]
    end
    
    T1["Thread 1"] --> B1["Borrow"] --> C1
    C1 --> R1["Return"] --> Pool
    
    T2["Thread 2"] --> B2["Borrow"] --> C2
    C2 --> R2["Return"] --> Pool
```

Pool sizes are configurable per route:
- `pool.min-idle`: Default 1
- `pool.max-idle`: Default 10
- `pool.max-total`: Default 20

---

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Transaction-aware** | Events only published after Keycloak transaction commits |
| **Virtual threads** | Parallel destination delivery without blocking |
| **Destination pooling** | Predictable resource usage, virtual thread compatibility |
| **Matcher caching** | O(1) lookup after first match per event type |
| **Serializer singletons** | One instance shared across routes (except `template`, `multipart-form` and `url-encoded-form`, which are transient) |

---

## Error Handling

| Error Type | Behavior |
|------------|----------|
| Destination connection failure | Logged, route skipped |
| Serialization error | Logged, affects all routes using that serializer |
| Matcher evaluation error | Logged as a send failure (`kete.events.failed.total`); the event is skipped for that route |
| Message send failure | Retry if configured, otherwise logged |

---

## See Also

- [Architecture](architecture.md) — Detailed technical design
- [Configuration](configuration.md) — All configuration options
- [Transaction Support](transaction-support.md) — Transaction handling details
