# Check if event was received via Dragonfly XRANGE (streams)
# Returns $true if message found, $false otherwise

$result = docker exec redis-stream-dragonfly-dragonfly-1 redis-cli XRANGE keycloak-events - + COUNT 1 2>&1
$matched = $result -match "LOGIN"
return $matched.Count -gt 0
