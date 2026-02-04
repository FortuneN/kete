#!/bin/sh
# NATS Subscriber Script
# Subscribes to a subject and prints EVENT-RECEIVED when a message arrives

NATS_URL="${NATS_URL:-nats://nats:4222}"
SUBJECT="${NATS_SUBJECT:-keycloak-events}"

echo "=================================================="
echo "NATS Subscriber Starting"
echo "  URL: $NATS_URL"
echo "  Subject: $SUBJECT"
echo "=================================================="

# Wait for NATS to be available
sleep 2

# Subscribe and output messages with marker
# Using nats sub with --raw to get just the message
nats sub "$SUBJECT" --server "$NATS_URL" 2>&1 | while read -r line; do
    echo "$line"
    # Check if this looks like an event message (contains JSON-like content)
    case "$line" in
        *"{"*"}"*)
            echo "=================================================="
            echo "EVENT-RECEIVED"
            ;;
    esac
done
