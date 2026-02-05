# Retry

By default, if a destination is unreachable, the event is lost. Enable retry for more reliable delivery.

## Configuration

Retry is configured at the **route level**, not within the destination:

```bash
kete.routes.reliable.retry.enabled=true
kete.routes.reliable.retry.max-attempts=5
kete.routes.reliable.retry.wait-duration=PT2S
```

| Property | Default | Description |
|----------|---------|-------------|
| `retry.enabled` | `false` | Enable retry |
| `retry.max-attempts` | `3` | How many times to retry |
| `retry.wait-duration` | `500ms` | Time between retries |

## Retry Behavior

- **Retries on**: Network errors, timeouts, 5xx server errors
- **No retry on**: 4xx client errors (authentication failures, bad requests)

## Duration Formats

The `wait-duration` property accepts multiple formats:

| Format | Example | Meaning |
|--------|---------|---------|
| Seconds (digits only) | `15` | 15 seconds |
| With suffix | `100ms` | 100 milliseconds |
| With suffix | `30s` | 30 seconds |
| With suffix | `15m` | 15 minutes |
| With suffix | `2h` | 2 hours |
| With suffix | `7d` | 7 days |
| With suffix | `2w` | 2 weeks |
| ISO 8601 | `PT1S` | 1 second |
| ISO 8601 | `PT2H30M` | 2 hours 30 minutes |
| ISO 8601 | `P1D` | 1 day |

## Example

HTTP destination with retry:

```bash
kete.routes.api.destination.kind=http
kete.routes.api.destination.url=https://api.example.com/events
kete.routes.api.destination.method=POST
kete.routes.api.retry.enabled=true
kete.routes.api.retry.max-attempts=3
kete.routes.api.retry.wait-duration=PT1S
```
