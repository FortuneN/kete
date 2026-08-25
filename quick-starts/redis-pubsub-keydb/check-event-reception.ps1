# Check if subscriber received the event
$output = docker logs redis-pubsub-keydb-subscriber-1 2>&1 | Select-String "EVENT-RECEIVED"
return $null -ne $output
