# PEM File Path

Load certificates from a PEM file on the filesystem.

| Property | Required | Description |
|----------|:--------:|-------------|
| `loader.kind` | ✓ | `pem-file-path` |
| `loader.path` | ✓ | Absolute path to PEM file |

## Notes

- Supports certificates and private keys. For key stores the PEM must contain the private key **and** its certificate chain together; a key without a certificate is ignored
- Private keys must be unencrypted (PKCS#8 `BEGIN PRIVATE KEY` or PKCS#1 `BEGIN RSA PRIVATE KEY`); encrypted keys are rejected
- Multiple certificates can be concatenated in one file
- Common extensions: `.pem`, `.crt`, `.cer`, `.key`

## Example

```bash
kete.routes.myroute.destination.tls.enabled=true

# Trust store (CA certificates)
kete.routes.myroute.destination.tls.trust-store.loader.kind=pem-file-path
kete.routes.myroute.destination.tls.trust-store.loader.path=/certs/ca.pem

# Key store (client certificate + private key)
kete.routes.myroute.destination.tls.key-store.loader.kind=pem-file-path
kete.routes.myroute.destination.tls.key-store.loader.path=/certs/client.pem
```
