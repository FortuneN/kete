# Retry

Retry is enabled by default. If a destination is temporarily unreachable, KETE automatically retries delivery.

## Configuration

Retry is configured at the **route level**, not within the destination:

```bash
kete.routes.reliable.retry.max-attempts=5
kete.routes.reliable.retry.wait-duration=PT2S
```

| Property | Default | Description |
|----------|---------|-------------|
| `retry.enabled` | `true` | Enable retry |
| `retry.max-attempts` | `3` | Maximum number of attempts (including initial call); must be an integer > 0 |
| `retry.wait-duration` | `500ms` | Time between retries; must be a non-zero duration |

## Retry Behavior

Retry uses [Resilience4j](https://resilience4j.readme.io/docs/retry). By default, any exception thrown during event delivery triggers a retry (up to `max-attempts`). There is no distinction between error types — all failures are retried equally. Every failed attempt discards the borrowed destination instance (its connection is closed) and the next attempt borrows or creates a fresh one from the pool. Invalid retry values fail route creation at start-up.

To disable retry for a specific route:

```bash
kete.routes.unreliable.retry.enabled=false
```

## Duration Formats

The `wait-duration` property accepts multiple formats:

| Format | Example | Meaning |
|--------|---------|---------|
| Seconds (digits only) | `15` | 15 seconds |
| With suffix | `500ns` | 500 nanoseconds |
| With suffix | `100us` | 100 microseconds |
| With suffix | `100ms` | 100 milliseconds |
| With suffix | `30s` | 30 seconds |
| With suffix | `15m` | 15 minutes |
| With suffix | `2h` | 2 hours |
| With suffix | `7d` | 7 days |
| With suffix | `2w` | 2 weeks |
| With suffix | `1y` | 1 year |
| ISO 8601 | `PT1S` | 1 second |
| ISO 8601 | `PT2H30M` | 2 hours 30 minutes |
| ISO 8601 | `P1D` | 1 day |

## Example

HTTP destination with retry (using non-default values):

```bash
kete.routes.api.destination.kind=http
kete.routes.api.destination.url=https://api.example.com/events
kete.routes.api.destination.method=POST
kete.routes.api.retry.max-attempts=5
kete.routes.api.retry.wait-duration=PT2S
```
