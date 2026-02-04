# MQTT Subscriber - Listens for messages and prints EVENT-RECEIVED marker
FROM eclipse-mosquitto:2

# Copy the subscriber script
COPY subscribe.sh /subscribe.sh
RUN chmod +x /subscribe.sh

ENTRYPOINT ["/subscribe.sh"]
