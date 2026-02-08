# Check if event was received via GCP Pub/Sub emulator subscription pull
# Returns $true if LOGIN event found in pulled messages, $false otherwise

$result = docker exec gcp-pubsub-emulator-pubsub-setup-1 curl -sf `
    -X POST http://pubsub-emulator:8085/v1/projects/demo-project/subscriptions/keycloak-events-sub:pull `
    -H "Content-Type: application/json" `
    -d "{`"maxMessages`":10}" 2>&1 | Out-String

return $result -match "LOGIN"
