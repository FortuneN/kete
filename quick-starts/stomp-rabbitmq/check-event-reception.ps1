# Check if event was received by RabbitMQ via STOMP
# Uses RabbitMQ Management API to check queue depth

$queueName = "keycloak-events"

$creds = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin"))
$response = Invoke-RestMethod -Uri "http://localhost:15672/api/queues/%2f/$queueName" -Headers @{Authorization = "Basic $creds"} -TimeoutSec 5 -ErrorAction Stop

if ($response.messages -gt 0) {
    return $true
}
return $false
