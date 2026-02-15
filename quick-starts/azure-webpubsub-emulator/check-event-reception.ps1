# Check if event was received via Azure Web PubSub (nginx as mock endpoint)
# Nginx logs POST request bodies which contain the event JSON
# Returns $true if LOGIN event found in nginx access logs, $false otherwise

$logs = docker logs azure-webpubsub-emulator-webhook-1 2>&1
$matched = $logs | Where-Object { $_ -match "LOGIN" }
return ($null -ne $matched -and @($matched).Count -gt 0)
