#!/bin/sh
# MQTT Subscriber Script
# Subscribes to a topic and prints EVENT-RECEIVED when a message arrives

BROKER_HOST="${MQTT_BROKER_HOST:-mqtt}"
BROKER_PORT="${MQTT_BROKER_PORT:-1883}"
TOPIC="${MQTT_TOPIC:-keycloak-events}"
CLIENT_ID="${MQTT_CLIENT_ID:-kete-test-subscriber}"

echo "=================================================="
echo "MQTT Subscriber Starting"
echo "  Broker: $BROKER_HOST:$BROKER_PORT"
echo "  Topic: $TOPIC"
echo "  Client ID: $CLIENT_ID"
echo "=================================================="

# Wait for broker to be available
sleep 2

# Subscribe and print marker on each message
mosquitto_sub -h "$BROKER_HOST" -p "$BROKER_PORT" -t "$TOPIC" -i "$CLIENT_ID" -v | while read -r line; do
    echo "=================================================="
    echo "MESSAGE RECEIVED:"
    echo "$line"
    echo "=================================================="
    echo "EVENT-RECEIVED"
done
