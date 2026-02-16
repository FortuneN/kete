# Docker Deployment

Add KETE to your Keycloak Docker image by copying the JAR into the `providers/` directory.

## How It Works

KETE is a single JAR file — not an image or container. To deploy it with Docker, build a custom Keycloak image that includes `kete.jar` in Keycloak's `providers/` directory. This is the standard mechanism for installing any Keycloak extension.



## Building Your Image

### 1. Download the JAR

Download the latest release from [GitHub Releases](https://github.com/FortuneN/kete/releases):

=== "Linux/macOS"

    ```bash
    curl -L -o kete.jar https://github.com/FortuneN/kete/releases/latest/download/kete.jar
    ```

=== "Windows (PowerShell)"

    ```powershell
    Invoke-WebRequest -Uri https://github.com/FortuneN/kete/releases/latest/download/kete.jar -OutFile kete.jar
    ```

### 2. Create a Dockerfile

```dockerfile
FROM quay.io/keycloak/keycloak:26.0.7
COPY kete.jar /opt/keycloak/providers/
RUN /opt/keycloak/bin/kc.sh build
```

### 3. Build and Run

=== "Linux/macOS"

    ```bash
    docker build -t my-keycloak .

    docker run -d \
      -p 8080:8080 \
      -e KEYCLOAK_ADMIN=admin \
      -e KEYCLOAK_ADMIN_PASSWORD=admin \
      -e kete.routes.events.destination.kind=http \
      -e kete.routes.events.destination.url=https://webhook.site/YOUR-ID \
      my-keycloak start-dev
    ```

=== "Windows (PowerShell)"

    ```powershell
    docker build -t my-keycloak .

    docker run -d `
      -p 8080:8080 `
      -e KEYCLOAK_ADMIN=admin `
      -e KEYCLOAK_ADMIN_PASSWORD=admin `
      -e kete.routes.events.destination.kind=http `
      -e kete.routes.events.destination.url=https://webhook.site/YOUR-ID `
      my-keycloak start-dev
    ```

Open [http://localhost:8080](http://localhost:8080), log in, and events appear at your webhook.



## Configuration via Environment

Pass KETE configuration as environment variables:

=== "Linux/macOS"

    ```bash
    docker run -d \
      -p 8080:8080 \
      -e KEYCLOAK_ADMIN=admin \
      -e KEYCLOAK_ADMIN_PASSWORD=admin \
      -e kete.routes.kafka.destination.kind=kafka \
      -e kete.routes.kafka.destination.bootstrap.servers=kafka:9092 \
      -e kete.routes.kafka.destination.topic=keycloak-events \
      -e kete.routes.kafka.event-matchers.filter=list:LOGIN,LOGOUT \
      my-keycloak start-dev
    ```

=== "Windows (PowerShell)"

    ```powershell
    docker run -d `
      -p 8080:8080 `
      -e KEYCLOAK_ADMIN=admin `
      -e KEYCLOAK_ADMIN_PASSWORD=admin `
      -e kete.routes.kafka.destination.kind=kafka `
      -e kete.routes.kafka.destination.bootstrap.servers=kafka:9092 `
      -e kete.routes.kafka.destination.topic=keycloak-events `
      -e kete.routes.kafka.event-matchers.filter=list:LOGIN,LOGOUT `
      my-keycloak start-dev
    ```



## Production Mode

For production, use `start` instead of `start-dev`:

=== "Linux/macOS"

    ```bash
    docker run -d \
      -p 8443:8443 \
      -e KC_HOSTNAME=auth.example.com \
      -e KC_DB=postgres \
      -e KC_DB_URL=jdbc:postgresql://db:5432/keycloak \
      -e KC_DB_USERNAME=keycloak \
      -e KC_DB_PASSWORD=secret \
      -e kete.routes.prod.destination.kind=kafka \
      -e kete.routes.prod.destination.bootstrap.servers=kafka:9092 \
      -e kete.routes.prod.destination.topic=keycloak-events \
      my-keycloak start
    ```

=== "Windows (PowerShell)"

    ```powershell
    docker run -d `
      -p 8443:8443 `
      -e KC_HOSTNAME=auth.example.com `
      -e KC_DB=postgres `
      -e KC_DB_URL=jdbc:postgresql://db:5432/keycloak `
      -e KC_DB_USERNAME=keycloak `
      -e KC_DB_PASSWORD=secret `
      -e kete.routes.prod.destination.kind=kafka `
      -e kete.routes.prod.destination.bootstrap.servers=kafka:9092 `
      -e kete.routes.prod.destination.topic=keycloak-events `
      my-keycloak start
    ```



## Upgrades

To upgrade KETE:

1. Download the new `kete.jar` from [GitHub Releases](https://github.com/FortuneN/kete/releases)
2. Rebuild your Docker image
3. Redeploy your container



## Quick Start Image (Demo Only)

!!! warning "Demo Only"
    The `ghcr.io/fortunen/kete/quick-start-keycloak` image is a pre-built convenience image with Keycloak and the KETE JAR pre-installed. It is intended for demos and quick starts only — not for production. For production, build your own image as shown above.

| Image | Description |
|-------|-------------|
| `ghcr.io/fortunen/kete/quick-start-keycloak:latest` | Latest stable release |
| `ghcr.io/fortunen/kete/quick-start-keycloak:X.Y.Z` | Specific version |
| `ghcr.io/fortunen/kete/quick-start-keycloak:develop` | Latest development build |

=== "Linux/macOS"

    ```bash
    docker run -d \
      -p 8080:8080 \
      -e KEYCLOAK_ADMIN=admin \
      -e KEYCLOAK_ADMIN_PASSWORD=admin \
      -e kete.routes.demo.destination.kind=http \
      -e kete.routes.demo.destination.url=https://webhook.site/YOUR-ID \
      ghcr.io/fortunen/kete/quick-start-keycloak:latest \
      start-dev
    ```

=== "Windows (PowerShell)"

    ```powershell
    docker run -d `
      -p 8080:8080 `
      -e KEYCLOAK_ADMIN=admin `
      -e KEYCLOAK_ADMIN_PASSWORD=admin `
      -e kete.routes.demo.destination.kind=http `
      -e kete.routes.demo.destination.url=https://webhook.site/YOUR-ID `
      ghcr.io/fortunen/kete/quick-start-keycloak:latest `
      start-dev
    ```
