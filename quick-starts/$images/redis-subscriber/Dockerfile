# Redis Pub/Sub Subscriber - Listens for messages and prints EVENT-RECEIVED marker
FROM redis:7-alpine

# Copy the subscriber script
COPY subscribe.sh /subscribe.sh
RUN chmod +x /subscribe.sh

ENTRYPOINT ["/subscribe.sh"]
