# Deployments

Get KETE running in your environment.

## Distribution

KETE is distributed as a single JAR file (`kete.jar`) via [GitHub Releases](https://github.com/FortuneN/kete/releases). Deploying KETE means copying this JAR into your Keycloak `providers/` directory — the standard mechanism for installing any Keycloak extension. Each guide below shows how to achieve this for different environments.

The `ghcr.io/fortunen/kete/quick-start-keycloak` Docker image is a convenience image for demos and quick starts only — it has Keycloak and the KETE JAR pre-installed. For production, build your own Keycloak image with the JAR, or use the Bare Metal approach.

## Choose Your Path

| I want to... | Guide | Time |
|--------------|-------|:----:|
| 🐳 Run with Docker | [Docker](docker.md) | 5 min |
| 🔧 Add to existing Keycloak | [Bare Metal](bare-metal.md) | 5 min |

## Quickest Start (Demo Only)

The quick start image is for demos only. For production, see the deployment guides above.

=== "Linux/macOS"

    ```bash
    docker run -d -p 8080:8080 \
      -e KEYCLOAK_ADMIN=admin \
      -e KEYCLOAK_ADMIN_PASSWORD=admin \
      -e kete.routes.demo.destination.kind=http \
      -e kete.routes.demo.destination.url=https://webhook.site/YOUR-ID \
      ghcr.io/fortunen/kete/quick-start-keycloak:latest start-dev
    ```

=== "Windows (PowerShell)"

    ```powershell
    docker run -d -p 8080:8080 `
      -e KEYCLOAK_ADMIN=admin `
      -e KEYCLOAK_ADMIN_PASSWORD=admin `
      -e kete.routes.demo.destination.kind=http `
      -e kete.routes.demo.destination.url=https://webhook.site/YOUR-ID `
      ghcr.io/fortunen/kete/quick-start-keycloak:latest start-dev
    ```

Open [http://localhost:8080](http://localhost:8080) and log in. Events appear at [webhook.site](https://webhook.site).

## Configuration Pattern

All KETE settings follow this pattern:

```
kete.routes.<route-name>.<setting>=<value>
```

See [Routes](../routes.md) for details.
