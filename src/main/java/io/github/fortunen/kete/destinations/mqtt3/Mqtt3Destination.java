package io.github.fortunen.kete.destinations.mqtt3;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Component(name = "mqtt-3")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class Mqtt3Destination extends Destination<Mqtt3DestinationConfig> {

	private MqttClient client;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		var clientId = config.getClientIdPrefix() + "-" + config.getClientIdCounter().incrementAndGet();

		client = new MqttClient(config.getUrl(), clientId, new MemoryPersistence());
		client.connect(config.getConnectOptions());
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// actualTopic

		var actualTopic = TemplateUtils.substitute(config.getTopic(), message);

		// mqttMessage

		var mqttMessage = new MqttMessage();

		mqttMessage.setQos(config.getQos());
		mqttMessage.setRetained(config.isRetained());
		mqttMessage.setPayload(message.eventBody());

		// publish

		client.publish(actualTopic, mqttMessage);
	}

	@Override
	@SneakyThrows
	public void close() {
		try {
			if (client != null && client.isConnected()) {
				client.disconnect();
			}
		} catch (Exception e) {
			log.warn("Failed to disconnect MQTT3 client: {}", e.getMessage());
		} finally {
			ValidationUtils.tryClose(client, "client");
		}
	}
}
