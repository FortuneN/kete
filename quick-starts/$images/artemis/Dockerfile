FROM apache/activemq-artemis:2.39.0-alpine

# Copy custom Jolokia config to allow CORS from any origin
# This enables the check script to access the Jolokia API via curl
# Using etc-override so it's copied after instance creation (per official Artemis Docker docs)
# Need to ensure proper permissions for the artemis user (uid 1000)
USER root
RUN mkdir -p /var/lib/artemis-instance/etc-override
COPY jolokia-access.xml /var/lib/artemis-instance/etc-override/jolokia-access.xml
RUN chown -R 1000:0 /var/lib/artemis-instance
USER 1000
