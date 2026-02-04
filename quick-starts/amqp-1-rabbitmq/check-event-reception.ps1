# Check if event was received by RabbitMQ via Management API
# RabbitMQ exposes a REST API on port 15672

$queueName = "keycloak-events"
$creds = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin"))
$response = Invoke-RestMethod -Uri "http://localhost:15672/api/queues/%2f/$queueName" -Headers @{Authorization = "Basic $creds"} -TimeoutSec 5 -ErrorAction Stop

if (-not $response -or $null -eq $response.messages) {
    throw "RabbitMQ API returned empty or incomplete response"
}

if ($response.messages -gt 0) {
    return $true
}

return $false
