# Check if event was received via Socket.IO echo server events endpoint
# Returns $true if LOGIN event found, $false otherwise

$result = Invoke-RestMethod -Uri "http://localhost:3000/events" -TimeoutSec 5 2>&1 | Out-String
return $result -match "LOGIN"
