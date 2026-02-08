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

=== "Shared Key (Account Name + Key)"

    ```bash
    kete.routes.asq.destination.kind=azure-storage-queue
    kete.routes.asq.destination.account-name=mystorageaccount
    kete.routes.asq.destination.account-key=your-account-key
    kete.routes.asq.destination.queue=keycloak-events
    ```

=== "Connection String"

    ```bash
    kete.routes.asq.destination.kind=azure-storage-queue
    kete.routes.asq.destination.connection-string=DefaultEndpointsProtocol=https;AccountName=mystorageaccount;AccountKey=your-account-key;EndpointSuffix=core.windows.net
    kete.routes.asq.destination.queue=keycloak-events
    ```

=== "SAS Token"

    ```bash
    kete.routes.asq.destination.kind=azure-storage-queue
    kete.routes.asq.destination.account-name=mystorageaccount
    kete.routes.asq.destination.sas-token=sv=2024-08-04&ss=q&srt=sco&sp=wau&se=...&sig=...
    kete.routes.asq.destination.queue=keycloak-events
    ```

=== "Azurite Emulator"

    ```bash
    kete.routes.asq.destination.kind=azure-storage-queue
    kete.routes.asq.destination.account-name=devstoreaccount1
    kete.routes.asq.destination.account-key=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==
    kete.routes.asq.destination.queue=keycloak-events
    kete.routes.asq.destination.url=http://azurite:10001/devstoreaccount1
    ```

=== "Custom Endpoint"

    ```bash
    kete.routes.asq.destination.kind=azure-storage-queue
    kete.routes.asq.destination.account-name=mystorageaccount
    kete.routes.asq.destination.account-key=your-account-key
    kete.routes.asq.destination.queue=keycloak-events
    kete.routes.asq.destination.url=https://mystorageaccount.queue.core.windows.net
    ```



## Features

- Azure Storage Queue REST API integration (no Azure SDK dependency)
- Three authentication methods: Shared Key, Connection String, SAS Token
- Emulator support via Azurite for local development and testing
- Queue name templating with variables
- Configurable message TTL
- Messages encoded as Base64
- TLS/mTLS support
- Custom endpoint URL support



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `azure-storage-queue` | `azure-storage-queue` |
| `destination.queue` | Queue name (supports templating) | `keycloak-events` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.url` | `https://{account-name}.queue.core.windows.net` | Custom endpoint URL | `http://azurite:10001/devstoreaccount1` |
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

Exactly **one** of the following authentication methods must be used:

#### Connection String

| Property | Description |
|----------|-------------|
| `destination.connection-string` | Full Azure Storage connection string |

The connection string is parsed to extract `AccountName`, `AccountKey` or `SharedAccessSignature`, `QueueEndpoint`, `DefaultEndpointsProtocol`, and `EndpointSuffix`. Mutually exclusive with individual credential properties.

#### Shared Key (Account Name + Account Key)

| Property | Description |
|----------|-------------|
| `destination.account-name` | Azure Storage account name |
| `destination.account-key` | Azure Storage account access key |

Uses HMAC-SHA256 signing per the Azure Storage REST API specification.

#### SAS Token

| Property | Description |
|----------|-------------|
| `destination.sas-token` | Shared Access Signature token |
| `destination.account-name` | _(optional)_ Storage account name |
| `destination.url` | _(optional)_ Custom queue endpoint URL |

The SAS token is appended as query parameters to each request. No signing is needed.

!!! note "Emulator Mode"
    When using Azurite, set `destination.url` to the emulator endpoint (e.g., `http://azurite:10001/devstoreaccount1`) and use the well-known Azurite development credentials.

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
kete.routes.prod.destination.account-name=prodstorageaccount
kete.routes.prod.destination.account-key=your-production-account-key
kete.routes.prod.destination.queue=keycloak-events
kete.routes.prod.destination.message-ttl=86400
kete.routes.prod.destination.timeout-seconds=30
```

### Example 2: Per-Realm Queues

```bash
# Route events to different queues per realm
kete.routes.events.destination.kind=azure-storage-queue
kete.routes.events.destination.account-name=mystorageaccount
kete.routes.events.destination.account-key=your-account-key
kete.routes.events.destination.queue=keycloak-${realmLowerCase}-events
```

### Example 3: Local Development with Azurite

```bash
kete.routes.local.destination.kind=azure-storage-queue
kete.routes.local.realm-matchers.realm=list:master
kete.routes.local.event-matchers.filter=glob:*
kete.routes.local.destination.account-name=devstoreaccount1
kete.routes.local.destination.account-key=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==
kete.routes.local.destination.queue=keycloak-events
kete.routes.local.destination.url=http://localhost:10001/devstoreaccount1
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
