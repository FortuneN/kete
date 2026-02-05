#!/usr/bin/env python3
"""Kafka subscriber for event reception verification."""
import os
import sys
import time
from kafka import KafkaConsumer

def main():
    bootstrap_servers = os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'localhost:9092')
    topic = os.getenv('KAFKA_TOPIC', 'keycloak-events')
    group_id = os.getenv('KAFKA_GROUP_ID', 'event-checker')
    security_protocol = os.getenv('KAFKA_SECURITY_PROTOCOL', 'PLAINTEXT')
    sasl_mechanism = os.getenv('KAFKA_SASL_MECHANISM', '')
    sasl_username = os.getenv('KAFKA_SASL_USERNAME', '')
    sasl_password = os.getenv('KAFKA_SASL_PASSWORD', '')

    print(f"Connecting to {bootstrap_servers}, subscribing to {topic}", flush=True)

    while True:
        try:
            # Build consumer config
            config = {
                'bootstrap_servers': bootstrap_servers.split(','),
                'group_id': group_id,
                'auto_offset_reset': 'earliest',
                'enable_auto_commit': True,
                'security_protocol': security_protocol,
            }

            # Add SASL config if needed
            if sasl_mechanism:
                config['sasl_mechanism'] = sasl_mechanism
                config['sasl_plain_username'] = sasl_username
                config['sasl_plain_password'] = sasl_password

            consumer = KafkaConsumer(topic, **config)
            print(f"Subscribed to {topic}", flush=True)

            for message in consumer:
                print("EVENT-RECEIVED", flush=True)

        except KeyboardInterrupt:
            break
        except Exception as e:
            print(f"Error: {e}", file=sys.stderr, flush=True)
            time.sleep(5)

if __name__ == '__main__':
    main()
