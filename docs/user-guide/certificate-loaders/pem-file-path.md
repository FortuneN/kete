# PEM File Path

Load certificates from a PEM file on the filesystem.

| Property | Required | Description |
|----------|:--------:|-------------|
| `loader.kind` | ✓ | `pem-file-path` |
| `loader.path` | ✓ | Absolute path to PEM file |

## Notes

- Supports certificates and private keys
- Multiple certificates can be concatenated in one file
- Common extensions: `.pem`, `.crt`, `.cer`, `.key`

## Example

```bash
kete.routes.myroute.destination.tls=true

# Trust store (CA certificates)
kete.routes.myroute.destination.trust-store.loader.kind=pem-file-path
kete.routes.myroute.destination.trust-store.loader.path=/certs/ca.pem

# Key store (client certificate + private key)
kete.routes.myroute.destination.key-store.loader.kind=pem-file-path
kete.routes.myroute.destination.key-store.loader.path=/certs/client.pem
```
