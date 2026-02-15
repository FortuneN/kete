# Observability

KETE provides opt-in metrics and always-on logging.

## Metrics

Enable metrics to expose KETE statistics at Keycloak's `/metrics` endpoint:

```bash
kete.metrics.enabled=true
```

### Available Metrics

| Metric | Labels | Description |
|--------|--------|-------------|
| `kete.events.forwarded.total` | route, event_type, realm | Events successfully sent |
| `kete.events.failed.total` | route, event_type, realm, error_type | Events that failed |
| `kete.forward.duration.seconds` | route | Time to send event |
| `kete.routes.active` | — | Number of active routes |
| `kete.pool.idle` | route | Idle connections in pool |
| `kete.pool.active` | route | Active connections in pool |
| `kete.pool.total` | route | Maximum pool size |

### Prometheus Example

```yaml
scrape_configs:
  - job_name: 'keycloak'
    static_configs:
      - targets: ['keycloak:8080']
    metrics_path: '/metrics'
```

## Logging

KETE logs lifecycle events at INFO level (always enabled):

```
INFO  kete (1.0.0) initializing
INFO  kete Route 'my-route' initialized: destination=KafkaDestination, serializer=JsonSerializer, realmMatchers=1, eventMatchers=2
INFO  kete initialized
...
INFO  kete closing
INFO  kete closed
```

Use standard Keycloak/Quarkus logging configuration to adjust log levels:

```bash
# Quarkus Keycloak
quarkus.log.category."io.github.fortunen.kete".level=DEBUG
```
