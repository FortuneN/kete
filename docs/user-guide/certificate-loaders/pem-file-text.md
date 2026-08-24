# PEM File Text

Embed raw PEM content directly in configuration.

| Property | Required | Description |
|----------|:--------:|-------------|
| `loader.kind` | ✓ | `pem-file-text` |
| `loader.text` | ✓ | Raw PEM content |

## Notes

- The value must contain real line breaks: in properties files escape them as `\n`; environment variables must carry actual newline characters (e.g. multi-line values from a Kubernetes secret) — a literal backslash-n is not unescaped
- Supports certificates and private keys. For key stores the PEM must contain the private key **and** its certificate chain together; a key without a certificate is ignored
- Private keys must be unencrypted (PKCS#8 `BEGIN PRIVATE KEY` or PKCS#1 `BEGIN RSA PRIVATE KEY`); encrypted keys are rejected
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
