# Check if event was received by NATS JetStream
# JetStream persists messages - we can check stream contents

$streamName = "KEYCLOAK_EVENTS"
$containerName = "nats-jetstream-nats-server-nats-setup-1"

# Use nats-setup container to check stream
$result = docker exec $containerName nats stream info $streamName --server nats://nats:4222 2>&1 | Out-String

# Look for "Messages: N" where N > 0
if ($result -match '(?m)^\s*Messages:\s+(\d+)') {
    $messageCount = [int]$Matches[1]
    if ($messageCount -gt 0) {
        return $true
    }
}

return $false
