# Configuration

KETE reads its configuration from the **environment variables** of the Keycloak process. Every setting is an environment variable whose name is the dotted property key (for example `kete.routes.myroute.destination.kind`) and whose value is the setting value.

!!! note "Keycloak SPI options are not read"
    Keycloak exposes provider options to the extension as `kc.spi-events-listener-kete-…` keys in dash-case. KETE looks for dotted `kete.*` keys, so options placed in `keycloak.conf`, passed as `--spi-events-listener-kete-…` CLI flags or as `-D` system properties are **not** picked up. Use environment variables (or a container/orchestrator secret/config map that becomes an environment variable).

## Environment Variables

Names contain dots and hyphens, so set them with a tool that accepts arbitrary variable names — Docker/Compose `environment:`, Kubernetes `env:`/`envFrom:`, systemd `Environment=`, or `env 'name=value' kc.sh start`. Plain `export name=value` is rejected by POSIX shells because of the dots.

```bash
kete.enabled=true
kete.metrics.enabled=true
kete.routes.myroute.destination.kind=kafka
kete.routes.myroute.destination.bootstrap.servers=kafka:9092
kete.routes.myroute.destination.topic=keycloak-events
```

Values are trimmed of surrounding whitespace. Unknown keys are ignored.

## Configuration Pattern

All settings follow this pattern:

```
kete.<setting>=<value>
kete.routes.<route-name>.<setting>=<value>
```

## Global Settings

| Property | Default | Description |
|----------|---------|-------------|
| `kete.enabled` | `true` | Master switch for KETE |
| `kete.metrics.enabled` | `false` | Enable metrics at `/metrics` endpoint |
| `kete.support-the-project-message` | `true` | Show support/sponsorship message in Keycloak logs on startup |

!!! note "Restart Required"
    Configuration changes require a **Keycloak restart**. There is no hot-reload.

## Best Practice

Use SPI configuration for base/default settings and environment variables for per-environment overrides (dev, staging, production).
