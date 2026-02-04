#!/bin/sh
# Redis Pub/Sub Subscriber Script
# Subscribes to a channel and prints EVENT-RECEIVED when a message arrives

REDIS_HOST="${REDIS_HOST:-redis}"
REDIS_PORT="${REDIS_PORT:-6379}"
CHANNEL="${REDIS_CHANNEL:-keycloak-events}"

echo "=================================================="
echo "Redis Pub/Sub Subscriber Starting"
echo "  Redis: $REDIS_HOST:$REDIS_PORT"
echo "  Channel: $CHANNEL"
echo "=================================================="

# Wait for Redis to be available
sleep 2

# Subscribe and print marker on each message
# Redis SUBSCRIBE outputs: subscribe, channel, count, then message, channel, data
redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" SUBSCRIBE "$CHANNEL" | while read -r line; do
    case "$line" in
        *"message"*)
            # Next lines will be channel and data
            read -r channel
            read -r data
            echo "=================================================="
            echo "MESSAGE RECEIVED on channel: $channel"
            echo "$data"
            echo "=================================================="
            echo "EVENT-RECEIVED"
            ;;
    esac
done
