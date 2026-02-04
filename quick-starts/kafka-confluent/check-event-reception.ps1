# Check if event was received by Confluent Kafka
# Uses kafka-console-consumer to check for messages

$topic = "keycloak-events"

# Use kafka container to consume messages with timeout
$result = docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic $topic --from-beginning --timeout-ms 5000 2>&1

# Check if we got any JSON content (event data)
if ($result -match '\{.*"type".*\}' -or $result -match '\{.*"id".*\}') {
    return $true
}

# Check if there's any non-empty output (excluding timeout message)
$lines = $result | Where-Object { $_ -and $_ -notmatch "Timeout" -and $_ -notmatch "Processed a total of" }
if ($lines) {
    return $true
}

return $false
