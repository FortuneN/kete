# Check if event was received by Artemis via STOMP
# Uses Artemis Jolokia API to check queue message count

$queueName = "keycloak-events"
$creds = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin"))
$response = Invoke-RestMethod -Uri "http://localhost:8161/console/jolokia/read/org.apache.activemq.artemis:broker=`"0.0.0.0`",component=addresses,address=`"$queueName`",subcomponent=queues,routing-type=`"anycast`",queue=`"$queueName`"/MessageCount" -Headers @{Authorization = "Basic $creds"} -TimeoutSec 5 -ErrorAction Stop

if (-not $response.value -and $response.value -ne 0) {
    throw "Artemis API returned empty MessageCount"
}

if ($response.value -gt 0) {
    return $true
}

return $false
