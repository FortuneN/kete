# Check if subscriber received the event
$output = docker logs mqtt-5-activemq-artemis-subscriber-1 2>&1 | Select-String "EVENT-RECEIVED"
return $null -ne $output
