# run-quick-starts.ps1

Tests quick-start Docker Compose stacks by starting containers, triggering a Keycloak login event, and verifying event delivery through metrics and per-quickstart reception checks.

## Usage

```powershell
# Test all quick-starts
.\run-quick-starts.ps1

# Test only quick-starts matching a pattern
.\run-quick-starts.ps1 -Filter "kafka-*"

# Custom timeout for event reception check
.\run-quick-starts.ps1 -TimeoutSeconds 120
```

## Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `-Filter` | `*` | Wildcard filter for quickstart folder names |
| `-TimeoutSeconds` | `60` | Timeout in seconds for event reception verification |

## What It Does (Per Quick-Start)

1. **Start containers** — `docker compose up -d`
2. **Wait for healthy** — polls `docker compose ps` until all services report healthy (60s timeout)
3. **Wait for Keycloak** — polls `http://localhost:8080/realms/master` until 200 (180s timeout)
4. **Trigger login** — sends OAuth `password` grant to `admin-cli` as `admin/admin`
5. **Verify sent** — checks `http://localhost:9000/metrics` for `kete_events_forwarded_total` increase (15s timeout with 2s polling)
6. **Verify received** — if a `check-event-reception.ps1` script exists in the quickstart folder, runs it in a retry loop until it returns `$true` or timeout
7. **Cleanup** — `docker compose down -v --remove-orphans`

## Quickstart Selection

The script discovers quickstarts from `quick-starts/` by:

- Listing all subdirectories except `$images`
- Excluding folders containing a `dont-run-this-quickstart` marker file
- Applying the `-Filter` wildcard pattern
- Sorting alphabetically

## Reception Check Scripts

Each quickstart can include a `check-event-reception.ps1` that returns:

- `$true` — event was received
- `$false` — not yet received (script will retry)
- `$null` — skip reception check (used for pub/sub or cloud-only destinations where reading back is not possible)

## Output

The script displays a branded KETE banner, per-quickstart test progress with spinner animations, and a final summary:

```
  Passed:  54
  Failed:  0
  Skipped: 0
  Total Duration: 45:12
```

## Exit Code

- `0` — all tested quick-starts passed
- `1` — at least one quick-start failed (execution stops on first failure)

## Prerequisites

- Docker Desktop
- PowerShell 5.1 or later
- Ports 8080 and 9000 available (Keycloak)
- Quick-start Docker images pre-built (see `quick-starts/$images/`)
