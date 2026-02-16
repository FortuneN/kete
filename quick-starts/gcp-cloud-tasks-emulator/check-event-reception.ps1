# Check if event was received via GCP Cloud Tasks (emulator dispatches to webhook)
# The emulator dispatches HTTP POST requests to the webhook, which logs them
# Returns $true if LOGIN event found in logs, $false otherwise

$logs = docker logs gcp-cloud-tasks-emulator-webhook-1 2>&1
$matched = $logs -match "LOGIN"
return $matched.Count -gt 0
