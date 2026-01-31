# Enabling & Disabling

KETE and all routes are **enabled by default**.

## Disabling KETE

To disable KETE entirely (no events will be forwarded):

```bash
kete.enabled=false
```

| Property | Default | Description |
|----------|---------|-------------|
| `kete.enabled` | `true` | Master switch for KETE |

## Disabling Routes

To disable a specific route while keeping others active:

```bash
kete.routes.my-route.enabled=false
```

See [Routes](routes.md) for more on route configuration.

## Restart Required

!!! note "Restart Required"
    Configuration changes require a **Keycloak restart** to take effect. There is no hot-reload or on-the-fly configuration refresh.
