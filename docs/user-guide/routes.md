# Routes

Routes are the heart of KETE. They define **what** events go **where**.

## What is a Route?

Think of a route like a mail forwarding rule:

> "When I get a LOGIN event from the master realm, convert it to JSON and send it to Kafka."

Every route has five parts:

| Part | What it does | Required? |
|------|--------------|:---------:|
| **Realm Matchers** | Which realms to listen to | No |
| **Event Matchers** | Which events to forward | No |
| **Serializer** | What format to use | No (JSON default) |
| **Destination** | Where to send events | ✅ Yes |
| **Retry** | Retry on failure | No |

## How Routes Work

```mermaid
flowchart LR
    E["📥 Event"] --> M{"🔍 Match?"}
    M -->|No| D["🗑️ Skip"]
    M -->|Yes| S["📝 Format"]
    S --> Dest["🚀 Send"]
```

1. **Event arrives** from Keycloak
2. **Serializer formats** — the event is serialized once per distinct serializer (JSON, XML, etc.)
3. **Matchers filter** — does this route want this event?
4. **Destination sends** — deliver to Kafka, RabbitMQ, etc.

## Quick Example

Send login events to Kafka:

```bash
kete.routes.logins.event-matchers.filter=glob:LOGIN*
kete.routes.logins.destination.kind=kafka
kete.routes.logins.destination.bootstrap.servers=kafka:9092
kete.routes.logins.destination.topic=login-events
```

Without matchers, a route accepts all events from all realms.

## Disabling Routes

Routes are enabled by default. To disable a specific route:

```bash
kete.routes.my-route.enabled=false
```

To disable KETE entirely, see [Enabling & Disabling](enabling-disabling.md).

## Delivery Semantics

- Delivery happens **after the Keycloak transaction commits**, asynchronously on a virtual-thread executor — events never block the Keycloak request, and routes never see events from rolled-back transactions.
- Routes are independent: a failing route does not affect the others, and there is no ordering guarantee across routes.
- Delivery is best-effort: after the route's [retry](retry.md) attempts are exhausted the event is logged at `WARN` and dropped (there is no dead-letter queue).
- A route whose destination fails to initialize at start-up is dropped with `WARN Failed to initialize route : <name>`; the remaining routes start normally.

## Route Properties

| Property | Default | Description |
|----------|---------|-------------|
| `kete.routes.<name>.enabled` | `true` | Enable/disable the route |
| `kete.routes.<name>.realm-matchers.<id>` | _(all realms)_ | Realm matcher — see [Matchers](matchers/overview.md) |
| `kete.routes.<name>.realm-match-mode` | `any` | `any` or `all` |
| `kete.routes.<name>.event-matchers.<id>` | _(all events)_ | Event matcher — see [Matchers](matchers/overview.md) |
| `kete.routes.<name>.event-match-mode` | `any` | `any` or `all` |
| `kete.routes.<name>.serializer.kind` | `json` | Serializer — see [Serializers](serializers/overview.md) |
| `kete.routes.<name>.serializer.*` | - | Serializer-specific properties |
| `kete.routes.<name>.destination.kind` | _(required)_ | Destination — see [Destinations](destinations/overview.md) |
| `kete.routes.<name>.destination.*` | - | Destination-specific properties |
| `kete.routes.<name>.destination.authentication-type` | - | Destination authentication method (values are destination-specific) |
| `kete.routes.<name>.destination.headers.<header>` | - | Custom headers (`eventkind`, `eventtype`, `contenttype` are reserved and ignored) |
| `kete.routes.<name>.destination.content-encoding` | - | `gzip` or `deflate` — see [Content Encodings](content-encodings/overview.md) |
| `kete.routes.<name>.destination.content-transfer-encoding` | - | `base64` — see [Content Transfer Encodings](content-transfer-encodings/overview.md) |
| `kete.routes.<name>.destination.tls.*` | - | TLS/mTLS — see [TLS & mTLS](destinations/overview.md#tls-mtls) |
| `kete.routes.<name>.destination.pool.*` | - | Destination pool — see [Destination Pool](destinations/overview.md#destination-pool) |
| `kete.routes.<name>.retry.*` | - | Retry — see [Retry](retry.md) |

## Multiple Routes

Define as many routes as you need. Each works independently:

```bash
# Route 1: Everything to Kafka
kete.routes.all.destination.kind=kafka
kete.routes.all.destination.bootstrap.servers=kafka:9092
kete.routes.all.destination.topic=all-events

# Route 2: Errors to webhook
kete.routes.alerts.event-matchers.filter=glob:*_ERROR
kete.routes.alerts.destination.kind=http
kete.routes.alerts.destination.url=https://alerts.example.com/hook
```

## Match Modes

When you have multiple matchers, you can control how they combine:

| Mode | Behavior |
|------|----------|
| `any` | Event matches if **any** matcher accepts it (default) |
| `all` | Event matches only if **all** matchers accept it |

```bash
# Match mode for realm matchers
kete.routes.strict.realm-match-mode=all

# Match mode for event matchers
kete.routes.strict.event-match-mode=all
```

### Example: Match Multiple Event Types (any)

```bash
# Match LOGIN OR LOGOUT events (default mode is 'any')
kete.routes.auth.event-matchers.login=glob:LOGIN*
kete.routes.auth.event-matchers.logout=glob:LOGOUT*
kete.routes.auth.destination.kind=kafka
kete.routes.auth.destination.bootstrap.servers=kafka:9092
kete.routes.auth.destination.topic=auth-events
```

### Example: Require All Conditions (all)

```bash
# Match only events that match BOTH patterns
kete.routes.strict.event-match-mode=all
kete.routes.strict.event-matchers.must-be-login=glob:LOGIN*
kete.routes.strict.event-matchers.must-not-error=glob:not:*_ERROR
kete.routes.strict.destination.kind=http
kete.routes.strict.destination.url=https://api.example.com/events
```

## Retry

Retry is enabled by default (3 attempts, 500ms between retries). Customize retry settings per route:

```bash
kete.routes.reliable.retry.max-attempts=5
kete.routes.reliable.retry.wait-duration=PT2S
```

See [Retry](retry.md) for full configuration options and duration formats.
