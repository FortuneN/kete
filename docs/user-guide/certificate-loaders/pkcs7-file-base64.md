# PKCS7 File Base64

Load Base64-encoded PKCS#7 content.

| Property | Required | Description |
|----------|:--------:|-------------|
| `loader.kind` | ✓ | `pkcs7-file-base64` |
| `loader.base64` | ✓ | Base64-encoded PKCS#7 content |

## Notes

- **Trust stores only** — PKCS#7 cannot contain private keys
- Common extensions: `.p7b`, `.p7c`
- Often used for CA certificate chains from enterprise PKI

## Example

```bash
kete.routes.myroute.destination.tls.enabled=true

# Trust store only (PKCS#7 cannot contain private keys)
kete.routes.myroute.destination.tls.trust-store.loader.kind=pkcs7-file-base64
kete.routes.myroute.destination.tls.trust-store.loader.base64=MIIHxgYJKoZIhvcNAQcCoIIHtzCCB7M...
```
