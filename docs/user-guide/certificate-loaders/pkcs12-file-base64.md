# PKCS12 File Base64

Load Base64-encoded PKCS#12 content.

| Property | Required | Description |
|----------|:--------:|-------------|
| `loader.kind` | ✓ | `pkcs12-file-base64` |
| `loader.base64` | ✓ | Base64-encoded PKCS#12 content |
| `password` | | Store password (if password-protected) |

## Notes

- Contains both certificate and private key (ideal for mTLS)
- Common extensions: `.p12`, `.pfx`

## Example

```bash
kete.routes.myroute.destination.tls=true

# Key store (client certificate + private key)
kete.routes.myroute.destination.key-store.loader.kind=pkcs12-file-base64
kete.routes.myroute.destination.key-store.loader.base64=MIIKegIBAzCCCj4GCSqGSIb3...
kete.routes.myroute.destination.key-store.password=changeit

# Trust store (CA certificates)
kete.routes.myroute.destination.trust-store.loader.kind=pkcs12-file-base64
kete.routes.myroute.destination.trust-store.loader.base64=MIIFgTCCBGmgAwIBAgIQ...
kete.routes.myroute.destination.trust-store.password=changeit
```
