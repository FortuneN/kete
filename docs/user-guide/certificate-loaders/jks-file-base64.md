# JKS File Base64

Load Base64-encoded Java KeyStore content. Format is auto-detected.

| Property | Required | Description |
|----------|:--------:|-------------|
| `loader.kind` | ✓ | `jks-file-base64` |
| `loader.base64` | ✓ | Base64-encoded keystore content |
| `password` | | Store password (if password-protected) |

## Notes

- Auto-detects JKS and PKCS#12 formats
- Common extensions: `.jks`, `.keystore`, `.p12`, `.pfx`

## Example

```bash
kete.routes.myroute.destination.tls.enabled=true

# Trust store (CA certificates)
kete.routes.myroute.destination.tls.trust-store.loader.kind=jks-file-base64
kete.routes.myroute.destination.tls.trust-store.loader.base64=/u3+7QAAAAIAAAABAAAAAQAFYWxpYXM...
kete.routes.myroute.destination.tls.trust-store.password=changeit

# Key store (client certificate + private key)
kete.routes.myroute.destination.tls.key-store.loader.kind=jks-file-base64
kete.routes.myroute.destination.tls.key-store.loader.base64=/u3+7QAAAAIAAAABAAAAAQAFa2V5c3Rv...
kete.routes.myroute.destination.tls.key-store.password=changeit
```
