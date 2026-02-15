# SOAP Destination

Stream Keycloak events to SOAP/XML web services.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `soap` |
| **Protocol** | HTTP/HTTPS (SOAP 1.1 / 1.2) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Any SOAP Web Service** | SOAP 1.1 and 1.2 endpoints |
| **Enterprise SOA** | Legacy SOAP services, ESBs |
| **WCF Services** | .NET Windows Communication Foundation |
| **Apache CXF** | Java SOAP framework |
| **Spring WS** | Spring Web Services |



## Example Configurations

=== "Basic SOAP 1.1"

    ```bash
    kete.routes.soap.destination.kind=soap
    kete.routes.soap.destination.url=https://soa.example.com/events
    ```

=== "SOAP 1.2 with SOAPAction"

    ```bash
    kete.routes.soap.destination.kind=soap
    kete.routes.soap.destination.url=https://soa.example.com/events
    kete.routes.soap.destination.soap-version=1.2
    kete.routes.soap.destination.soap-action=urn:SendEvent
    ```

=== "With Basic Auth"

    ```bash
    kete.routes.soap.destination.kind=soap
    kete.routes.soap.destination.url=https://soa.example.com/events
    kete.routes.soap.destination.authentication-type=basic
    kete.routes.soap.destination.basic-username=keycloak
    kete.routes.soap.destination.basic-password=secret
    ```



## Features

- SOAP 1.1 and SOAP 1.2 envelope wrapping
- Serialized event body placed inside `<soap:Body>` as raw content
- Optional SOAPAction header (SOAP 1.1) or action parameter (SOAP 1.2)
- URL templating with event variables
- OAuth 2.0, Basic, API Key, and X-API-Key authentication
- Custom headers support
- Content encoding (gzip) and content transfer encoding (base64)
- TLS/mTLS support
- Configurable timeouts



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `soap` | `soap` |
| `destination.url` | Full URL (supports templating) | `https://soa.example.com/events` |

### Dynamic URLs (Templating)

The `url` property supports template variables:

```bash
kete.routes.soap.destination.url=https://soa.example.com/events/${realmLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

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
| `destination.host` | Target host (required if no `url`) | `soa.example.com` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.port` | `80` (HTTP) / `443` (HTTPS) | HTTP port | `8080` |
| `destination.path-and-query` | `/` | URL path and query string | `/ws/events` |
| `destination.timeout-seconds` | `10` | Request timeout in seconds | `30` |
| `destination.soap-version` | `1.1` | SOAP version (`1.1` or `1.2`) | `1.2` |
| `destination.soap-action` | _(empty)_ | SOAPAction header value | `urn:SendEvent` |
| `destination.content-encoding` | _(empty)_ | Compress body before wrapping (e.g., `gzip`) | `gzip` |
| `destination.content-transfer-encoding` | _(empty)_ | Encode body after compression (e.g., `base64`) | `base64` |

### SOAP Versions

| Version | Content-Type | SOAPAction | Namespace |
|---------|-------------|------------|-----------|
| **1.1** | `text/xml` | Separate HTTP header | `http://schemas.xmlsoap.org/soap/envelope/` |
| **1.2** | `application/soap+xml` | `action` parameter in Content-Type | `http://www.w3.org/2003/05/soap-envelope` |

### Custom Headers

Headers are configured under `destination.headers.<NAME>`:

```bash
kete.routes.soap.destination.headers.X-Source=keycloak
kete.routes.soap.destination.headers.X-Environment=production
```

!!! note "Reserved Headers"
    The `Content-Type`, `x-eventkind`, and `x-eventtype` headers are managed automatically and cannot be overridden via custom headers.

### Authentication

The SOAP destination supports multiple authentication methods via the `authentication-type` property:

| `authentication-type` | Description | Required Properties |
|-----------------------|-------------|---------------------|
| `oauth` | OAuth 2.0 Client Credentials flow | See [HTTP Destination — OAuth](http.md#oauth-20-properties) |
| `basic` | HTTP Basic Authentication | `basic-username`, `basic-password` |
| `api-key` | API key sent in `Api-Key` header | `api-key-value` |
| `x-api-key` | API key sent in `X-API-Key` header | `x-api-key-value` |

#### Basic Authentication

```bash
kete.routes.soap.destination.authentication-type=basic
kete.routes.soap.destination.basic-username=keycloak
kete.routes.soap.destination.basic-password=secret123
```

#### API Key Authentication

```bash
# Sends header: Api-Key: <value>
kete.routes.soap.destination.authentication-type=api-key
kete.routes.soap.destination.api-key-value=sk-1234567890
```

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable HTTPS (auto-enabled when using `url` with `https://` scheme) |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |



## Configuration Examples

### Example 1: Simple SOAP 1.1 Endpoint

```bash
kete.routes.events.realm-matchers.realm=list:master
kete.routes.events.destination.kind=soap
kete.routes.events.destination.url=https://soa.example.com/EventService
kete.routes.events.event-matchers.filter=glob:*
```

### Example 2: SOAP 1.2 with Authentication

```bash
kete.routes.secure.destination.kind=soap
kete.routes.secure.destination.url=https://soa.example.com/SecureEventService
kete.routes.secure.destination.soap-version=1.2
kete.routes.secure.destination.soap-action=urn:ProcessEvent
kete.routes.secure.destination.authentication-type=basic
kete.routes.secure.destination.basic-username=keycloak
kete.routes.secure.destination.basic-password=secret
```

### Example 3: With Content Encoding

```bash
kete.routes.compressed.destination.kind=soap
kete.routes.compressed.destination.url=https://soa.example.com/events
kete.routes.compressed.destination.content-encoding=gzip
kete.routes.compressed.destination.content-transfer-encoding=base64
```



## Quick Starts

| Target | Quickstart Folder |
|--------|-------------------|
| HTTP Echo (Webhook) | `soap-webhook/` |



## See Also

- [Serializers](../serializers/overview.md) — Format events before sending
- [Matchers](../matchers/overview.md) — Filter which events are forwarded
- [Event Types](../event-types.md) — Available event types
- [Certificate Loaders](../certificate-loaders/overview.md) — TLS certificate formats
- [HTTP Destination](http.md) — Similar HTTP-based destination with REST focus
