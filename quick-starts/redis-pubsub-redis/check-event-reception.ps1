# Check if subscriber received the event
$output = docker logs redis-pubsub-redis-subscriber-1 2>&1 | Select-String "EVENT-RECEIVED"
return $output -ne $null
