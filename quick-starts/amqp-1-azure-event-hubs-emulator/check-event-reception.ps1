# Check if event was received by Kafka subscriber
# (Event Hubs emulator uses Kafka protocol for reception verification)
# Returns $true if EVENT-RECEIVED marker found in subscriber logs

$logs = docker logs amqp-1-azure-event-hubs-emulator-subscriber-1 2>&1

if ($logs -match "EVENT-RECEIVED") {
    return $true
}

return $false
