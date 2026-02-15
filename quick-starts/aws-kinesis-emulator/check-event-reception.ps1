# Check if event was received via AWS Kinesis (LocalStack emulator)
# Uses Kinesis GetShardIterator + GetRecords API to read from the stream
# Returns $true if LOGIN event found, $false otherwise

# Get shard iterator
$shardIterResponse = docker exec aws-kinesis-emulator-kinesis-setup-1 curl -sf `
    -X POST http://localstack:4566/ `
    -H "Content-Type: application/x-amz-json-1.1" `
    -H "X-Amz-Target: Kinesis_20131202.GetShardIterator" `
    -d '{"StreamName":"keycloak-events","ShardId":"shardId-000000000000","ShardIteratorType":"TRIM_HORIZON"}' `
    2>&1 | Out-String

if ($shardIterResponse -notmatch '"ShardIterator"\s*:\s*"([^"]+)"') {
    return $false
}

$shardIterator = $Matches[1]

# Get records using the shard iterator
$recordsResponse = docker exec aws-kinesis-emulator-kinesis-setup-1 curl -sf `
    -X POST http://localstack:4566/ `
    -H "Content-Type: application/x-amz-json-1.1" `
    -H "X-Amz-Target: Kinesis_20131202.GetRecords" `
    -d "{`"ShardIterator`":`"$shardIterator`",`"Limit`":10}" `
    2>&1 | Out-String

if ($recordsResponse -notmatch '"Data"\s*:\s*"([^"]+)"') {
    return $false
}

# Decode base64 data
$base64Data = $Matches[1]
$decoded = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($base64Data))

return $decoded -match "LOGIN"
