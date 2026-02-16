# Check if event was received via AWS EventBridge (LocalStack emulator)
# EventBridge routes events to an SQS verification queue, so we read from SQS
# Returns $true if LOGIN event found, $false otherwise

$result = docker exec aws-eventbridge-emulator-eventbridge-setup-1 curl -sf `
    "http://localstack:4566/000000000000/keycloak-events-verification?Action=ReceiveMessage&WaitTimeSeconds=5" `
    2>&1 | Out-String

return $result -match "LOGIN"
