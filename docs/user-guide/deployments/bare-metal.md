# Bare Metal Deployment

Add the KETE extension to Keycloak running on bare metal or VMs.

## Overview

Deploying KETE means copying the JAR file into Keycloak's `providers/` directory. This method works for:
- Traditional server installations
- VM deployments
- Manual installations
- Development environments

## Prerequisites

- Keycloak 25.0.6 or later installed
- Java 21 or later
- Access to Keycloak's filesystem
- Appropriate file permissions

## Installation Steps

### 1. Download the Extension

Download the latest JAR from [GitHub Releases](https://github.com/FortuneN/kete/releases):

=== "Linux/macOS"

    ```bash
    curl -L -o kete.jar https://github.com/FortuneN/kete/releases/latest/download/kete.jar
    ```

=== "Windows (PowerShell)"

    ```powershell
    Invoke-WebRequest -Uri https://github.com/FortuneN/kete/releases/latest/download/kete.jar -OutFile kete.jar
    ```

### 2. Copy to Keycloak

=== "Linux/macOS"

    ```bash
    cp kete.jar /opt/keycloak/providers/
    chown keycloak:keycloak /opt/keycloak/providers/kete.jar
    ```

=== "Windows (PowerShell)"

    ```powershell
    Copy-Item kete.jar C:\keycloak\providers\
    ```

**Default Keycloak paths:**
- Linux/Unix: `/opt/keycloak/providers/`
- Windows: `C:\keycloak\providers\`
- Custom: `$KEYCLOAK_HOME/providers/`

### 3. Rebuild Keycloak

Keycloak needs to rebuild its internal cache to recognize new providers:

=== "Linux/macOS"

    ```bash
    /opt/keycloak/bin/kc.sh build
    ```

=== "Windows"

    ```cmd
    C:\keycloak\bin\kc.bat build
    ```

### 4. Configure Environment Variables

Add configuration to your environment or startup script:

=== "Linux/macOS"

    Add to `/etc/environment` or your startup script:

    ```bash
    export kete.routes.kafka-example.destination.kind=kafka
    export kete.routes.kafka-example.destination.bootstrap.servers=localhost:9092
    export kete.routes.kafka-example.destination.topic=keycloak-events
    export kete.routes.kafka-example.event-matchers.filter=list:LOGIN,LOGOUT
    ```

=== "Windows (PowerShell)"

    Set system environment variables:

    ```powershell
    [Environment]::SetEnvironmentVariable("kete.routes.kafka-example.destination.kind", "kafka", "Machine")
    [Environment]::SetEnvironmentVariable("kete.routes.kafka-example.destination.bootstrap.servers", "localhost:9092", "Machine")
    [Environment]::SetEnvironmentVariable("kete.routes.kafka-example.destination.topic", "keycloak-events", "Machine")
    [Environment]::SetEnvironmentVariable("kete.routes.kafka-example.event-matchers.filter", "list:LOGIN,LOGOUT", "Machine")
    ```

### 5. Restart Keycloak

=== "Linux/macOS (systemd)"

    ```bash
    sudo systemctl restart keycloak
    ```

=== "Linux/macOS (manual)"

    ```bash
    /opt/keycloak/bin/kc.sh start
    ```

=== "Windows (service)"

    ```powershell
    Restart-Service keycloak
    ```

=== "Windows (manual)"

    ```cmd
    C:\keycloak\bin\kc.bat start
    ```

## Configuration Methods

### Method 1: Environment Variables

Set in your shell profile, systemd service file, or Windows environment variables.

### Method 2: Configuration File

Create a properties file and reference it:

**keycloak.conf** or custom properties file:
```properties
kete.routes.kafka-example.destination.kind=kafka
kete.routes.kafka-example.destination.bootstrap.servers=localhost:9092
kete.routes.kafka-example.destination.topic=keycloak-events
```

### Method 3: Startup Arguments

Pass directly to Keycloak:

=== "Linux/macOS"

    ```bash
    /opt/keycloak/bin/kc.sh start \
      -Dkete.routes.kafka-example.destination.kind=kafka \
      -Dkete.routes.kafka-example.destination.bootstrap.servers=localhost:9092
    ```

=== "Windows (PowerShell)"

    ```powershell
    C:\keycloak\bin\kc.bat start `
      -Dkete.routes.kafka-example.destination.kind=kafka `
      -Dkete.routes.kafka-example.destination.bootstrap.servers=localhost:9092
    ```

## Systemd Service Configuration

For Linux systems using systemd:

**/etc/systemd/system/keycloak.service**:
```ini
[Unit]
Description=Keycloak Server
After=network.target

[Service]
Type=simple
User=keycloak
Group=keycloak
Environment="kete.routes.kafka-example.destination.kind=kafka"
Environment="kete.routes.kafka-example.destination.bootstrap.servers=localhost:9092"
ExecStart=/opt/keycloak/bin/kc.sh start
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

Reload and restart:
```bash
sudo systemctl daemon-reload
sudo systemctl restart keycloak
```

## Upgrades

### Upgrading the Extension

=== "Linux/macOS (systemd)"

    ```bash
    # 1. Stop Keycloak
    sudo systemctl stop keycloak

    # 2. Remove old version
    rm /opt/keycloak/providers/kete.jar

    # 3. Download new version
    curl -L -o /opt/keycloak/providers/kete.jar https://github.com/FortuneN/kete/releases/latest/download/kete.jar

    # 4. Rebuild
    /opt/keycloak/bin/kc.sh build

    # 5. Start Keycloak
    sudo systemctl start keycloak
    ```

=== "Windows (PowerShell)"

    ```powershell
    # 1. Stop Keycloak service
    Stop-Service keycloak

    # 2. Remove old version
    Remove-Item C:\keycloak\providers\kete.jar

    # 3. Download new version
    Invoke-WebRequest -Uri https://github.com/FortuneN/kete/releases/latest/download/kete.jar -OutFile C:\keycloak\providers\kete.jar

    # 4. Rebuild
    C:\keycloak\bin\kc.bat build

    # 5. Start Keycloak service
    Start-Service keycloak
    ```
