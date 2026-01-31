# PKCS12 File Path

Load certificates and keys from a PKCS#12 file on the filesystem.

| Property | Required | Description |
|----------|:--------:|-------------|
| `loader.kind` | ✓ | `pkcs12-file-path` |
| `loader.path` | ✓ | Absolute path to .p12 or .pfx file |
| `password` | | Store password (if password-protected) |

## Notes

- Contains both certificate and private key (ideal for mTLS)
- Common extensions: `.p12`, `.pfx`

## Example

```bash
kete.routes.myroute.destination.tls=true

# Key store (client certificate + private key)
kete.routes.myroute.destination.key-store.loader.kind=pkcs12-file-path
kete.routes.myroute.destination.key-store.loader.path=/certs/client.p12
kete.routes.myroute.destination.key-store.password=changeit

# Trust store (CA certificates)
kete.routes.myroute.destination.trust-store.loader.kind=pkcs12-file-path
kete.routes.myroute.destination.trust-store.loader.path=/certs/truststore.p12
kete.routes.myroute.destination.trust-store.password=changeit
```
