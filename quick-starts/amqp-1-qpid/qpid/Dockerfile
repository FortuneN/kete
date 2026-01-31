FROM eclipse-temurin:21-jre

# Qpid Broker-J version
ARG QPID_VERSION=9.2.0

# Download and extract Qpid Broker-J
RUN apt-get update && apt-get install -y curl && \
    curl -L https://archive.apache.org/dist/qpid/broker-j/${QPID_VERSION}/binaries/apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz -o /tmp/qpid.tar.gz && \
    tar -xzf /tmp/qpid.tar.gz -C /opt && \
    mv /opt/qpid-broker/${QPID_VERSION} /opt/qpid-broker-j && \
    rm -rf /tmp/qpid.tar.gz && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV QPID_HOME=/opt/qpid-broker-j
ENV QPID_WORK=/var/qpid
ENV PATH="${QPID_HOME}/bin:${PATH}"

# Create work directory
RUN mkdir -p ${QPID_WORK}

# Copy initial configuration
COPY initial-config.json ${QPID_HOME}/etc/initial-config.json

# Expose AMQP and HTTP management ports
EXPOSE 5672 8080

# Start Qpid Broker-J with initial config
CMD ["qpid-server", "-icp", "/opt/qpid-broker-j/etc/initial-config.json", "-prop", "qpid.amqp_port=5672", "-prop", "qpid.http_port=8080"]
