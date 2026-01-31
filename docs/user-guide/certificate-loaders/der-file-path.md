# DER File Path

Load a certificate from a DER file on the filesystem.

| Property | Required | Description |
|----------|:--------:|-------------|
| `loader.kind` | ✓ | `der-file-path` |
| `loader.path` | ✓ | Absolute path to DER file |

## Notes

- DER is the binary form of PEM (same data, different encoding)
- Contains a single certificate
- Common extensions: `.der`, `.cer`

## Example

```bash
kete.routes.myroute.destination.tls=true

# Trust store (CA certificate)
kete.routes.myroute.destination.trust-store.loader.kind=der-file-path
kete.routes.myroute.destination.trust-store.loader.path=/certs/ca.der

# Key store (client certificate)
kete.routes.myroute.destination.key-store.loader.kind=der-file-path
kete.routes.myroute.destination.key-store.loader.path=/certs/client.der
```
