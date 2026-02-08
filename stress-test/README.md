# KETE Stress Test

A comprehensive stress testing suite for KETE (Keycloak Events To Everywhere) that validates performance under sustained load.

## Overview

This stress test simulates real-world high-traffic scenarios by:
- Running **5 concurrent login repeaters** that continuously authenticate against Keycloak
- Streaming all authentication events through **KETE to Redis Stream**
- Monitoring **throughput and metrics every minute** for 10 minutes
- Calculating **messages per second** and tracking system health

## Components

### Infrastructure (`docker-compose.yml`)
- **Keycloak**: Event source with KETE provider enabled (metrics enabled)
- **Redis**: Destination system (Redis Stream)
- **5x Login Repeaters**: Concurrent workers generating authentication events in tight loops

### Login Repeater (`login-repeater.sh`)
- Continuously authenticates against Keycloak using `admin-cli`
- Logs progress every 100 requests
- Runs without sleep for maximum stress (configurable)

### Monitor (`monitor.ps1`)
- Runs for 10 minutes (configurable)
- Checks metrics every 60 seconds (configurable)
- Tracks:
  - Redis Stream message count
  - Messages processed since last check
  - Current throughput (msg/s)
  - Average throughput (msg/s)
  - KETE routes initialized
  - KETE events sent total
  - KETE events failed total
- Generates CSV report with detailed measurements
- Provides final summary with performance analysis

## Requirements

- Docker & Docker Compose
- PowerShell 7+ (for monitoring script)
- 4GB+ RAM recommended
- CPU: 4+ cores recommended

## Usage

### 1. Start the Stress Test Infrastructure

```bash
cd stress-test
docker compose up -d
```

This will start:
- Redis (port 6379)
- Keycloak with KETE (port 8080, metrics on 9000)
- 5 login repeater workers

### 2. Run the Monitor

**Default (10 minutes, check every 60 seconds):**
```powershell
.\monitor.ps1
```

**Custom duration and interval:**
```powershell
# Run for 5 minutes, check every 30 seconds
.\monitor.ps1 -DurationMinutes 5 -IntervalSeconds 30
```

**Custom parameters:**
```powershell
.\monitor.ps1 `
    -DurationMinutes 10 `
    -IntervalSeconds 60 `
    -RedisContainer "stress-test-redis-1" `
    -KeycloakUrl "http://localhost:8080" `
    -StreamName "keycloak-events-stress"
```

### 3. Watch Worker Progress (Optional)

Monitor individual worker output:
```bash
docker compose logs -f login-repeater-1
docker compose logs -f login-repeater-2
# ... etc
```

### 4. Stop the Test

```bash
docker compose down
```

## Output

### Console Output

The monitor displays real-time updates every minute:

```
═══════════════════════════════════════════════════════════════════════
  KETE Stress Test Monitor
═══════════════════════════════════════════════════════════════════════
Duration       : 10 minutes
Check Interval : 60 seconds
Redis Stream   : keycloak-events-stress
Start Time     : 2026-02-01 18:30:00

───────────────────────────────────────────────────────────────────────
Check #1 - 18:31:00 - Elapsed: 1.0m / Remaining: 9.0m

  Redis Stream Messages     : 15234
  Messages Since Last Check : 15234
  Current Throughput        : 254.23 msg/s
  Average Throughput        : 254.23 msg/s

  KETE Routes Initialized   : 1
  KETE Events Sent Total    : 15234
  KETE Events Failed Total  : 0
```

### Final Summary

After completion, the monitor provides a comprehensive summary:

```
═══════════════════════════════════════════════════════════════════════
  STRESS TEST COMPLETE
═══════════════════════════════════════════════════════════════════════

Test Duration            : 10.02 minutes
Total Messages Processed : 152340

Overall Throughput       : 253.90 msg/s
Peak Throughput          : 312.45 msg/s
Average Throughput       : 254.67 msg/s

KETE Routes Initialized  : 1
KETE Events Sent         : 152340
KETE Events Failed       : 0

Detailed results saved to: stress-test-results-20260201-183010.csv

✓ SUCCESS: No failed events detected!
✓ PERFORMANCE: System processed 253.90 messages per second
```

### CSV Report

Detailed measurements are saved to a timestamped CSV file:

```csv
Timestamp,TotalMessages,MessagesSinceLastCheck,CurrentThroughput,AverageThroughput,RoutesInitialized,EventsSent,EventsFailed
2026-02-01 18:31:00,15234,15234,254.23,254.23,1,15234,0
2026-02-01 18:32:00,30567,15333,255.55,254.89,1,30567,0
...
```

## Tuning

### Increase Load

To increase stress, edit `login-repeater.sh` and comment out the sleep:

```bash
# Tiny sleep to prevent overwhelming the system (adjust as needed)
# Comment out for maximum stress
# sleep 0.01   <-- Comment this line
```

### Scale Workers

Add more repeaters in `docker-compose.yml`:

```yaml
  login-repeater-6:
    image: curlimages/curl:8.5.0
    depends_on:
      - keycloak
    volumes:
      - ./login-repeater.sh:/login-repeater.sh:ro
    entrypoint: ["/bin/sh", "/login-repeater.sh"]
    environment:
      KEYCLOAK_URL: http://keycloak:8080
      WORKER_ID: "6"
```

### Different Destination

To test with a different destination, modify `docker-compose.yml` Keycloak environment:

**NATS Core:**
```yaml
kete.routes.stress-test.destination.kind: nats
kete.routes.stress-test.destination.servers: nats://nats:4222
kete.routes.stress-test.destination.subject: keycloak.events
```

**Kafka:**
```yaml
kete.routes.stress-test.destination.kind: kafka
kete.routes.stress-test.destination.bootstrap.servers: kafka:9092
kete.routes.stress-test.destination.topic: keycloak-events
```

## Metrics Endpoints

- **Keycloak Health**: http://localhost:8080/health/ready
- **Keycloak Metrics**: http://localhost:8080/metrics
- **Keycloak Admin**: http://localhost:8080 (admin/admin)

## Troubleshooting

### Workers Not Starting

Check if Keycloak is ready:
```bash
docker logs stress-test-keycloak-1
```

### No Messages in Redis

Verify KETE route initialization:
```bash
docker logs stress-test-keycloak-1 | grep "kete Route"
```

Expected output:
```
kete Route 'stress-test' initialized: destination=RedisStreamDestinationConfig
```

### Check Redis Stream Manually

```bash
docker exec stress-test-redis-1 redis-cli XLEN keycloak-events-stress
docker exec stress-test-redis-1 redis-cli XREAD COUNT 1 STREAMS keycloak-events-stress 0
```

### High CPU/Memory Usage

This is expected under stress. Monitor with:
```bash
docker stats
```

To reduce load, add sleep to `login-repeater.sh` or reduce worker count.

## Performance Expectations

Typical throughput (on modern hardware):

| Workers | Expected Throughput |
|---------|-------------------|
| 1       | 50-100 msg/s      |
| 5       | 200-400 msg/s     |
| 10      | 400-800 msg/s     |

Actual performance depends on:
- Hardware (CPU, RAM, disk I/O)
- Network latency
- Keycloak configuration
- Destination system performance

## Cleanup

Remove all containers and networks:
```bash
docker compose down -v
```

Remove generated CSV files:
```bash
rm stress-test-results-*.csv
```

## Known Limitations

- Workers may see connection errors during Keycloak startup (expected, they retry)
- Very high loads (>1000 msg/s) may require Keycloak tuning
- Redis Stream is used for easy verification; production systems may use Kafka/NATS
- Test runs entirely on localhost; network latency not simulated

## License

This stress test is part of the KETE project and follows the same license.
