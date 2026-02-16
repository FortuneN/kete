# Configuration

KETE reads configuration from **two sources**:

1. **Keycloak SPI Configuration** — XML/properties
2. **Environment Variables** — override SPI config

## Priority

Environment variables **override** SPI configuration. Use SPI for base settings and environment variables for per-deployment overrides.

## Keycloak SPI Configuration

For bare metal and standalone deployments, configure via Keycloak's native configuration.

=== "Quarkus Keycloak (v17+)"

    In `conf/keycloak.conf`:
    ```properties
    spi-events-listener-kete-enabled=true
    spi-events-listener-kete-metrics-enabled=true
    spi-events-listener-kete-routes-myroute-destination-kind=kafka
    spi-events-listener-kete-routes-myroute-destination-bootstrap-servers=localhost:9092
    spi-events-listener-kete-routes-myroute-destination-topic=keycloak-events
    ```

    Or via CLI:
    ```bash
    bin/kc.sh start \
      --spi-events-listener-kete-enabled=true \
      --spi-events-listener-kete-routes-myroute-destination-kind=kafka \
      ...
    ```

=== "Legacy WildFly Keycloak"

    In `standalone.xml`:
    ```xml
    <spi name="eventsListener">
        <provider name="kete" enabled="true">
            <properties>
                <property name="enabled" value="true"/>
                <property name="metrics.enabled" value="true"/>
                <property name="routes.myroute.destination.kind" value="kafka"/>
                <property name="routes.myroute.destination.bootstrap.servers" value="localhost:9092"/>
                <property name="routes.myroute.destination.topic" value="keycloak-events"/>
            </properties>
        </provider>
    </spi>
    ```

## Environment Variables

Environment variables override SPI configuration. Ideal for containerized deployments:

```bash
kete.enabled=true
kete.metrics.enabled=true
kete.routes.myroute.destination.kind=kafka
kete.routes.myroute.destination.bootstrap.servers=kafka:9092
kete.routes.myroute.destination.topic=keycloak-events
```

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
