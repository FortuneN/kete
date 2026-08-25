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
    kete.routes.oauth-api.destination.authentication-type=oauth
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
    kete.routes.internal-oauth.destination.authentication-type=oauth
    kete.routes.internal-oauth.destination.oauth.enabled=true
    kete.routes.internal-oauth.destination.oauth.mode=internal
    kete.routes.internal-oauth.destination.oauth.realm=master
    ```

=== "With Custom Retry"

    ```bash
    kete.routes.reliable.realm-matchers.realm=list:master
    kete.routes.reliable.destination.kind=http
    kete.routes.reliable.destination.url=https://api.example.com/events
    kete.routes.reliable.retry.max-attempts=5
    kete.routes.reliable.retry.wait-duration=PT2S
    ```



## Features

- Automatic retry with configurable attempts and wait duration
- OAuth 2.0 Client Credentials with token caching
- Custom headers and configurable timeouts
- TLS/SSL with mTLS support
- Start-up connectivity check: a `GET` to the URL's scheme, host and port root; an unreachable host fails route initialization (skipped when `url` is templated, because the real URL is only known per event)



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

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

When using `destination.url`:

- The **host** is extracted from the URL
- The **port** defaults to 80 for http, 443 for https
- The **path and query string** are extracted
- **TLS follows the scheme**: `https` enables TLS and `http` disables it, overriding `tls.enabled` in both directions

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
| `destination.method` | `POST` | HTTP method, upper-cased and passed through unvalidated (e.g. `POST`, `PUT`, `PATCH`) | `PUT` |
| `destination.timeout-seconds` | `10` | Request timeout in seconds | `60` |
| `destination.content-encoding` | _(empty)_ | Compress body (e.g., `gzip`, `deflate`). Sets `Content-Encoding` header. | `gzip` |
| `destination.content-transfer-encoding` | _(empty)_ | Encode body (e.g., `base64`). Sets `Content-Transfer-Encoding` header. | `base64` |
| `destination.pool.min-idle` | `1` | Minimum idle connections in pool | `5` |
| `destination.pool.max-idle` | `10` | Maximum idle connections in pool | `20` |
| `destination.pool.max-total` | `20` | Maximum total connections in pool | `50` |

### Custom Headers

Headers are configured under `destination.headers.<NAME>`:

```bash
kete.routes.my-api.destination.headers.X-API-Key=my-secret-key
kete.routes.my-api.destination.headers.X-Source=keycloak
```

Every request also carries the standard `x-eventkind`, `x-eventtype` and `Content-Type` headers. Custom headers with those names (case-insensitive), or named `eventkind`/`eventtype`/`contenttype`, are ignored.

### Authentication

The HTTP destination supports multiple authentication methods via the `authentication-type` property:

| `authentication-type` | Description | Required Properties |
|-----------------------|-------------|---------------------|
| `oauth` | OAuth 2.0 Client Credentials flow | See [OAuth 2.0 Properties](#oauth-20-properties) below |
| `basic` | HTTP Basic Authentication | `basic-username`, `basic-password` |
| `api-key` | API key sent in `Api-Key` header | `api-key-value` |
| `x-api-key` | API key sent in `X-API-Key` header | `x-api-key-value` |

#### Basic Authentication

```bash
kete.routes.my-api.destination.authentication-type=basic
kete.routes.my-api.destination.basic-username=keycloak
kete.routes.my-api.destination.basic-password=secret123
```

#### API Key Authentication

```bash
# Sends header: Api-Key: <value>
kete.routes.my-api.destination.authentication-type=api-key
kete.routes.my-api.destination.api-key-value=sk-1234567890

# Sends header: X-API-Key: <value>
kete.routes.my-api.destination.authentication-type=x-api-key
kete.routes.my-api.destination.x-api-key-value=sk-1234567890
```

### OAuth 2.0 Properties

OAuth is activated by `destination.authentication-type=oauth`; the `oauth.*` properties below are only read when that is set. The HTTP destination supports two OAuth modes:

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

Use the current Keycloak instance as the OAuth server. This mode **automatically registers a service account client** in Keycloak during initialization (when no `oauth.client-secret` is configured) - the simplest setup possible:

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `destination.oauth.enabled` | Yes | `false` | Enable OAuth 2.0 |
| `destination.oauth.mode` | Yes | - | Must be `internal` |
| `destination.oauth.realm` | Yes | - | Realm that hosts the auto-registered client and issues the tokens |
| `destination.oauth.token-url` | No | `http://localhost:8080/realms/<realm>/protocol/openid-connect/token` | Token endpoint (override when Keycloak is not reachable on `localhost:8080`) |
| `destination.oauth.client-id` | No | `kete-oauth-client` | Override auto-generated client ID |
| `destination.oauth.client-secret` | No | Auto-generated | Provide the secret of an existing client; when set, KETE does not register a client and the client must already exist |
| `destination.oauth.scope` | No | `""` | Requested OAuth scopes |

**Internal Mode Example:**

```bash
# Simplest OAuth setup - just 4 properties!
kete.routes.api.destination.authentication-type=oauth
kete.routes.api.destination.oauth.enabled=true
kete.routes.api.destination.oauth.mode=internal
kete.routes.api.destination.oauth.realm=master
```

This automatically:

1. Creates (or reuses) a confidential client `kete-oauth-client` in the `oauth.realm` realm
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
kete.routes.internal-oauth.destination.authentication-type=oauth
kete.routes.internal-oauth.destination.oauth.enabled=true
kete.routes.internal-oauth.destination.oauth.mode=internal
kete.routes.internal-oauth.destination.oauth.realm=master
kete.routes.internal-oauth.event-matchers.filter=glob:*
```

### Example 3: External OAuth with Retry

```bash
kete.routes.api.realm-matchers.realm=list:master
kete.routes.api.destination.kind=http
kete.routes.api.destination.url=https://api.example.com/v1/events
kete.routes.api.destination.method=PUT
kete.routes.api.destination.timeout-seconds=60
kete.routes.api.destination.authentication-type=oauth
kete.routes.api.destination.oauth.enabled=true
kete.routes.api.destination.oauth.mode=external
kete.routes.api.destination.oauth.token-url=https://auth.example.com/token
kete.routes.api.destination.oauth.client-id=events-publisher
kete.routes.api.destination.oauth.client-secret=your-secret-here
kete.routes.api.destination.oauth.scope=api:write
kete.routes.api.event-matchers.filter=glob:*
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



## Quick Starts

| Quick Start | Description |
|-------------|-------------|
| [http-webhook](https://github.com/FortuneN/kete/tree/release/quick-starts/http-webhook/) | HTTP webhook endpoint |
| [http-azure-event-grid](https://github.com/FortuneN/kete/tree/release/quick-starts/http-azure-event-grid/) | Azure Event Grid |



## See Also

- [Serializers](../serializers/overview.md)
- [Matchers](../matchers/overview.md)
- [Event Types](../event-types.md)
- [Certificate Loaders](../certificate-loaders/overview.md)
