# Check if event was received via AWS SNS (LocalStack emulator)
# SNS is push-only, so we read from an SQS verification queue subscribed to the topic
# Returns $true if LOGIN event found, $false otherwise

$result = docker exec aws-sns-emulator-sns-setup-1 curl -sf `
    "http://localstack:4566/000000000000/keycloak-events-verification?Action=ReceiveMessage&WaitTimeSeconds=5" `
    2>&1 | Out-String

return $result -match "LOGIN"
