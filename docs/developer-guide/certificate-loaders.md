# Certificate Loaders

Certificate loaders provide a flexible way to load TLS certificates and keys from various sources and formats. They are used for configuring key-stores (client certificates for mTLS) and trust-stores (CA certificates for server verification).



## Overview

The certificate loader system supports multiple certificate formats and sources:

| Loader Kind | Component Name | Source | Format | Use Case |
|-------------|----------------|--------|--------|----------|
| **PKCS12 File Path** | `pkcs12-file-path` | File path | PKCS12 (.p12, .pfx) | Standard keystore files |
| **PKCS12 File Base64** | `pkcs12-file-base64` | Inline | PKCS12 (Base64) | Kubernetes secrets, env vars |
| **PKCS7 File Path** | `pkcs7-file-path` | File path | PKCS7 (.p7b, .p7c) | Certificate bundles/chains |
| **PKCS7 File Base64** | `pkcs7-file-base64` | Inline | PKCS7 (Base64) | Certificate bundles in secrets |
| **JKS File Path** | `jks-file-path` | File path | JKS/PKCS12 | Legacy Java keystores |
| **JKS File Base64** | `jks-file-base64` | Inline | JKS/PKCS12 (Base64) | Embedded keystores |
| **PEM File Path** | `pem-file-path` | File path | PEM | Standard certs/keys |
| **PEM File Base64** | `pem-file-base64` | Inline | PEM (Base64) | Kubernetes secrets |
| **PEM File Text** | `pem-file-text` | Inline | PEM (raw) | Direct PEM content |
| **DER File Path** | `der-file-path` | File path | DER | Binary certificates |
| **DER File Base64** | `der-file-base64` | Inline | DER (Base64) | Embedded binary certs |



## Configuration Structure

Certificate loaders are configured within TLS settings for both key-stores and trust-stores:

```bash
# Key-store (client certificate for mTLS)
kete.routes.<NAME>.destination.tls.key-store.loader.kind=<loader-kind>
kete.routes.<NAME>.destination.tls.key-store.loader.<property>=<value>
kete.routes.<NAME>.destination.tls.key-store.password=<password>

# Trust-store (CA certificate for server verification)
kete.routes.<NAME>.destination.tls.trust-store.loader.kind=<loader-kind>
kete.routes.<NAME>.destination.tls.trust-store.loader.<property>=<value>
kete.routes.<NAME>.destination.tls.trust-store.password=<password>
```



## Loader Kinds

### PKCS12 File Path Loader (`pkcs12-file-path`)

Loads a PKCS12 keystore from a file path. Most common format for client certificates.

**Properties:**

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `path` | Yes | - | Absolute path to PKCS12 file |

**Example:**
```bash
kete.routes.secure.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.secure.destination.tls.key-store.loader.path=/opt/keycloak/certs/client.p12
kete.routes.secure.destination.tls.key-store.password=changeit
```



### PKCS12 File Base64 Loader (`pkcs12-file-base64`)

Loads a PKCS12 keystore from Base64-encoded content. Ideal for Kubernetes secrets or environment variables.

**Properties:**

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `base64` | Yes | - | Base64-encoded PKCS12 content |

**Example:**
```bash
kete.routes.secure.destination.tls.key-store.loader.kind=pkcs12-file-base64
kete.routes.secure.destination.tls.key-store.loader.base64=MIIKegIBAzCCCj4GCSqGSIb3...
kete.routes.secure.destination.tls.key-store.password=changeit
```

**Encoding a PKCS12 file:**
```bash
# Linux/macOS
base64 -w 0 client.p12 > client.p12.b64

# Windows PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("client.p12")) | Out-File client.p12.b64
```



### JKS File Path Loader (`jks-file-path`)

Loads any Java KeyStore (JKS or PKCS12) from a file path. Auto-detects format.

**Properties:**

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `path` | Yes | - | Absolute path to keystore file |

**Example:**
```bash
kete.routes.secure.destination.tls.trust-store.loader.kind=jks-file-path
kete.routes.secure.destination.tls.trust-store.loader.path=/opt/keycloak/certs/truststore.jks
kete.routes.secure.destination.tls.trust-store.password=changeit
```



### JKS File Base64 Loader (`jks-file-base64`)

Loads any Java KeyStore (JKS or PKCS12) from Base64-encoded content.

**Properties:**

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `base64` | Yes | - | Base64-encoded keystore content |

**Example:**
```bash
kete.routes.secure.destination.tls.trust-store.loader.kind=jks-file-base64
kete.routes.secure.destination.tls.trust-store.loader.base64=/u3+7QAAAAIAAAABAAAAAQAFYWxpYXM...
kete.routes.secure.destination.tls.trust-store.password=changeit
```



### PEM File Path Loader (`pem-file-path`)

Loads certificates and/or private keys from a PEM-formatted file. Supports certificate chains and encrypted private keys.

**Properties:**

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `path` | Yes | - | Absolute path to PEM file |

**Example - CA Certificate:**
```bash
kete.routes.secure.destination.tls.trust-store.loader.kind=pem-file-path
kete.routes.secure.destination.tls.trust-store.loader.path=/opt/keycloak/certs/ca.pem
```

**Example - Client Certificate + Key:**
```bash
kete.routes.secure.destination.tls.key-store.loader.kind=pem-file-path
kete.routes.secure.destination.tls.key-store.loader.path=/opt/keycloak/certs/client-combined.pem
kete.routes.secure.destination.tls.key-store.password=keypassword
```

**PEM File Format:**
```pem
-----BEGIN CERTIFICATE-----
MIIDXTCCAkWgAwIBAgIJAJC1...
-----END CERTIFICATE-----
-----BEGIN RSA PRIVATE KEY-----
MIIEowIBAAKCAQEAw7...
-----END RSA PRIVATE KEY-----
```



### PEM File Base64 Loader (`pem-file-base64`)

Loads PEM content that has been Base64-encoded. Useful for embedding PEM content in environment variables or secrets.

**Properties:**

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `base64` | Yes | - | Base64-encoded PEM content |

**Example:**
```bash
kete.routes.secure.destination.tls.trust-store.loader.kind=pem-file-base64
kete.routes.secure.destination.tls.trust-store.loader.base64=LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0t...
```

**Encoding PEM to Base64:**
```bash
# Linux/macOS
base64 -w 0 ca.pem > ca.pem.b64

# Windows PowerShell
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes((Get-Content -Raw ca.pem))) | Out-File ca.pem.b64
```



### PEM File Text Loader (`pem-file-text`)

Loads raw PEM content directly from configuration. Useful for simple setups or testing.

**Properties:**

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `text` | Yes | - | Raw PEM content (with newlines escaped) |

**Example:**
```bash
kete.routes.secure.destination.tls.trust-store.loader.kind=pem-file-text
kete.routes.secure.destination.tls.trust-store.loader.text=-----BEGIN CERTIFICATE-----\nMIIDXTCC...kWgAwIB\n-----END CERTIFICATE-----
```

> **Note:** Newlines in PEM content must be escaped as `\n` when using environment variables or properties files.



### DER File Path Loader (`der-file-path`)

Loads a DER-encoded (binary) X.509 certificate from a file.

**Properties:**

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `path` | Yes | - | Absolute path to DER file |

**Example:**
```bash
kete.routes.secure.destination.tls.trust-store.loader.kind=der-file-path
kete.routes.secure.destination.tls.trust-store.loader.path=/opt/keycloak/certs/ca.der
```



### DER File Base64 Loader (`der-file-base64`)

Loads a DER-encoded certificate from Base64-encoded content.

**Properties:**

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `base64` | Yes | - | Base64-encoded DER content |

**Example:**
```bash
kete.routes.secure.destination.tls.trust-store.loader.kind=der-file-base64
kete.routes.secure.destination.tls.trust-store.loader.base64=MIIDXTCCAkWgAwIBAgIJAJC1...
```



### PKCS7 File Path Loader (`pkcs7-file-path`)

Loads certificates from a PKCS#7 file (.p7b, .p7c). PKCS#7 is a certificate-only format commonly used for certificate chains and bundles. It cannot contain private keys, so this loader is **only suitable for trust-stores**.

**Properties:**

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `path` | Yes | - | Absolute path to PKCS#7 file |

**Example:**
```bash
kete.routes.secure.destination.tls.trust-store.loader.kind=pkcs7-file-path
kete.routes.secure.destination.tls.trust-store.loader.path=/opt/keycloak/certs/ca-chain.p7b
```

> **Note:** PKCS#7 files can contain multiple certificates. Aliases are auto-generated from each certificate's Common Name (CN). If no CN is found, the alias is derived from the certificate's serial number.



### PKCS7 File Base64 Loader (`pkcs7-file-base64`)

Loads certificates from Base64-encoded PKCS#7 content. Ideal for Kubernetes secrets or environment variables containing certificate bundles.

**Properties:**

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `base64` | Yes | - | Base64-encoded PKCS#7 content |

**Example:**
```bash
kete.routes.secure.destination.tls.trust-store.loader.kind=pkcs7-file-base64
kete.routes.secure.destination.tls.trust-store.loader.base64=MIIHGgYJKoZIhvcNAQcCoIIHCz...
```

> **Note:** Aliases are auto-generated from each certificate's Common Name (CN). If no CN is found, the alias is derived from the certificate's serial number (e.g., `cert-1a2b3c`).

**Encoding a PKCS#7 file:**
```bash
# Linux/macOS
base64 -w 0 ca-chain.p7b > ca-chain.p7b.b64

# Windows PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("ca-chain.p7b")) | Out-File ca-chain.p7b.b64
```

> **Note:** PKCS#7 cannot contain private keys. Use PKCS#12 or PEM formats for client certificates (key-stores).



## Complete Examples

### Example 1: mTLS with PKCS12 Files

Standard mutual TLS using file-based keystores:

```bash
kete.routes.mtls-api.destination.kind=http
kete.routes.mtls-api.destination.host=secure-api.example.com
kete.routes.mtls-api.destination.port=443
kete.routes.mtls-api.destination.tls.enabled=true

# Client certificate (key-store)
kete.routes.mtls-api.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.mtls-api.destination.tls.key-store.loader.path=/opt/keycloak/certs/client.p12
kete.routes.mtls-api.destination.tls.key-store.password=clientpass

# CA certificate (trust-store)
kete.routes.mtls-api.destination.tls.trust-store.loader.kind=pkcs12-file-path
kete.routes.mtls-api.destination.tls.trust-store.loader.path=/opt/keycloak/certs/truststore.p12
kete.routes.mtls-api.destination.tls.trust-store.password=trustpass
```



### Example 2: Kubernetes with Base64 Secrets

Containerized deployment using Base64-encoded certificates:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: tls-certs
type: Opaque
stringData:
  client-cert: "MIIKegIBAzCCCj4GCSqGSIb3..."  # Base64 PKCS12
  ca-cert: "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0t..."  # Base64 PEM

apiVersion: v1
kind: ConfigMap
metadata:
  name: events-config
data:
  kete.routes.secure.destination.tls.enabled: "true"
  kete.routes.secure.destination.tls.key-store.loader.kind: "pkcs12-file-base64"
  kete.routes.secure.destination.tls.trust-store.loader.kind: "pem-file-base64"
```

```yaml
# Pod spec referencing secrets
env:
  - name: kete.routes.secure.destination.tls.key-store.loader.base64
    valueFrom:
      secretKeyRef:
        name: tls-certs
        key: client-cert
  - name: kete.routes.secure.destination.tls.key-store.password
    valueFrom:
      secretKeyRef:
        name: tls-certs
        key: client-cert-password
  - name: kete.routes.secure.destination.tls.trust-store.loader.base64
    valueFrom:
      secretKeyRef:
        name: tls-certs
        key: ca-cert
```



### Example 3: PEM Files for Let's Encrypt

Using Let's Encrypt certificates in PEM format:

```bash
kete.routes.letsencrypt.destination.kind=http
kete.routes.letsencrypt.destination.host=api.example.com
kete.routes.letsencrypt.destination.tls.enabled=true

# Server CA verification
kete.routes.letsencrypt.destination.tls.trust-store.loader.kind=pem-file-path
kete.routes.letsencrypt.destination.tls.trust-store.loader.path=/etc/ssl/certs/ca-certificates.crt
```



### Example 4: Mixed Formats

Combining different loader kinds for key-store and trust-store:

```bash
kete.routes.mixed.destination.kind=http
kete.routes.mixed.destination.host=api.internal.company.com
kete.routes.mixed.destination.tls.enabled=true

# Client cert from PKCS12 file
kete.routes.mixed.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.mixed.destination.tls.key-store.loader.path=/certs/client.p12
kete.routes.mixed.destination.tls.key-store.password=clientpass

# CA from PEM Base64 (injected from secret)
kete.routes.mixed.destination.tls.trust-store.loader.kind=pem-file-base64
kete.routes.mixed.destination.tls.trust-store.loader.base64=${CA_CERT_BASE64}
```



## Choosing the Right Loader

| Scenario | Recommended Loader | Notes |
|----------|-------------------|-------|
| File-based deployment | `pkcs12-file-path`, `pem-file-path` | Simple, familiar to ops teams |
| Kubernetes secrets | `pkcs12-file-base64`, `pem-file-base64` | No file mounts needed |
| Environment variables | `*-file-base64` variants | Portable, no file system dependencies |
| Let's Encrypt / PEM certs | `pem-file-path`, `pem-file-base64` | Native format, no conversion |
| Legacy Java keystores | `jks-file-path`, `jks-file-base64` | JKS and PKCS12 auto-detection |
| Binary DER certificates | `der-file-path`, `der-file-base64` | Raw X.509 format |
| Certificate bundles/chains | `pkcs7-file-path`, `pkcs7-file-base64` | Trust-store only (no keys) |
| Windows/IIS CA exports | `pkcs7-file-path`, `pkcs7-file-base64` | Common in enterprise environments |
| Testing/development | `pem-file-text` | Quick inline configuration |



## Certificate Conversion Commands

### PEM to PKCS12

```bash
openssl pkcs12 -export \
  -in client.pem \
  -inkey client-key.pem \
  -out client.p12 \
  -name "client-cert" \
  -password pass:changeit
```

### PKCS12 to PEM

```bash
# Extract certificate
openssl pkcs12 -in client.p12 -clcerts -nokeys -out client.pem

# Extract private key
openssl pkcs12 -in client.p12 -nocerts -nodes -out client-key.pem
```

### DER to PEM

```bash
openssl x509 -inform DER -in cert.der -out cert.pem
```

### PEM to DER

```bash
openssl x509 -outform DER -in cert.pem -out cert.der
```



## Troubleshooting

### Common Issues

| Error | Cause | Solution |
|-------|-------|----------|
| `path is required` | Missing `path` property | Add `loader.path=/path/to/cert` |
| `base64 is required` | Missing `base64` property | Add `loader.base64=...` |
| `text is required` | Missing `text` property | Add `loader.text=-----BEGIN...` |
| `Password verification failed` | Wrong keystore password | Verify `key-store.password` value |
| `Certificate parsing error` | Wrong loader kind for format | Match loader kind to certificate format |

### Debugging

Enable debug logging to troubleshoot keystore loading:

```bash
# Use standard Quarkus logging configuration
quarkus.log.category."io.github.fortunen.kete".level=DEBUG
```

Check the Keycloak server log for detailed TLS initialization messages.
