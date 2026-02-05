# Check if subscriber received the event
$output = docker logs stomp-emqx-subscriber-1 2>&1 | Select-String "EVENT-RECEIVED"
return $output -ne $null
