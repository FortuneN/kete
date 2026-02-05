# Check if event was received by AMQP 1.0 subscriber
# Returns $true if EVENT-RECEIVED marker found in subscriber logs

$logs = docker logs amqp-1-qpid-subscriber-1 2>&1
if ($logs -match "EVENT-RECEIVED") {
    return $true
}
return $false
