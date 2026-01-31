# HTTP Destination

Stream Keycloak events to HTTP/REST endpoints.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `http` |
| **Protocol** | HTTP/HTTPS |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Any REST API** | POST/PUT to any HTTP endpoint |
| **Webhooks** | Slack, Discord, Teams, PagerDuty, etc. |
| **API Gateways** | Kong, AWS API Gateway, Azure APIM |
| **Cloud Functions** | AWS Lambda URLs, Azure Functions, Cloud Run |
| **Custom backends** | Any HTTP-accessible service |



## Example Configurations

=== "Basic Webhook"

    ```bash
    kete.routes.webhook.realm-matchers.realm=list:master
    kete.routes.webhook.destination.kind=http
    kete.routes.webhook.destination.url=https://api.example.com/webhook/keycloak
    kete.routes.webhook.destination.headers.X-API-Key=your-api-key
    ```

=== "With OAuth 2.0 (External)"

    ```bash
    kete.routes.oauth-api.realm-matchers.realm=list:master
    kete.routes.oauth-api.destination.kind=http
    kete.routes.oauth-api.destination.url=https://api.example.com/api/v1/events
    kete.routes.oauth-api.destination.oauth.enabled=true
    kete.routes.oauth-api.destination.oauth.token-url=https://auth.example.com/oauth/token
    kete.routes.oauth-api.destination.oauth.client-id=keycloak-client
    kete.routes.oauth-api.destination.oauth.client-secret=secret
    kete.routes.oauth-api.destination.oauth.scope=events:write
    ```

=== "With OAuth 2.0 (Internal)"

    ```bash
    # Uses Keycloak itself as OAuth server - auto-creates client!
    kete.routes.internal-oauth.realm-matchers.realm=list:master
    kete.routes.internal-oauth.destination.kind=http
    kete.routes.internal-oauth.destination.url=https://api.example.com/api/events
    kete.routes.internal-oauth.destination.oauth.enabled=true
    kete.routes.internal-oauth.destination.oauth.mode=internal
    ```

=== "With Retry"

    ```bash
    kete.routes.reliable.realm-matchers.realm=list:master
    kete.routes.reliable.destination.kind=http
    kete.routes.reliable.destination.url=https://api.example.com/events
    kete.routes.reliable.retry.enabled=true
    kete.routes.reliable.retry.max-attempts=5
    kete.routes.reliable.retry.wait-duration=PT2S
    ```



## Features

- Automatic retry with exponential backoff
- OAuth 2.0 Client Credentials with token caching
- Custom headers and configurable timeouts
- TLS/SSL with mTLS support



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `http` | `http` |
| `destination.url` | Full URL (supports templating) | `https://api.example.com:8443/events/${realmLowerCase}` |

### Dynamic URLs (Templating)

The `url` property supports template variables:

```bash
# Dynamic URL per realm
kete.routes.webhook.destination.url=https://api.example.com/events/${realmLowerCase}

# Dynamic URL with event type
kete.routes.webhook.destination.url=https://api.example.com/${kindLowerCase}/${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${resultLowerCase}`, `${resultUpperCase}`

When using `destination.url`:

- The **host** is extracted from the URL
- The **port** defaults to 80 for http, 443 for https
- The **path and query string** are extracted
- **TLS is auto-enabled** when the scheme is `https`

If both `url` and individual properties are specified, `url` takes precedence.

### Alternative: Individual Properties

Instead of `url`, you can configure each component separately:

| Property | Description | Example |
|----------|-------------|---------|
| `destination.host` | Target HTTP host (required if no `url`) | `api.example.com` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.port` | `80` (HTTP) / `443` (HTTPS) | HTTP port | `8080` |
| `destination.path-and-query` | `/` | URL path and query string | `/api/v1/events?source=keycloak` |
| `destination.method` | `POST` | HTTP method (POST or PUT) | `PUT` |
| `destination.timeout-seconds` | `10` | Request timeout in seconds | `60` |
| `destination.message-headers-enabled` | `true` | Include event metadata as HTTP headers | `false` |
| `destination.min-pool-size` | `5` | Minimum connections in pool | `10` |
| `destination.max-pool-size` | `20` | Maximum connections in pool | `50` |

### Custom Headers

Headers are configured under `destination.headers.<NAME>`:

```bash
kete.routes.my-api.destination.headers.X-API-Key=my-secret-key
kete.routes.my-api.destination.headers.X-Source=keycloak
```

### OAuth 2.0 Properties

The HTTP destination supports two OAuth modes:

#### External Mode (Default)

Use an external OAuth 2.0 authorization server:

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `destination.oauth.enabled` | No | `false` | Enable OAuth 2.0 Client Credentials flow |
| `destination.oauth.mode` | No | `external` | OAuth mode: `external` or `internal` |
| `destination.oauth.token-url` | Yes* | - | OAuth token endpoint URL |
| `destination.oauth.client-id` | Yes* | - | OAuth client ID |
| `destination.oauth.client-secret` | Yes* | - | OAuth client secret |
| `destination.oauth.scope` | No | `""` | Requested OAuth scopes (space-separated) |

*Required when `oauth.enabled=true` and `oauth.mode=external`.

#### Internal Mode

Use the current Keycloak instance as the OAuth server. This mode **automatically registers a service account client** in Keycloak during initialization - the simplest setup possible:

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `destination.oauth.enabled` | Yes | `false` | Enable OAuth 2.0 |
| `destination.oauth.mode` | Yes | - | Must be `internal` |
| `destination.oauth.realm` | No | Route realm | Override realm for token URL |
| `destination.oauth.client-id` | No | `kete-oauth-client` | Override auto-generated client ID |
| `destination.oauth.client-secret` | No | Auto-generated | Override auto-generated secret |
| `destination.oauth.scope` | No | `""` | Requested OAuth scopes |

**Internal Mode Example:**

```bash
# Simplest OAuth setup - just 2 properties!
kete.routes.api.destination.oauth.enabled=true
kete.routes.api.destination.oauth.mode=internal
```

This automatically:

1. Creates a confidential client `kete-oauth-client` in the route's realm
2. Enables service account (client credentials grant)
3. Generates a secure client secret
4. Configures the token URL for the realm

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable HTTPS (auto-enabled when using `url` with `https://` scheme) |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |



## Configuration Examples

### Example 1: Simple POST Webhook

```bash
kete.routes.webhook.realm-matchers.realm=list:master
kete.routes.webhook.destination.kind=http
kete.routes.webhook.destination.url=https://hooks.example.com/keycloak/events
kete.routes.webhook.event-matchers.login=glob:LOGIN*
```

### Example 2: Internal OAuth (Simplest Setup)

Use the current Keycloak instance for OAuth - no external auth server needed:

```bash
kete.routes.internal-oauth.realm-matchers.realm=list:master
kete.routes.internal-oauth.destination.kind=http
kete.routes.internal-oauth.destination.url=https://api.example.com/v1/events
kete.routes.internal-oauth.destination.oauth.enabled=true
kete.routes.internal-oauth.destination.oauth.mode=internal
kete.routes.internal-oauth.event-matchers.filter=glob:*
```

### Example 3: External OAuth with Retry

```bash
kete.routes.api.realm-matchers.realm=list:master
kete.routes.api.destination.kind=http
kete.routes.api.destination.url=https://api.example.com/v1/events
kete.routes.api.destination.method=PUT
kete.routes.api.destination.timeout-seconds=60
kete.routes.api.destination.oauth.enabled=true
kete.routes.api.destination.oauth.mode=external
kete.routes.api.destination.oauth.token-url=https://auth.example.com/token
kete.routes.api.destination.oauth.client-id=events-publisher
kete.routes.api.destination.oauth.client-secret=your-secret-here
kete.routes.api.destination.oauth.scope=api:write
kete.routes.api.event-matchers.filter=glob:*
kete.routes.api.retry.enabled=true
kete.routes.api.retry.max-attempts=3
kete.routes.api.retry.wait-duration=PT1S
```

### Example 4: Custom Headers with API Key

```bash
kete.routes.custom.realm-matchers.realm=list:master
kete.routes.custom.destination.kind=http
kete.routes.custom.destination.url=https://api.example.com/events
kete.routes.custom.destination.headers.X-API-Key=sk-1234567890
kete.routes.custom.destination.headers.X-Source=keycloak
kete.routes.custom.event-matchers.filter=glob:*
```

### Example 5: mTLS Configuration

```bash
kete.routes.secure.realm-matchers.realm=list:master
kete.routes.secure.destination.kind=http
kete.routes.secure.destination.url=https://secure-api.example.com/events
kete.routes.secure.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.secure.destination.tls.key-store.loader.path=/certs/client.p12
kete.routes.secure.destination.tls.key-store.password=keystorepass
kete.routes.secure.destination.tls.trust-store.loader.kind=pem-file-path
kete.routes.secure.destination.tls.trust-store.loader.path=/certs/ca.pem
kete.routes.secure.event-matchers.filter=glob:*
```



## Retry Configuration

Retry is configured at the **route level**, not within the destination. See [Retry](../retry.md) for full configuration options.



## Common Integration Patterns

### Webhook Services

**Zapier:**
```bash
kete.routes.zapier.realm-matchers.realm=list:master
kete.routes.zapier.destination.kind=http
kete.routes.zapier.destination.url=https://hooks.zapier.com/hooks/catch/123456/abcdef/
```

**Make (Integromat):**
```bash
kete.routes.make.realm-matchers.realm=list:master
kete.routes.make.destination.kind=http
kete.routes.make.destination.url=https://hook.eu1.make.com/abc123def456
```

### Serverless Functions

**AWS Lambda (via API Gateway):**
```bash
kete.routes.lambda.realm-matchers.realm=list:master
kete.routes.lambda.destination.kind=http
kete.routes.lambda.destination.url=https://abc123.execute-api.us-east-1.amazonaws.com/prod/events
kete.routes.lambda.destination.headers.x-api-key=your-api-key
```

**Azure Functions:**
```bash
kete.routes.azure.realm-matchers.realm=list:master
kete.routes.azure.destination.kind=http
kete.routes.azure.destination.url=https://myfunction.azurewebsites.net/api/events
kete.routes.azure.destination.headers.x-functions-key=your-function-key
```
