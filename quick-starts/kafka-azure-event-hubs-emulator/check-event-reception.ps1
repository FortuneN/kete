# Check if event was received by Kafka subscriber
# Returns $true if EVENT-RECEIVED marker found in subscriber logs

$logs = docker logs kafka-azure-event-hubs-emulator-subscriber-1 2>&1

if ($logs -match "EVENT-RECEIVED") {
    return $true
}

return $false
