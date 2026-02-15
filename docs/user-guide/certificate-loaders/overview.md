# Certificate Loaders

Loaders specify how KETE reads certificates and keys for TLS connections.

| Loader Kind | Source | Description |
|-------------|--------|-------------|
| [`pem-file-path`](pem-file-path.md) | File | PEM file (.pem, .crt, .cer, .key) |
| [`pem-file-base64`](pem-file-base64.md) | Inline | Base64-encoded PEM content |
| [`pem-file-text`](pem-file-text.md) | Inline | PEM text directly in configuration |
| [`pkcs12-file-path`](pkcs12-file-path.md) | File | PKCS#12 file (.p12, .pfx) |
| [`pkcs12-file-base64`](pkcs12-file-base64.md) | Inline | Base64-encoded PKCS#12 content |
| [`pkcs7-file-path`](pkcs7-file-path.md) | File | PKCS#7 file (.p7b, .p7c) — trust stores only |
| [`pkcs7-file-base64`](pkcs7-file-base64.md) | Inline | Base64-encoded PKCS#7 content |
| [`der-file-path`](der-file-path.md) | File | DER file (.der, .cer) — trust stores only |
| [`der-file-base64`](der-file-base64.md) | Inline | Base64-encoded DER content — trust stores only |
| [`jks-file-path`](jks-file-path.md) | File | Java KeyStore (.jks, .keystore) — auto-detects JKS/PKCS12 |
| [`jks-file-base64`](jks-file-base64.md) | Inline | Base64-encoded keystore content |
