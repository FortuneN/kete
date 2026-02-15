# PEM File Text

Embed raw PEM content directly in configuration.

| Property | Required | Description |
|----------|:--------:|-------------|
| `loader.kind` | ✓ | `pem-file-text` |
| `loader.text` | ✓ | Raw PEM content |

## Notes

- Newlines must be escaped as `\n` in environment variables and single-line configs
- Supports certificates and private keys
- Common extensions: `.pem`, `.crt`, `.cer`, `.key`

## Example

```bash
kete.routes.myroute.destination.tls.enabled=true

# Trust store (CA certificates)
kete.routes.myroute.destination.tls.trust-store.loader.kind=pem-file-text
kete.routes.myroute.destination.tls.trust-store.loader.text=-----BEGIN CERTIFICATE-----\nMIID...

# Key store (client certificate + private key)
kete.routes.myroute.destination.tls.key-store.loader.kind=pem-file-text
kete.routes.myroute.destination.tls.key-store.loader.text=-----BEGIN PRIVATE KEY-----\nMIIE...
```
