# Azure Event Hubs Emulator (Kafka Protocol)

> **⚠️ CURRENTLY NOT WORKING - EMULATOR LIMITATION**

## Status: Blocked

This quick-start is **currently non-functional** due to a limitation in the Azure Event Hubs Emulator.

### Issue

The Azure Event Hubs Emulator only supports **Kafka clients ≤3.8.x**, but KETE uses **Kafka 4.1.1**.

When attempting to send events, the emulator returns:
```
io.github.fortunen.kete.shaded.kafka.common.errors.UnsupportedForMessageFormatException: 
The message format version on the broker does not support the request.
```

### Microsoft's Response

Per [GitHub Issue #50](https://github.com/Azure/azure-event-hubs-emulator-installer/issues/50):
- **Status:** Work in progress (as of July 2025)
- **Expected:** Future emulator release will support Kafka 3.9.0+

### TODO

- [ ] Revisit when Microsoft releases updated Event Hubs Emulator with Kafka 3.9+ support
- [ ] Monitor: https://github.com/Azure/azure-event-hubs-emulator-installer/issues/50

## Configuration for Production Azure Event Hubs

The configuration in `docker-compose.yml` is **correct for production Azure Event Hubs** (not the emulator):

```yaml
kete.routes.quick-start.destination.kind: kafka
kete.routes.quick-start.destination.bootstrap.servers: <namespace>.servicebus.windows.net:9093
kete.routes.quick-start.destination.topic: <your-eventhub-name>
kete.routes.quick-start.destination.sasl.mechanism: PLAIN
kete.routes.quick-start.destination.security.protocol: SASL_PLAINTEXT
kete.routes.quick-start.destination.enable.idempotence: "false"
kete.routes.quick-start.destination.sasl.jaas.config: io.github.fortunen.kete.shaded.kafka.common.security.plain.PlainLoginModule required username="$ConnectionString" password="Endpoint=sb://<namespace>.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=<your-key>;";
```

Replace:
- `<namespace>` - Your Event Hubs namespace
- `<your-eventhub-name>` - Your Event Hub name
- `<your-key>` - Your connection string key

## Notes

- **Production Azure Event Hubs supports Kafka 1.0+** (including 4.1.1)
- This is purely a **local emulator limitation**
- KETE works fine with real Azure Event Hubs in production
