# Check if event was received via ZeroMQ subscriber logs
# Returns $true if EVENT-RECEIVED marker found in subscriber logs

$logs = docker logs zeromq-push-subscriber-1 2>&1

if ($logs -match "EVENT-RECEIVED") {
    return $true
}

return $false
