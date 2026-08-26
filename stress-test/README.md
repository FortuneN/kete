# KETE Stress Test

A small harness that puts sustained event load on KETE and watches the delivery rate and the destination pool.

## Overview

`docker-compose.yml` starts Keycloak with KETE (backed by PostgreSQL), a Redis instance, and **20 login-repeater workers**. Each worker logs in once as `admin` and then refreshes its token in a tight loop — every refresh raises a `REFRESH_TOKEN` event, which KETE forwards to the Redis Pub/Sub channel `keycloak-events-stress`. `monitor.ps1` subscribes to that channel and prints the delivery rate together with KETE's pool metrics.

## Components

### Infrastructure (`docker-compose.yml`)

| Service | Image | Notes |
|---------|-------|-------|
| `postgres` | `postgres:16-alpine` | Keycloak database (`keycloak`/`keycloak`) |
| `redis` | `redis:7-alpine` | Destination; not published on the host — reach it with `docker exec` |
| `keycloak` | `ghcr.io/fortunen/kete/quick-start-keycloak` | `start --http-enabled=true --hostname-strict=false`; ports `8080` (Keycloak) and `9000` (management, `/metrics`) |
| `login-repeater-1` … `login-repeater-20` | `curlimages/curl:8.5.0` | Run `login-repeater.sh` |

The KETE route is configured with environment variables on the `keycloak` service:

```yaml
kete.routes.stress-test.destination.kind: redis-pubsub
kete.routes.stress-test.destination.host: redis
kete.routes.stress-test.destination.port: 6379
kete.routes.stress-test.destination.channel: keycloak-events-stress
kete.metrics.enabled: "true"
```

### Login Repeater (`login-repeater.sh`)

1. Waits until `http://keycloak:8080/realms/master` answers, then 10 more seconds.
2. Obtains a token with the password grant (`admin`/`admin`, client `admin-cli`).
3. Loops forever on the `refresh_token` grant, rotating the refresh token each time, and logs its throughput every 200 refreshes:

```
[Worker 7] Completed 2000 refreshes in 41s (48.78 req/s)
```

### Monitor (`monitor.ps1`)

```powershell
.\monitor.ps1 [-StartupWaitSeconds 45] [-CheckIntervalSeconds 60]
```

1. Waits `StartupWaitSeconds`, then polls `http://localhost:9000/metrics` until `kete_routes_active` is `1.0` (up to two minutes).
2. Starts a background `redis-cli SUBSCRIBE keycloak-events-stress` inside the `stress-test-redis-1` container and counts the messages.
3. Every `CheckIntervalSeconds` prints one line — it runs until you stop it with `Ctrl+C`:

```
[14:32:05] Events: 12,400 | Rate: 2,067/sec | Pool: 3 active, 7 idle, 20 total | Forward: 4.21ms
```

The line is red below 1,000 events/s, yellow below 2,000 and green above. `Forward` is `kete_forward_duration_seconds_max` in milliseconds.

## Requirements

- Docker with Docker Compose
- PowerShell 7+ (for `monitor.ps1`)
- The `ghcr.io/fortunen/kete/quick-start-keycloak` image (pulled automatically)

## Usage

```bash
cd stress-test
docker compose up -d
```

```powershell
.\monitor.ps1
# or, for a faster first sample:
.\monitor.ps1 -StartupWaitSeconds 30 -CheckIntervalSeconds 15
```

Watch a worker:

```bash
docker compose logs -f login-repeater-1
```

Stop everything (the `-v` drops the PostgreSQL volume):

```bash
docker compose down -v
```

## Tuning

- **More or fewer workers:** add or remove `login-repeater-N` services in `docker-compose.yml` (they are identical apart from `WORKER_ID`).
- **Heavier events:** replace the refresh loop in `login-repeater.sh` with a password-grant login per iteration (`LOGIN` events cost Keycloak far more than `REFRESH_TOKEN` events).
- **Another destination:** change the `kete.routes.stress-test.destination.*` variables and point the monitor's subscriber at the new sink; the metrics part of the monitor works for any destination.
- **Pool size:** add `kete.routes.stress-test.destination.pool.max-total` (and the other `pool.*` options) to the `keycloak` service — see the [pool guidance](https://fortunen.github.io/kete/user-guide/destinations/overview/) for multiplexing clients such as Redis.

## Metrics

KETE's metrics are exposed on `http://localhost:9000/metrics` (Keycloak's management port, `KC_METRICS_ENABLED=true`): `kete_events_forwarded_total`, `kete_events_failed_total`, `kete_forward_duration_seconds_*`, `kete_pool_active|idle|total`, `kete_events_inflight`, `kete_pool_wait_seconds_*`, `kete_retries_total`, `kete_routes_active`, `kete_routes_failed`.

## Troubleshooting

| Symptom | Check |
|---------|-------|
| `Route active: N/A` | `docker compose logs keycloak` — Keycloak may still be starting (`StartupWaitSeconds`), or the route failed to initialize |
| Workers print `Failed to get initial token` | Keycloak is up but the bootstrap admin is missing; recreate the stack with `docker compose down -v && docker compose up -d` |
| `Events: 0` while workers report refreshes | Subscribe manually: `docker exec stress-test-redis-1 redis-cli SUBSCRIBE keycloak-events-stress`; confirm `kete_events_forwarded_total` grows on `:9000/metrics` |
| Rate stuck far below the workers' request rate | Look at `kete_events_failed_total` and the Keycloak log (`Failed to send …`); PostgreSQL is usually the bottleneck on a laptop |

## Known Limitations

- Everything runs on one machine; the numbers describe that machine, not KETE's ceiling.
- Redis Pub/Sub drops messages when there is no subscriber, so start the monitor before judging the delivery count.
- The monitor counts by subscribing itself; a second subscriber does not change what KETE forwards.
