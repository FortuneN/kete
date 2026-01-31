# JKS File Path

Load a Java KeyStore from a file on the filesystem. Format is auto-detected.

| Property | Required | Description |
|----------|:--------:|-------------|
| `loader.kind` | ✓ | `jks-file-path` |
| `loader.path` | ✓ | Absolute path to keystore file |
| `password` | | Store password (if password-protected) |

## Notes

- Auto-detects JKS and PKCS#12 formats
- Common extensions: `.jks`, `.keystore`, `.p12`, `.pfx`

## Example

```bash
kete.routes.myroute.destination.tls=true

# Trust store (CA certificates)
kete.routes.myroute.destination.trust-store.loader.kind=jks-file-path
kete.routes.myroute.destination.trust-store.loader.path=/certs/truststore.jks
kete.routes.myroute.destination.trust-store.password=changeit

# Key store (client certificate + private key)
kete.routes.myroute.destination.key-store.loader.kind=jks-file-path
kete.routes.myroute.destination.key-store.loader.path=/certs/keystore.jks
kete.routes.myroute.destination.key-store.password=changeit
```
