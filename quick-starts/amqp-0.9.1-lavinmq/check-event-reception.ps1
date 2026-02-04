# Check if event was received via LavinMQ Management API
# Returns $true if message found, $false otherwise

$queue = "keycloak-events"
$url = "http://localhost:15672/api/queues/%2F/$queue"
$cred = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("guest:guest"))

$response = Invoke-RestMethod -Uri $url -Headers @{ Authorization = "Basic $cred" } -TimeoutSec 5
return $response.ready -gt 0 -or $response.unacked -gt 0
