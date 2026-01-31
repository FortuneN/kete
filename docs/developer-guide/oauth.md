# OAuth Authentication for HTTP Destinations

## Overview

The HTTP destination supports **OAuth 2.0 Client Credentials** flow for authenticating with protected APIs with two modes:

- **Internal Mode**: Uses the current Keycloak instance as the OAuth server (simplest setup)
- **External Mode**: Uses an external OAuth 2.0 authorization server

Features:

- Automatic token management with built-in expiry handling
- Thread-safe token caching
- Fully RFC 6749 compliant
- Tokens refreshed 30 seconds before expiry



## OAuth Modes

### Internal Mode (Recommended for Same-Keycloak APIs)

Use the current Keycloak instance as the OAuth server. This mode **automatically registers a service account client** during initialization:

```bash
# Simplest OAuth setup - just 2 properties!
kete.routes.api.destination.oauth.enabled=true
kete.routes.api.destination.oauth.mode=internal
```

This automatically:

1. Creates a confidential client `kete-oauth-client` in the route's realm
2. Enables service account (client credentials grant)
3. Generates a secure client secret (UUID)
4. Configures the token URL for the realm

**Internal Mode Properties:**

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `oauth.enabled` | Yes | `false` | Enable OAuth |
| `oauth.mode` | Yes | - | Must be `internal` |
| `oauth.realm` | No | Route realm | Override realm for token URL |
| `oauth.client-id` | No | `kete-oauth-client` | Override client ID |
| `oauth.client-secret` | No | Auto-generated | Override client secret |
| `oauth.scope` | No | `""` | Requested OAuth scopes |

### External Mode

Use an external OAuth 2.0 authorization server:

```bash
kete.routes.api.destination.oauth.enabled=true
kete.routes.api.destination.oauth.mode=external
kete.routes.api.destination.oauth.token-url=https://auth.example.com/token
kete.routes.api.destination.oauth.client-id=my-client
kete.routes.api.destination.oauth.client-secret=my-secret
```

**External Mode Properties:**

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `oauth.enabled` | Yes | `false` | Enable OAuth |
| `oauth.mode` | Yes | `internal` | OAuth mode (`internal` or `external`) |
| `oauth.token-url` | Yes | - | OAuth token endpoint URL |
| `oauth.client-id` | Yes | - | OAuth client ID |
| `oauth.client-secret` | Yes | - | OAuth client secret |
| `oauth.scope` | No | `""` | Requested OAuth scopes |



## Token Management

Token management features:

- **Automatic Token Retrieval**: Tokens fetched using Client Credentials flow
- **Token Caching**: Access tokens cached with expiry tracking
- **Automatic Refresh**: Tokens refreshed 30 seconds before expiry
- **Thread-Safe**: Safe for concurrent event publishing
- **Default Lifetime**: Assumes 3600 seconds if not in response



## Usage Examples

### Example 1: Internal Mode (Same Keycloak)

Send events to an API protected by the same Keycloak instance:

```bash
kete.routes.internal-api.realm-matchers.realm=list:master
kete.routes.internal-api.destination.kind=http
kete.routes.internal-api.destination.host=api.internal.com
kete.routes.internal-api.destination.port=443
kete.routes.internal-api.destination.tls.enabled=true
kete.routes.internal-api.destination.oauth.enabled=true
kete.routes.internal-api.destination.oauth.mode=internal
kete.routes.internal-api.event-matchers.filter=glob:*
```

### Example 2: External Mode (External OAuth Provider)

Send events to an API protected by an external OAuth provider:

```bash
kete.routes.external-api.realm-matchers.realm=list:master
kete.routes.external-api.destination.kind=http
kete.routes.external-api.destination.host=api.external.com
kete.routes.external-api.destination.port=443
kete.routes.external-api.destination.tls.enabled=true
kete.routes.external-api.destination.oauth.enabled=true
kete.routes.external-api.destination.oauth.mode=external
kete.routes.external-api.destination.oauth.token-url=https://auth.example.com/oauth/token
kete.routes.external-api.destination.oauth.client-id=keycloak-integration
kete.routes.external-api.destination.oauth.client-secret=super-secret
kete.routes.external-api.destination.oauth.scope=events:write
kete.routes.external-api.event-matchers.filter=glob:*
```

### Example 3: Kubernetes with Internal OAuth

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: keycloak-events-config
data:
  kete.routes.api.realm-matchers.realm: "list:production"
  kete.routes.api.destination.kind: "http"
  kete.routes.api.destination.host: "api.company.com"
  kete.routes.api.destination.port: "443"
  kete.routes.api.destination.tls.enabled: "true"
  kete.routes.api.destination.oauth.enabled: "true"
  kete.routes.api.destination.oauth.mode: "internal"
  kete.routes.api.event-matchers.all: "glob:*"
```



## Setting Up OAuth Client Manually

If using External mode or overriding Internal mode settings:

### Create OAuth Client in Keycloak

1. **Log into Keycloak Admin Console**

2. **Navigate to Clients**: Go to your realm → Clients → Create

3. **Configure Client**:
   - **Client ID**: `keycloak-events-publisher`
   - **Client Protocol**: `openid-connect`
   - **Access Type**: `confidential`
   - **Service Accounts Enabled**: `ON`
   - **Standard Flow Enabled**: `OFF`
   - **Direct Access Grants Enabled**: `OFF`

4. **Get Client Secret**: Go to Credentials tab → Copy secret

5. **Assign Roles** (if needed): Go to Service Account Roles tab

### Token URL Format

For Keycloak, the token URL format is:
```
https://{keycloak-host}/realms/{realm-name}/protocol/openid-connect/token

export kete.routes.my-api.destination.oauth.client-id=events-client
export kete.routes.my-api.destination.oauth.client-secret=your-client-secret
export kete.routes.my-api.destination.oauth.scope="email profile"
```

### Example 2: Authenticate with External OAuth Provider

Send events to an API protected by an external OAuth 2.0 provider (e.g., Auth0, Okta):

```bash
export kete.routes.external-api.destination.kind=http
export kete.routes.external-api.destination.url=https://api.external.com/webhooks/keycloak
export kete.routes.external-api.destination.oauth.enabled=true
export kete.routes.external-api.destination.oauth.token-url=https://oauth-provider.com/oauth/token
export kete.routes.external-api.destination.oauth.client-id=my-app-client
export kete.routes.external-api.destination.oauth.client-secret=super-secret-key
```

### Example 3: Kubernetes ConfigMap with OAuth

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: keycloak-events-config
data:
  kete.routes.protected-api.destination.kind: "http"
  kete.routes.protected-api.destination.url: "https://api.company.com/events"
  kete.routes.protected-api.destination.method: "POST"
  kete.routes.protected-api.destination.timeout-seconds: "10"
  kete.routes.protected-api.retry.enabled: "true"
  kete.routes.protected-api.retry.max-attempts: "3"
  kete.routes.protected-api.retry.wait-duration: "PT2S"
  kete.routes.protected-api.destination.oauth.enabled: "true"
  kete.routes.protected-api.destination.oauth.token-url: "https://keycloak.company.com/realms/production/protocol/openid-connect/token"
  kete.routes.protected-api.destination.oauth.client-id: "keycloak-events-publisher"
  kete.routes.protected-api.destination.oauth.scope: "api:write events:publish"

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

## Setting Up OAuth in Keycloak

### Create OAuth Client for Event Publishing

1. **Log into Keycloak Admin Console**

2. **Navigate to Clients**:
   - Go to your realm → Clients → Create

3. **Configure Client**:
   - **Client ID**: `keycloak-events-publisher`
   - **Client Protocol**: `openid-connect`
   - **Access Type**: `confidential`
   - **Service Accounts Enabled**: `ON`
   - **Authorization Enabled**: `OFF`
   - **Standard Flow Enabled**: `OFF`
   - **Direct Access Grants Enabled**: `OFF`

4. **Set Valid Redirect URIs**:
   - Not needed for client credentials flow

5. **Get Client Secret**:
   - Go to Credentials tab
   - Copy the secret value

6. **Assign Roles** (if your API requires specific roles):
   - Go to Service Account Roles tab
   - Assign necessary roles

### Token URL Format

For Keycloak, the token URL format is:
```
https://{keycloak-host}/realms/{realm-name}/protocol/openid-connect/token
```

Examples:
- `https://keycloak.example.com/realms/master/protocol/openid-connect/token`
- `https://auth.company.com/realms/production/protocol/openid-connect/token`

## Troubleshooting

### Token Request Failures

**Symptom**: `OAuth token request failed with status 401`

**Solutions**:
- Verify `destination.oauth.client-id` and `destination.oauth.client-secret` are correct
- Check client configuration in Keycloak (Access Type: confidential)
- Ensure Service Accounts are enabled

### Invalid Scope

**Symptom**: `OAuth token request failed with status 400: invalid_scope`

**Solutions**:
- Verify the scopes are valid for your OAuth provider
- Check client has permission to request those scopes
- Try without `destination.oauth.scope` (will use default scopes)

### Token Expiry Issues

**Symptom**: Events fail after token expires

**Solutions**:
- Check OAuth server token expiry time (`expires_in` in response)
- The destination refreshes tokens 30 seconds before expiry automatically
- Verify system clocks are synchronized (NTP)

### Connection Timeouts

**Symptom**: `OAuth token request timed out`

**Solutions**:
- Increase `destination.timeout-seconds` configuration
- Check network connectivity to `destination.oauth.token-url`
- Verify firewall rules allow HTTPS outbound

## Logging

Enable detailed OAuth logging:

```bash
# Set log level to FINE for HTTP destination
-Djava.util.logging.config.file=/path/to/logging.properties
```

logging.properties:
```properties
io.github.fortunen.kete.destinations.impl.HttpDestination.level=FINE
```

OAuth log messages:
- `OAuth enabled for HTTP destination: token-url=...`
- `Fetching new OAuth access token from {url}`
- `OAuth access token obtained, expires in {seconds} seconds`

## Related Documentation

- [Configuration Reference](configuration.md) - Environment variable patterns
