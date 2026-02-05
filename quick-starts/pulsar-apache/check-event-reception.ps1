# Check if event was received by Apache Pulsar
# Uses pulsar-admin to check topic stats

$tenant = "public"
$namespace = "default"
$topic = "keycloak-events"

# Use pulsar container to check topic stats
$result = docker exec pulsar-apache-pulsar-1 bin/pulsar-admin topics stats "persistent://$tenant/$namespace/$topic" 2>&1

# Convert array to single string for regex matching
$output = $result -join "`n"

# Check for message count in stats
if ($output -match '"msgInCounter"\s*:\s*(\d+)' -and [int]$Matches[1] -gt 0) {
    return $true
}

# Alternative: check storageSize
if ($output -match '"storageSize"\s*:\s*(\d+)' -and [int]$Matches[1] -gt 0) {
    return $true
}

return $false
