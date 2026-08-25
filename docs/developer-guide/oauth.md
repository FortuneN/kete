# OAuth 2.0 Client Credentials

## Overview

KETE ships one OAuth 2.0 **Client Credentials** implementation (`OAuthMaterial`) that is shared by every destination with an `oauth` authentication type: `http`, `soap`, `websocket`, `signalr`, `socketio` and `pulsar`. It supports two modes:

- **Internal Mode**: uses the current Keycloak instance as the authorization server and auto-registers a service-account client
- **External Mode** (default): uses any external OAuth 2.0 authorization server

Features:

- Automatic token retrieval with the Client Credentials grant (Nimbus `oauth2-oidc-sdk`)
- Thread-safe token caching with expiry tracking; tokens are refreshed 30 seconds before they expire
- Default lifetime of 3600 seconds when the token response carries no `expires_in`



## Activation

OAuth is only wired in when the destination's `authentication-type` is `oauth`. The `oauth.*` properties are ignored otherwise.

```bash
kete.routes.<name>.destination.authentication-type=oauth
kete.routes.<name>.destination.oauth.enabled=true
```



## OAuth Modes

### External Mode (Default)

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `oauth.enabled` | Yes | `false` | Must be `true` |
| `oauth.mode` | No | `external` | `external` or `internal` (case-insensitive; unknown values fall back to `external`) |
| `oauth.token-url` | Yes | - | Token endpoint URL |
| `oauth.client-id` | Yes | - | Client ID |
| `oauth.client-secret` | Yes | - | Client secret |
| `oauth.timeout-seconds` | No | _(none)_ | Connect and read timeout in seconds for the token request; applied only when set |
| `oauth.scope` | No | `""` | Requested scopes (space-separated) |

```bash
kete.routes.api.destination.authentication-type=oauth
kete.routes.api.destination.oauth.enabled=true
kete.routes.api.destination.oauth.mode=external
kete.routes.api.destination.oauth.token-url=https://auth.example.com/token
kete.routes.api.destination.oauth.client-id=my-client
kete.routes.api.destination.oauth.client-secret=my-secret
```

### Internal Mode

Uses the current Keycloak instance as the authorization server. When no `oauth.client-secret` is configured, KETE registers a confidential client with service accounts enabled in the configured realm during initialization (or reuses it if it already exists, adopting the secret stored on that client so token requests keep working after a restart). When you provide `oauth.client-secret`, no registration takes place — the client must already exist in the realm.

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `oauth.enabled` | Yes | `false` | Must be `true` |
| `oauth.mode` | Yes | - | Must be `internal` |
| `oauth.realm` | Yes | - | Realm that hosts the client and issues the tokens |
| `oauth.token-url` | No | `http://localhost:8080/realms/<realm>/protocol/openid-connect/token` | Override when Keycloak is not reachable on `localhost:8080` from inside the Keycloak process |
| `oauth.client-id` | No | `kete-oauth-client` | Client ID to register/reuse |
| `oauth.client-secret` | No | Auto-generated (UUID) | Client secret to register/reuse |
| `oauth.timeout-seconds` | No | _(none)_ | Connect and read timeout in seconds for the token request; applied only when set |
| `oauth.scope` | No | `""` | Requested scopes |

```bash
kete.routes.api.destination.authentication-type=oauth
kete.routes.api.destination.oauth.enabled=true
kete.routes.api.destination.oauth.mode=internal
kete.routes.api.destination.oauth.realm=master
```

This automatically:

1. Generates a client secret (UUID)
2. Creates the confidential client `kete-oauth-client` in the `oauth.realm` realm with that secret, or reuses the client if it already exists
3. Enables its service account (client credentials grant)
4. Derives the token URL for the realm unless `oauth.token-url` is set

If the realm does not exist, or client registration fails, a `WARN` is logged and the route still starts; every token request then fails at send time (`OAuth token request failed: …`), which surfaces as delivery failures for that route.



## Usage Examples

### Example 1: Internal Mode (Same Keycloak)

```bash
kete.routes.internal-api.realm-matchers.realm=list:master
kete.routes.internal-api.destination.kind=http
kete.routes.internal-api.destination.url=https://api.internal.com/events
kete.routes.internal-api.destination.authentication-type=oauth
kete.routes.internal-api.destination.oauth.enabled=true
kete.routes.internal-api.destination.oauth.mode=internal
kete.routes.internal-api.destination.oauth.realm=master
kete.routes.internal-api.event-matchers.filter=glob:*
```

### Example 2: External Mode (External OAuth Provider)

```bash
kete.routes.external-api.realm-matchers.realm=list:master
kete.routes.external-api.destination.kind=http
kete.routes.external-api.destination.url=https://api.external.com/events
kete.routes.external-api.destination.authentication-type=oauth
kete.routes.external-api.destination.oauth.enabled=true
kete.routes.external-api.destination.oauth.mode=external
kete.routes.external-api.destination.oauth.token-url=https://auth.example.com/oauth/token
kete.routes.external-api.destination.oauth.client-id=keycloak-integration
kete.routes.external-api.destination.oauth.client-secret=super-secret
kete.routes.external-api.destination.oauth.scope=events:write
kete.routes.external-api.event-matchers.filter=glob:*
```

### Example 3: Kubernetes ConfigMap and Secret

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: keycloak-events-config
data:
  kete.routes.protected-api.destination.kind: "http"
  kete.routes.protected-api.destination.url: "https://api.company.com/events"
  kete.routes.protected-api.destination.authentication-type: "oauth"
  kete.routes.protected-api.destination.oauth.enabled: "true"
  kete.routes.protected-api.destination.oauth.token-url: "https://keycloak.company.com/realms/production/protocol/openid-connect/token"
  kete.routes.protected-api.destination.oauth.client-id: "keycloak-events-publisher"
  kete.routes.protected-api.destination.oauth.scope: "api:write events:publish"
---
apiVersion: v1
kind: Secret
metadata:
  name: keycloak-events-secrets
type: Opaque
stringData:
  oauth-client-secret: "your-secure-client-secret-here"
```

```yaml
# In your Keycloak deployment
env:
- name: kete.routes.protected-api.destination.oauth.client-secret
  valueFrom:
    secretKeyRef:
      name: keycloak-events-secrets
      key: oauth-client-secret
envFrom:
- configMapRef:
    name: keycloak-events-config
```



## Setting Up an OAuth Client Manually

For external mode against a Keycloak realm (or to pre-create the internal-mode client):

1. Log into the Keycloak Admin Console and open the realm
2. **Clients → Create client**: Client ID `keycloak-events-publisher`, protocol `openid-connect`
3. Enable **Client authentication** (confidential client) and **Service accounts roles**; leave Standard flow and Direct access grants off
4. Copy the secret from the **Credentials** tab
5. Assign roles on the **Service account roles** tab if your API requires them

Keycloak token URLs have the form `https://{keycloak-host}/realms/{realm}/protocol/openid-connect/token`.



## Troubleshooting

| Symptom | Meaning / Fix |
|---------|---------------|
| `OAuth token request failed: <error object>` | The token endpoint rejected the request. The error object carries the OAuth error code (`invalid_client`, `invalid_scope`, …). Check client ID/secret, that the client is confidential with service accounts enabled, and that the requested scopes are allowed. |
| `oauth configuration is required when authentication-type is 'oauth'` (Pulsar) | `oauth.enabled` must be `true` when `authentication-type=oauth`. |
| `realm is required for INTERNAL mode` | Set `oauth.realm` in internal mode. |
| Requests fail once the first token expires | Check the authorization server's `expires_in`; tokens are refreshed 30 seconds before expiry, so synchronise clocks (NTP). |
| Token request hangs | The token request uses the HTTP client's default timeout (it is not governed by `destination.timeout-seconds`). Check network connectivity and firewall rules to `oauth.token-url`. |

Internal-mode problems are logged at `WARN` by `io.github.fortunen.kete.OAuthMaterial` (realm not found, client registration failed). Raise that category's log level to `DEBUG` in Keycloak (`--log-level=INFO,io.github.fortunen.kete:DEBUG`) for more detail.



## Related Documentation

- [HTTP Destination → OAuth 2.0 Properties](../user-guide/destinations/http.md#oauth-20-properties)
- [Configuration Reference](configuration.md)
