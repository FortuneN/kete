# Check if subscriber received the event
$output = docker logs nats-nats-server-subscriber-1 2>&1 | Select-String "EVENT-RECEIVED"
return $null -ne $output
