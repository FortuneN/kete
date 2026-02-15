# Check if event was received by AMQP 1.0 subscriber
$output = docker logs amqp-1-solace-subscriber-1 2>&1 | Select-String "EVENT-RECEIVED"
return $null -ne $output
