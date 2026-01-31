# Kubernetes Deployment

Deploy KETE in Kubernetes with proper configuration management, secrets, and TLS certificates.



## Overview

This guide covers deploying the extension in Kubernetes with:
- ConfigMaps for non-sensitive configuration
- Secrets for passwords and certificates
- Volume mounts for keystores/truststores
- Multiple destination examples



## Basic Deployment

### 1. Download the Extension

Download the latest JAR from [GitHub Releases](https://github.com/FortuneN/kete/releases):

=== "Linux/macOS"

    ```bash
    curl -L -o kete.jar https://github.com/FortuneN/kete/releases/latest/download/kete.jar
    ```

=== "Windows (PowerShell)"

    ```powershell
    Invoke-WebRequest -Uri https://github.com/FortuneN/kete/releases/latest/download/kete.jar -OutFile kete.jar
    ```

### 2. Create JAR as ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: keycloak-extensions
  namespace: keycloak
data:
  # Or mount from external source
binaryData:
  kete.jar: <base64-encoded-jar>  # Linux: base64 -w0 kete.jar | PowerShell: [Convert]::ToBase64String([IO.File]::ReadAllBytes('kete.jar'))
```

### 3. Keycloak Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: keycloak
  namespace: keycloak
spec:
  replicas: 2
  selector:
    matchLabels:
      app: keycloak
  template:
    metadata:
      labels:
        app: keycloak
    spec:
      containers:
      - name: keycloak
        image: quay.io/keycloak/keycloak:26.5.0
        command: ["/opt/keycloak/bin/kc.sh"]
        args: ["start", "--optimized"]
        envFrom:
        - configMapRef:
            name: kete-config
        - secretRef:
            name: kete-secrets
        ports:
        - name: http
          containerPort: 8080
        - name: https
          containerPort: 8443
        volumeMounts:
        - name: extensions
          mountPath: /opt/keycloak/providers
          readOnly: true
        - name: certs
          mountPath: /opt/keycloak/certs
          readOnly: true
      volumes:
      - name: extensions
        emptyDir: {}
      - name: certs
        secret:
          secretName: rabbitmq-tls-certs
      initContainers:
      - name: copy-extensions
        image: busybox
        command: ['sh', '-c', 'cp /extensions/* /opt/keycloak/providers/']
        volumeMounts:
        - name: extensions-source
          mountPath: /extensions
        - name: extensions
          mountPath: /opt/keycloak/providers
```



## Configuration with Kafka

### ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: kete-config
  namespace: keycloak
data:
  # Kafka destination - login events
  kete.routes.kafka-logins.realm-matchers.realm: "list:master"
  kete.routes.kafka-logins.event-matchers.events: "regex:^LOGIN"
  kete.routes.kafka-logins.serializer.kind: "json"
  kete.routes.kafka-logins.destination.kind: "kafka"
  kete.routes.kafka-logins.destination.bootstrap.servers: "kafka-cluster.kafka.svc.cluster.local:9092"
  kete.routes.kafka-logins.destination.topic: "keycloak-logins"
  kete.routes.kafka-logins.destination.acks: "all"
  kete.routes.kafka-logins.destination.compression.type: "snappy"
  kete.routes.kafka-logins.destination.retries: "3"
  
  # Kafka destination - admin events
  kete.routes.kafka-admin.realm-matchers.realm: "list:master"
  kete.routes.kafka-admin.event-matchers.events: "regex:^(CREATE|UPDATE|DELETE):"
  kete.routes.kafka-admin.serializer.kind: "json"
  kete.routes.kafka-admin.destination.kind: "kafka"
  kete.routes.kafka-admin.destination.bootstrap.servers: "kafka-cluster.kafka.svc.cluster.local:9092"
  kete.routes.kafka-admin.destination.topic: "keycloak-admin"
```

### Secret (for Kafka SASL)

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: kete-secrets
  namespace: keycloak
type: Opaque
stringData:
  kete.routes.kafka-logins.destination.sasl.jaas.config: |
    org.apache.kafka.common.security.plain.PlainLoginModule required 
    username="keycloak" 
    password="kafka-secret-password";
  kete.routes.kafka-admin.destination.sasl.jaas.config: |
    org.apache.kafka.common.security.plain.PlainLoginModule required 
    username="keycloak" 
    password="kafka-secret-password";
```



## Configuration with RabbitMQ

### ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: kete-config
  namespace: keycloak
data:
  # RabbitMQ destination
  kete.routes.rabbitmq-events.realm-matchers.realm: "list:master"
  kete.routes.rabbitmq-events.event-matchers.events: "glob:*"
  kete.routes.rabbitmq-events.serializer.kind: "json"
  kete.routes.rabbitmq-events.destination.kind: "amqp-0.9.1"
  kete.routes.rabbitmq-events.destination.host: "rabbitmq.messaging.svc.cluster.local"
  kete.routes.rabbitmq-events.destination.port: "5672"
  kete.routes.rabbitmq-events.destination.virtual-host: "/keycloak"
  kete.routes.rabbitmq-events.destination.exchange: "keycloak-events"
  kete.routes.rabbitmq-events.destination.routing-key: "events"
```

### Secret

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: kete-secrets
  namespace: keycloak
type: Opaque
stringData:
  kete.routes.rabbitmq-events.destination.username: "keycloak"
  kete.routes.rabbitmq-events.destination.password: "rabbitmq-secret-password"
```



## RabbitMQ with TLS/SSL

### Create TLS Certificate Secret

=== "Linux/macOS"

    ```bash
    # Create secret from certificate files
    kubectl create secret generic rabbitmq-tls-certs \
      --from-file=client.p12=/path/to/client.p12 \
      --from-file=truststore.jks=/path/to/truststore.jks \
      --namespace=keycloak
    ```

=== "Windows (PowerShell)"

    ```powershell
    # Create secret from certificate files
    kubectl create secret generic rabbitmq-tls-certs `
      --from-file=client.p12=/path/to/client.p12 `
      --from-file=truststore.jks=/path/to/truststore.jks `
      --namespace=keycloak
    ```

### ConfigMap with TLS

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: kete-config
  namespace: keycloak
data:
  # RabbitMQ with TLS
  kete.routes.rabbitmq-secure.realm-matchers.realm: "list:master"
  kete.routes.rabbitmq-secure.event-matchers.events: "glob:*"
  kete.routes.rabbitmq-secure.serializer.kind: "json"
  kete.routes.rabbitmq-secure.destination.kind: "amqp-0.9.1"
  kete.routes.rabbitmq-secure.destination.host: "rabbitmq-secure.messaging.svc.cluster.local"
  kete.routes.rabbitmq-secure.destination.port: "5671"
  kete.routes.rabbitmq-secure.destination.exchange: "keycloak-events"
  kete.routes.rabbitmq-secure.destination.tls.enabled: "true"
  kete.routes.rabbitmq-secure.destination.tls.key-store.loader.kind: "pkcs12-file-path"
  kete.routes.rabbitmq-secure.destination.tls.key-store.loader.file: "/opt/keycloak/certs/client.p12"
  kete.routes.rabbitmq-secure.destination.tls.trust-store.loader.kind: "jks-file-path"
  kete.routes.rabbitmq-secure.destination.tls.trust-store.loader.file: "/opt/keycloak/certs/truststore.jks"
```

### Secret with Passwords

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: kete-secrets
  namespace: keycloak
type: Opaque
stringData:
  kete.routes.rabbitmq-secure.destination.username: "keycloak"
  kete.routes.rabbitmq-secure.destination.password: "rabbitmq-password"
  kete.routes.rabbitmq-secure.destination.tls.key-store.password: "keystore-password"
  kete.routes.rabbitmq-secure.destination.tls.trust-store.password: "truststore-password"
```



## Configuration with HTTP Webhook

### ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: kete-config
  namespace: keycloak
data:
  # HTTP webhook destination
  kete.routes.webhook.realm-matchers.realm: "list:master"
  kete.routes.webhook.event-matchers.events: "regex:^LOG(IN|OUT)"
  kete.routes.webhook.serializer.kind: "json"
  kete.routes.webhook.destination.kind: "http"
  kete.routes.webhook.destination.url: "https://webhook.example.com/keycloak-events"
  kete.routes.webhook.destination.method: "POST"
  kete.routes.webhook.destination.timeout-seconds: "10"
  kete.routes.webhook.retry.enabled: "true"
  kete.routes.webhook.retry.max-attempts: "3"
  kete.routes.webhook.retry.wait-duration: "PT2S"
  kete.routes.webhook.destination.headers.Content-Type: "application/json"
  kete.routes.webhook.destination.headers.X-Service: "keycloak"
```

### Secret (for Bearer Token)

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: kete-secrets
  namespace: keycloak
type: Opaque
stringData:
  kete.routes.webhook.destination.headers.Authorization: "Bearer sk-1234567890abcdef"
  kete.routes.webhook.destination.headers.X-Service: "keycloak"
```



## Multi-Destination Setup

Complete example with Kafka, RabbitMQ, and HTTP:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: kete-config
  namespace: keycloak
data:
  # Kafka for all events
  kete.routes.kafka-all.realm-matchers.realm: "list:master"
  kete.routes.kafka-all.event-matchers.events: "glob:*"
  kete.routes.kafka-all.serializer.kind: "json"
  kete.routes.kafka-all.destination.kind: "kafka"
  kete.routes.kafka-all.destination.bootstrap.servers: "kafka:9092"
  kete.routes.kafka-all.destination.topic: "keycloak-all-events"
  
  # RabbitMQ for failed logins
  kete.routes.rabbitmq-failures.realm-matchers.realm: "list:master"
  kete.routes.rabbitmq-failures.event-matchers.events: "glob:LOGIN_ERROR"
  kete.routes.rabbitmq-failures.serializer.kind: "json"
  kete.routes.rabbitmq-failures.destination.kind: "amqp-0.9.1"
  kete.routes.rabbitmq-failures.destination.host: "rabbitmq"
  kete.routes.rabbitmq-failures.destination.exchange: "security-alerts"
  kete.routes.rabbitmq-failures.destination.routing-key: "login.failed"
  kete.routes.rabbitmq-failures.destination.priority: "9"
  
  # HTTP webhook for admin actions
  kete.routes.webhook-admin.realm-matchers.realm: "list:master"
  kete.routes.webhook-admin.event-matchers.events: "regex:^(CREATE|UPDATE|DELETE):"
  kete.routes.webhook-admin.serializer.kind: "json"
  kete.routes.webhook-admin.destination.kind: "http"
  kete.routes.webhook-admin.destination.url: "https://audit.example.com/keycloak"
  kete.routes.webhook-admin.destination.method: "POST"
```
