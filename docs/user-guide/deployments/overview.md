# Deployments

Get KETE running in your environment.

## Choose Your Path

| I want to... | Guide | Time |
|--------------|-------|:----:|
| 🚀 Try it out quickly | [Docker](docker.md) | 2 min |
| 🐳 Run a full local stack | [Docker Compose](docker-compose.md) | 10 min |
| ☸️ Deploy to production | [Kubernetes](kubernetes.md) | 30 min |
| 🔧 Add to existing Keycloak | [Bare Metal](bare-metal.md) | 5 min |

## Quickest Start

=== "Linux/macOS"

    ```bash
    docker run -d -p 8080:8080 \
      -e KEYCLOAK_ADMIN=admin \
      -e KEYCLOAK_ADMIN_PASSWORD=admin \
      -e kete.routes.demo.destination.kind=http \
      -e kete.routes.demo.destination.url=https://webhook.site/YOUR-ID \
      ghcr.io/fortunen/kete:latest start-dev
    ```

=== "Windows (PowerShell)"

    ```powershell
    docker run -d -p 8080:8080 `
      -e KEYCLOAK_ADMIN=admin `
      -e KEYCLOAK_ADMIN_PASSWORD=admin `
      -e kete.routes.demo.destination.kind=http `
      -e kete.routes.demo.destination.url=https://webhook.site/YOUR-ID `
      ghcr.io/fortunen/kete:latest start-dev
    ```

Open [http://localhost:8080](http://localhost:8080) and log in. Events appear at [webhook.site](https://webhook.site).

## Configuration Pattern

All KETE settings follow this pattern:

```
kete.routes.<route-name>.<setting>=<value>
```

See [Routes](../routes.md) for details.
