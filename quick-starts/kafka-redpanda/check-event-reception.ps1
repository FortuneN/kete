# Check if event was received via Redpanda
# Returns $true if message found, $false otherwise

$result = docker exec kafka-redpanda-redpanda-1 rpk topic consume keycloak-events --num 1 --fetch-max-wait 5s 2>&1 | Out-String
return $result -match "LOGIN"
