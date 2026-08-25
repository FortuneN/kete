# Observability

KETE provides opt-in metrics and always-on logging.

## Metrics

KETE registers its meters with Keycloak's Micrometer registry. Two switches are needed: Keycloak's own metrics (`--metrics-enabled=true` / `KC_METRICS_ENABLED=true`, exposed on the management port `9000` at `/metrics`) and KETE's:

```bash
kete.metrics.enabled=true
```

### Available Metrics

| Metric | Labels | Description |
|--------|--------|-------------|
| `kete.events.forwarded.total` | route, event_type, realm | Events successfully sent |
| `kete.events.failed.total` | route, event_type, realm, error_type | Events that failed |
| `kete.events.serialization.failed.total` | serializer, event_type, realm, error_type | Events that could not be serialized (never reach a destination) |
| `kete.forward.duration.seconds` | route | Time to send event |
| `kete.routes.active` | — | Number of active routes |
| `kete.routes.failed` | — | Routes that failed to initialize at start-up |
| `kete.pool.idle` | route | Idle connections in pool |
| `kete.pool.active` | route | Active connections in pool |
| `kete.pool.total` | route | Maximum pool size |

### Prometheus Example

```yaml
scrape_configs:
  - job_name: 'keycloak'
    static_configs:
      - targets: ['keycloak:9000']
    metrics_path: '/metrics'
```

`error_type` is the simple class name of the exception that failed the delivery (for example `IOException`, `ConnectException` or `MaxRetriesExceededException`).

## Logging

KETE logs lifecycle events at INFO level (always enabled):

```
INFO  kete (1.0.0) initializing
INFO  kete Route 'my-route' initialized: destination=kafka, serializer=json, realmMatchers=1, eventMatchers=2
INFO  kete initialized
...
INFO  kete closing
INFO  kete closed
```

Messages worth alerting on (all `WARN`):

| Message | Meaning |
|---------|---------|
| `Failed to initialize route : <name>` | The route's destination could not be initialized; the route is skipped |
| `Failed to send <type> : <id> : to route : <name>` | Delivery failed after all retry attempts; the event is dropped |
| `event executor did not terminate gracefully within 30 seconds` | Shutdown timed out waiting for in-flight deliveries |

`INFO kete (<version>) disabled` is logged when `kete.enabled=false`. A support/sponsorship banner is logged at start-up unless `kete.support-the-project-message=false`.

Use standard Keycloak/Quarkus logging configuration to adjust log levels:

```bash
# Quarkus Keycloak
quarkus.log.category."io.github.fortunen.kete".level=DEBUG
```
