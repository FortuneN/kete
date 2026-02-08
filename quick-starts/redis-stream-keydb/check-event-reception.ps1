# Check if event was received via KeyDB XRANGE (streams)
# Returns $true if message found, $false otherwise

$result = docker exec redis-stream-keydb-keydb-1 keydb-cli XRANGE keycloak-events - + COUNT 1 2>&1
$matched = $result -match "LOGIN"
return $matched.Count -gt 0
