# Check if event was received via AWS SQS (LocalStack emulator)
# Uses curl in the sqs-setup container to receive messages from the queue
# Returns $true if LOGIN event found, $false otherwise

$result = docker exec aws-sqs-emulator-sqs-setup-1 curl -sf `
    "http://localstack:4566/000000000000/keycloak-events?Action=ReceiveMessage&WaitTimeSeconds=5" `
    2>&1 | Out-String

return $result -match "LOGIN"
