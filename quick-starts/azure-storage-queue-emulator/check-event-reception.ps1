# Check if event was received via Azure Storage Queue (Azurite emulator)
# Uses the queue-setup container (has Python + azure-storage-queue) to peek messages
# Returns $true if LOGIN event found, $false otherwise

$result = docker exec azure-storage-queue-emulator-queue-setup-1 python -c @"
from azure.storage.queue import QueueServiceClient
import base64
conn = 'DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;QueueEndpoint=http://azurite:10001/devstoreaccount1'
queue = QueueServiceClient.from_connection_string(conn).get_queue_client('keycloak-events')
for msg in queue.peek_messages(max_messages=32):
    try:
        decoded = base64.b64decode(msg.content).decode()
    except Exception:
        decoded = msg.content or ''
    print(decoded)
"@ 2>&1 | Out-String

return $result -match "LOGIN"
