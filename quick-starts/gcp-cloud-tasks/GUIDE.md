# GCP Cloud Tasks Quick Start

This quick start demonstrates forwarding Keycloak events to **Google Cloud Tasks**.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- A Google Cloud account with a project
- [Google Cloud CLI](https://cloud.google.com/sdk/docs/install) (`gcloud`)

## GCP Setup

### 1. Authenticate and Set Project

```bash
# Authenticate with Google Cloud
gcloud auth login

# Set your project
gcloud config set project <YOUR-PROJECT-ID>
```

### 2. Enable the Cloud Tasks API

```bash
gcloud services enable cloudtasks.googleapis.com
```

### 3. Create a Queue

```bash
gcloud tasks queues create keycloak-events --location=us-central1
```

### 4. Create a Service Account

```bash
# Create a service account
gcloud iam service-accounts create kete-cloud-tasks \
  --display-name="KETE Cloud Tasks Enqueuer"

# Grant Cloud Tasks Enqueuer role
gcloud projects add-iam-policy-binding <YOUR-PROJECT-ID> \
  --member="serviceAccount:kete-cloud-tasks@<YOUR-PROJECT-ID>.iam.gserviceaccount.com" \
  --role="roles/cloudtasks.enqueuer"

# Generate a JSON key file
gcloud iam service-accounts keys create service-account.json \
  --iam-account=kete-cloud-tasks@<YOUR-PROJECT-ID>.iam.gserviceaccount.com
```

### 5. Base64-Encode the Key (for Docker)

```bash
# Linux/macOS
base64 -w 0 service-account.json

# PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("service-account.json"))
```

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholders:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-PROJECT-ID>` | GCP project ID | `my-keycloak-project` |
| `<YOUR-TARGET-URL>` | URL that Cloud Tasks will POST to | `https://my-service.example.com/handler` |
| `<YOUR-BASE64-ENCODED-SERVICE-ACCOUNT-JSON>` | Base64-encoded service account key | `ewogICJ0eXBlIjog...` |

### Option 2: Use environment variables

Create a `.env` file from the example:

```bash
cp .env.example .env
```

Edit `.env` and fill in your values:

```env
GCP_PROJECT_ID=my-keycloak-project
GCP_LOCATION=us-central1
GCP_TARGET_URL=https://my-service.example.com/handler
GCP_SA_KEY_BASE64=ewogICJ0eXBlIjog...
```

## Running

```bash
docker compose up -d
```

## Testing

1. Open Keycloak at http://localhost:8080
2. Log in with `admin` / `admin`
3. Perform actions (create users, login, etc.)
4. Check your Cloud Tasks queue for received tasks:

```bash
gcloud tasks list --queue=keycloak-events --location=us-central1
```

Or use the [Google Cloud Console](https://console.cloud.google.com/cloudtasks) to view tasks.

## Troubleshooting

### Check Keycloak Logs

```bash
docker compose logs keycloak
```

Look for:
- `kete Route 'quick-start' initialized: destination=...` - Success
- Connection errors - Check credentials and network

### Common Issues

| Issue | Solution |
|-------|----------|
| `401 Unauthorized` | Verify service account JSON is valid and base64-encoded correctly |
| `403 Forbidden` | Ensure service account has `roles/cloudtasks.enqueuer` on the project |
| `404 Not Found` | Check project ID, location, and queue name exist |
| Connection timeout | Verify outbound HTTPS (port 443) is not blocked |

## Cleanup

```bash
docker compose down

# Remove GCP resources (optional)
gcloud tasks queues delete keycloak-events --location=us-central1
gcloud iam service-accounts delete kete-cloud-tasks@<YOUR-PROJECT-ID>.iam.gserviceaccount.com
```
