# Azure Storage Queue Destination

Stream Keycloak events to Azure Storage Queue.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `azure-storage-queue` |
| **Protocol** | Azure Storage Queue REST API |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Azure Storage Queue** | Fully managed cloud queue service |
| **Azurite Emulator** | Local development and testing |



## Example Configurations

=== "Azure Cloud"

    ```bash
    kete.routes.asq.destination.kind=azure-storage-queue
    kete.routes.asq.destination.connection-string=DefaultEndpointsProtocol=https;AccountName=mystorageaccount;AccountKey=your-account-key;EndpointSuffix=core.windows.net
    kete.routes.asq.destination.queue=keycloak-events
    ```

=== "SAS Token"

    ```bash
    kete.routes.asq.destination.kind=azure-storage-queue
    kete.routes.asq.destination.connection-string=QueueEndpoint=https://mystorageaccount.queue.core.windows.net;SharedAccessSignature=sv=2024-08-04&ss=q&srt=sco&sp=wau&se=...&sig=...
    kete.routes.asq.destination.queue=keycloak-events
    ```

=== "Azurite Emulator"

    ```bash
    kete.routes.asq.destination.kind=azure-storage-queue
    kete.routes.asq.destination.connection-string=DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;QueueEndpoint=http://azurite:10001/devstoreaccount1
    kete.routes.asq.destination.queue=keycloak-events
    ```



## Features

- Azure Storage Queue REST API integration (no Azure SDK dependency)
- Authentication via connection string (Shared Key or SAS Token)
- Emulator support via Azurite for local development and testing
- Queue name templating with variables
- Configurable message TTL
- Messages encoded as Base64
- TLS/mTLS support



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `azure-storage-queue` | `azure-storage-queue` |
| `destination.connection-string` | Azure Storage connection string | `DefaultEndpointsProtocol=https;AccountName=...;AccountKey=...;EndpointSuffix=core.windows.net` |
| `destination.queue` | Queue name (supports templating) | `keycloak-events` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.message-ttl` | `0` | Message TTL in seconds (`0` = Azure default 7 days, `-1` = no expiry) | `3600` |
| `destination.timeout-seconds` | `10` | HTTP connect and request timeout in seconds | `30` |

### Dynamic Queue Name (Templating)

The `queue` property supports template variables:

```bash
# Dynamic queue per realm
kete.routes.asq.destination.queue=keycloak-events-${realmLowerCase}

# Dynamic queue per event type
kete.routes.asq.destination.queue=keycloak-${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${resultLowerCase}`, `${resultUpperCase}`

### Authentication

Authentication is configured entirely through the `connection-string` property. The connection string is parsed to extract `AccountName`, `AccountKey` or `SharedAccessSignature`, `QueueEndpoint`, `DefaultEndpointsProtocol`, and `EndpointSuffix`.

#### Shared Key Connection String

```bash
# Standard Azure connection string
DefaultEndpointsProtocol=https;AccountName=myaccount;AccountKey=your-key;EndpointSuffix=core.windows.net

# With explicit QueueEndpoint
AccountName=myaccount;AccountKey=your-key;QueueEndpoint=https://myaccount.queue.core.windows.net
```

Uses HMAC-SHA256 signing per the Azure Storage REST API specification.

#### SAS Token Connection String

```bash
# SAS with explicit endpoint
QueueEndpoint=https://myaccount.queue.core.windows.net;SharedAccessSignature=sv=2024-08-04&ss=q&srt=sco&sp=wau&se=...&sig=...

# SAS with account name (endpoint derived)
AccountName=myaccount;SharedAccessSignature=sv=2024-08-04&ss=q&sig=...
```

The SAS token is appended as query parameters to each request. No signing is needed.

!!! note "Emulator Mode"
    When using Azurite, use the well-known Azurite development connection string with an explicit `QueueEndpoint` pointing to the emulator:
    ```
    DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;QueueEndpoint=http://azurite:10001/devstoreaccount1
    ```

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable TLS |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |

!!! note "Azure Storage Queue TLS"
    When connecting to the real Azure Storage Queue service, TLS is handled via HTTPS by default — no explicit TLS configuration needed. TLS properties are useful when connecting through a proxy, private endpoint, or custom emulator configuration.



## Configuration Examples

### Example 1: Production Setup

```bash
kete.routes.prod.destination.kind=azure-storage-queue
kete.routes.prod.realm-matchers.realm=list:master
kete.routes.prod.event-matchers.filter=glob:*
kete.routes.prod.destination.connection-string=DefaultEndpointsProtocol=https;AccountName=prodstorageaccount;AccountKey=your-production-account-key;EndpointSuffix=core.windows.net
kete.routes.prod.destination.queue=keycloak-events
kete.routes.prod.destination.message-ttl=86400
kete.routes.prod.destination.timeout-seconds=30
```

### Example 2: Per-Realm Queues

```bash
# Route events to different queues per realm
kete.routes.events.destination.kind=azure-storage-queue
kete.routes.events.destination.connection-string=DefaultEndpointsProtocol=https;AccountName=mystorageaccount;AccountKey=your-key;EndpointSuffix=core.windows.net
kete.routes.events.destination.queue=keycloak-${realmLowerCase}-events
```

### Example 3: Local Development with Azurite

```bash
kete.routes.local.destination.kind=azure-storage-queue
kete.routes.local.realm-matchers.realm=list:master
kete.routes.local.event-matchers.filter=glob:*
kete.routes.local.destination.connection-string=DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;QueueEndpoint=http://localhost:10001/devstoreaccount1
kete.routes.local.destination.queue=keycloak-events
```

### Example 4: Connection String from Environment Variable

```bash
kete.routes.env.destination.kind=azure-storage-queue
kete.routes.env.realm-matchers.realm=list:master
kete.routes.env.event-matchers.filter=glob:*
kete.routes.env.destination.connection-string=${AZURE_STORAGE_CONNECTION_STRING}
kete.routes.env.destination.queue=keycloak-events
```



## Quick Starts

| Quick Start | Description |
|-------------|-------------|
| [azure-storage-queue](https://github.com/FortuneN/kete/tree/develop/quick-starts/azure-storage-queue) | Azure Storage Queue (real cloud) |
| [azure-storage-queue-emulator](https://github.com/FortuneN/kete/tree/develop/quick-starts/azure-storage-queue-emulator) | Azurite Emulator (local) |



## See Also

- [Serializers](../serializers/overview.md) - Choose JSON, YAML, CBOR, Properties, etc.
- [Matchers](../matchers/overview.md) - Filter events by realm, type, resource, operation
- [Event Types](../event-types.md) - List of all event types
- [Certificate Loaders](../certificate-loaders/overview.md) - For TLS configuration
