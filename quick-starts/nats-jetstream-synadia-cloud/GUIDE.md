# NATS JetStream — Synadia Cloud Quick Start

Stream Keycloak events to [Synadia Cloud](https://www.synadia.com/cloud) (NGS) using NATS JetStream for durable, persistent messaging.

## Prerequisites

- A Synadia Cloud account with JetStream enabled
- A NATS credentials file (`.creds`) generated from the Synadia Cloud dashboard or `nsc`
- Docker and Docker Compose

## Setup

1. **Create an account and credentials** in the [Synadia Cloud dashboard](https://cloud.synadia.com/).
   - Create or select an account with JetStream enabled
   - Generate a user and download the `.creds` file

2. **Create a JetStream stream** for your events. Using the `nats` CLI:
   ```bash
   nats stream add KEYCLOAK_EVENTS \
     --subjects "keycloak.events" \
     --retention limits \
     --max-msgs -1 \
     --max-bytes -1 \
     --max-age 72h \
     --storage file \
     --replicas 1 \
     --creds /path/to/your.creds \
     --server tls://connect.ngs.global
   ```

3. **Copy `.env.example` to `.env`** and fill in your values:
   ```bash
   cp .env.example .env
   ```

4. **Set `NATS_CREDENTIALS_FILE_TEXT`** to the full contents of your `.creds` file, with newlines replaced by `\n` and the whole value in double quotes (Docker Compose only turns `\n` back into line breaks inside double-quoted values):
   ```
   NATS_CREDENTIALS_FILE_TEXT="-----BEGIN NATS USER JWT-----\neyJ0eXAi...\n------END NATS USER JWT------\n\n-----BEGIN USER NKEY SEED-----\nSUAM...\n------END USER NKEY SEED------"
   ```

5. **Start Keycloak**:
   ```bash
   docker compose up -d
   ```

6. **Log in to Keycloak** at `http://localhost:8080` (admin/admin) to generate events.

## Verifying Event Reception

Use the `nats` CLI to consume messages from the stream:

```bash
nats stream view KEYCLOAK_EVENTS --creds /path/to/your.creds --server tls://connect.ngs.global
```

Or create a consumer and subscribe:

```bash
nats consumer add KEYCLOAK_EVENTS reader --pull --deliver all --creds /path/to/your.creds --server tls://connect.ngs.global
nats consumer next KEYCLOAK_EVENTS reader --count 10 --creds /path/to/your.creds --server tls://connect.ngs.global
```

## Configuration Reference

| Variable | Description | Example |
|---|---|---|
| `NATS_SERVERS` | Synadia Cloud server URL | `tls://connect.ngs.global` |
| `NATS_SUBJECT` | NATS subject for events | `keycloak.events` |
| `NATS_STREAM` | JetStream stream name | `KEYCLOAK_EVENTS` |
| `NATS_CREDENTIALS_FILE_TEXT` | Contents of your `.creds` file (newlines as `\n`, value double-quoted) | See `.env.example` |
