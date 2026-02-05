# Check if event was received via MQTT subscriber logs
# Returns $true if EVENT-RECEIVED marker found in subscriber logs

$logs = docker logs mqtt-3-hivemq-subscriber-1 2>&1
if ($logs -match "EVENT-RECEIVED") {
    return $true
}
return $false
