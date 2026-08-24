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

When KETE is disabled it removes itself from the event-listener list of every realm at start-up, so no events are delivered to it.

## Automatic Event Listener Registration

When KETE is enabled it registers itself at start-up: for every realm accepted by at least one route it adds the `kete` event listener, enables user events and admin events (with representation details) and enables **all** event types. Realms that no route accepts have the `kete` listener removed. No manual "Event Listeners" configuration in the Admin Console is required.

Only realms that exist when Keycloak starts are processed — after creating a new realm, restart Keycloak for KETE to register on it.

## Disabling Routes

To disable a specific route while keeping others active:

```bash
kete.routes.my-route.enabled=false
```

See [Routes](routes.md) for more on route configuration.

## Restart Required

!!! note "Restart Required"
    Configuration changes require a **Keycloak restart** to take effect. There is no hot-reload or on-the-fly configuration refresh.
