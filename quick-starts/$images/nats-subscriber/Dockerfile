# NATS Subscriber - Listens for messages and prints EVENT-RECEIVED marker
FROM natsio/nats-box:latest

# Copy the subscriber script
COPY subscribe.sh /subscribe.sh

ENTRYPOINT ["/bin/sh", "/subscribe.sh"]
