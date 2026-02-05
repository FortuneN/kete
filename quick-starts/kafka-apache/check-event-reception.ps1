# Check if event was received via Kafka console consumer
# Returns $true if message found, $false otherwise

# Give Kafka a moment to fully commit the message
Start-Sleep -Milliseconds 500

# Use kafka-console-consumer to check for messages
$result = docker exec kafka-apache-kafka-1 /opt/kafka/bin/kafka-console-consumer.sh `
    --bootstrap-server localhost:9092 `
    --topic keycloak-events `
    --from-beginning `
    --max-messages 1 `
    --timeout-ms 5000 2>&1 | Out-String

return $result -match "LOGIN"
