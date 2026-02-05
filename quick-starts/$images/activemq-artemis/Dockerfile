FROM apache/activemq-artemis:2.37.0

# Copy custom broker.xml with STOMP anycast/multicast prefix configuration
# The STOMP acceptor needs anycastPrefix=/queue/;multicastPrefix=/topic/ for proper routing
# Using etc-override so it's copied after instance creation (per official Artemis Docker docs)
USER root
RUN mkdir -p /var/lib/artemis-instance/etc-override
COPY broker.xml /var/lib/artemis-instance/etc-override/broker.xml
RUN chown -R 1000:0 /var/lib/artemis-instance
USER 1000
