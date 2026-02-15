# NATS — Synadia Cloud Quick Start

Stream Keycloak events to [Synadia Cloud](https://www.synadia.com/cloud) (NGS) using the NATS protocol.

## Prerequisites

- A Synadia Cloud account
- A NATS credentials file (`.creds`) generated from the Synadia Cloud dashboard or `nsc`
- Docker and Docker Compose

## Setup

1. **Create an account and credentials** in the [Synadia Cloud dashboard](https://cloud.synadia.com/).
   - Create or select an account
   - Generate a user and download the `.creds` file

2. **Copy `.env.example` to `.env`** and fill in your values:
   ```bash
   cp .env.example .env
   ```

3. **Set `NATS_CREDENTIALS_FILE_TEXT`** to the full contents of your `.creds` file (with newlines replaced by `\n`):
   ```
   NATS_CREDENTIALS_FILE_TEXT=-----BEGIN NATS USER JWT-----\neyJ0eXAi...\n------END NATS USER JWT------\n\n-----BEGIN USER NKEY SEED-----\nSUAM...\n------END USER NKEY SEED------
   ```

4. **Start Keycloak**:
   ```bash
   docker compose up -d
   ```

5. **Log in to Keycloak** at `http://localhost:8080` (admin/admin) to generate events.

## Verifying Event Reception

Use the `nats` CLI or the Synadia Cloud dashboard to subscribe to the subject and confirm events arrive:

```bash
nats sub keycloak.events --creds /path/to/your.creds --server tls://connect.ngs.global
```

## Configuration Reference

| Variable | Description | Example |
|---|---|---|
| `NATS_SERVERS` | Synadia Cloud server URL | `tls://connect.ngs.global` |
| `NATS_SUBJECT` | NATS subject for events | `keycloak.events` |
| `NATS_CREDENTIALS_FILE_TEXT` | Contents of your `.creds` file (newlines as `\n`) | See `.env.example` |
