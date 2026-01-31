# Docker Deployment

Run KETE with Keycloak in Docker.



## Quick Start

=== "Linux/macOS"

    ```bash
    docker run -d \
      -p 8080:8080 \
      -e KEYCLOAK_ADMIN=admin \
      -e KEYCLOAK_ADMIN_PASSWORD=admin \
      -e kete.routes.demo.destination.kind=http \
      -e kete.routes.demo.destination.url=https://webhook.site/YOUR-ID \
      ghcr.io/fortunen/kete:latest \
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
      ghcr.io/fortunen/kete:latest `
      start-dev
    ```

Open [http://localhost:8080](http://localhost:8080), log in, and events appear at your webhook.



## Image Options

| Image | Description |
|-------|-------------|
| `ghcr.io/fortunen/kete:latest` | Latest stable release |
| `ghcr.io/fortunen/kete:X.Y.Z` | Specific version |
| `ghcr.io/fortunen/kete:develop` | Latest development build |



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
      ghcr.io/fortunen/kete:latest \
      start-dev
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
      ghcr.io/fortunen/kete:latest `
      start-dev
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
      ghcr.io/fortunen/kete:latest \
      start
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
      ghcr.io/fortunen/kete:latest `
      start
    ```
