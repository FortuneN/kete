#!/bin/sh
# MQTT Subscriber Script
# Subscribes to a topic and prints EVENT-RECEIVED when a message arrives

BROKER_HOST="${MQTT_BROKER_HOST:-mqtt}"
BROKER_PORT="${MQTT_BROKER_PORT:-1883}"
TOPIC="${MQTT_TOPIC:-keycloak-events}"
CLIENT_ID="${MQTT_CLIENT_ID:-kete-test-subscriber}"
USERNAME="${MQTT_USERNAME:-}"
PASSWORD="${MQTT_PASSWORD:-}"

echo "=================================================="
echo "MQTT Subscriber Starting"
echo "  Broker: $BROKER_HOST:$BROKER_PORT"
echo "  Topic: $TOPIC"
echo "  Client ID: $CLIENT_ID"
echo "=================================================="

# Wait for broker to be available
sleep 2

# Build auth flags if credentials are provided
AUTH_FLAGS=""
if [ -n "$USERNAME" ]; then
    AUTH_FLAGS="-u $USERNAME"
    if [ -n "$PASSWORD" ]; then
        AUTH_FLAGS="$AUTH_FLAGS -P $PASSWORD"
    fi
fi

# Subscribe and print marker on each message
mosquitto_sub -h "$BROKER_HOST" -p "$BROKER_PORT" -t "$TOPIC" -i "$CLIENT_ID" $AUTH_FLAGS -v | while read -r line; do
    echo "=================================================="
    echo "MESSAGE RECEIVED:"
    echo "$line"
    echo "=================================================="
    echo "EVENT-RECEIVED"
done
