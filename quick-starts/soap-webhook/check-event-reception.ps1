# Check if event was received via HTTP Echo server logs (SOAP sends HTTP POST)
# Returns $true if LOGIN event found in logs, $false otherwise

$logs = docker logs soap-webhook-webhook-1 2>&1
$matched = $logs -match "LOGIN"
return $matched.Count -gt 0
