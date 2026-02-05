# Check if event was received by ActiveMQ Classic via STOMP
# Uses ActiveMQ's Jolokia API to check queue depth

$queueName = "keycloak-events"

$creds = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin"))
$response = Invoke-RestMethod -Uri "http://localhost:8161/api/jolokia/read/org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=$queueName/QueueSize" -Headers @{Authorization = "Basic $creds"; Origin = "http://localhost:8161" } -TimeoutSec 5 -ErrorAction Stop

if ($response.value -gt 0) {
    return $true
}
return $false
