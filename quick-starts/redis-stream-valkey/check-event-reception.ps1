# Check if event was received via Valkey XRANGE (streams)
# Returns $true if message found, $false otherwise

$result = docker exec redis-stream-valkey-valkey-1 valkey-cli XRANGE keycloak-events - + COUNT 1 2>&1
$matched = $result -match "LOGIN"
return $matched.Count -gt 0
