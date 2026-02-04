# Check if event was received via WebSocket Echo server logs
# Returns $true if LOGIN event found in logs, $false otherwise

$logs = docker logs websocket-echo-websocket-echo-1 2>&1
$matched = $logs -match "LOGIN"
return $matched.Count -gt 0
