# Check if event was received by MQTT subscriber
$output = docker logs mqtt-5-solace-subscriber-1 2>&1 | Select-String "EVENT-RECEIVED"
return $null -ne $output
