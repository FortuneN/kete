#!/bin/sh

# Wait for Keycloak to be ready
echo "[Worker $WORKER_ID] Waiting for Keycloak to be ready..."
until curl -sf "$KEYCLOAK_URL/realms/master" > /dev/null 2>&1; do
    echo "[Worker $WORKER_ID] Keycloak not ready yet, waiting..."
    sleep 5
done

echo "[Worker $WORKER_ID] Keycloak is ready! Getting initial token..."
sleep 10

# Get initial token and extract refresh_token
RESPONSE=$(curl -sf \
    -X POST \
    "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
    -d "client_id=admin-cli" \
    -d "username=admin" \
    -d "password=admin" \
    -d "grant_type=password")

REFRESH_TOKEN=$(echo "$RESPONSE" | grep -o '"refresh_token":"[^"]*' | cut -d'"' -f4)

if [ -z "$REFRESH_TOKEN" ]; then
    echo "[Worker $WORKER_ID] Failed to get initial token. Exiting."
    exit 1
fi

echo "[Worker $WORKER_ID] Got token! Starting refresh loop (lighter load = more KETE events)..."

# Counter for logging
count=0
start_time=$(date +%s)

# Infinite loop - continuously refresh token (much lighter than login)
while true; do
    # Refresh token (generates REFRESH_TOKEN event - way lighter than LOGIN)
    RESPONSE=$(curl -sf \
        -X POST \
        "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
        -d "client_id=admin-cli" \
        -d "grant_type=refresh_token" \
        -d "refresh_token=$REFRESH_TOKEN")

    # Update refresh token for next iteration
    NEW_REFRESH_TOKEN=$(echo "$RESPONSE" | grep -o '"refresh_token":"[^"]*' | cut -d'"' -f4)
    if [ -n "$NEW_REFRESH_TOKEN" ]; then
        REFRESH_TOKEN="$NEW_REFRESH_TOKEN"
    fi

    count=$((count + 1))

    # Log progress every 200 requests (more frequent due to higher throughput)
    if [ $((count % 200)) -eq 0 ]; then
        elapsed=$(($(date +%s) - start_time))
        rate=$(awk "BEGIN {printf \"%.2f\", $count / $elapsed}")
        echo "[Worker $WORKER_ID] Completed $count refreshes in ${elapsed}s (${rate} req/s)"
    fi
done
