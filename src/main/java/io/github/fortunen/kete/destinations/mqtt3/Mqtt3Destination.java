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

	private int qos;
	private String topic;
	private boolean retained;
	private MqttClient client;
	private boolean isTopicTemplated;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		var clientId = config.getClientIdPrefix() + "-" + config.getClientIdCounter().incrementAndGet();

		qos = config.getQos();
		topic = config.getTopic();
		retained = config.isRetained();
		isTopicTemplated = config.isTopicTemplated();
		client = new MqttClient(config.getUrl(), clientId, new MemoryPersistence());

		// verify connection

		client.connect(config.getConnectOptions());
	}

	@Override
	public boolean isHealthy() {
		return ValidationUtils.isNotNull(client) && client.isConnected();
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// actualTopic

		var actualTopic = isTopicTemplated ? TemplateUtils.substitute(topic, message) : topic;

		// mqttMessage

		var mqttMessage = new MqttMessage();

		mqttMessage.setQos(qos);
		mqttMessage.setRetained(retained);
		mqttMessage.setPayload(message.eventBody());

		// publish

		client.publish(actualTopic, mqttMessage);
	}

	@Override
	@SneakyThrows
	public void close() {

		if (ValidationUtils.isNull(client)) {
			return;
		}

		try {
			if (client.isConnected()) {
				client.disconnectForcibly(0, 0, true);
			}
		} catch (Exception e) {
			log.warn("Failed to disconnect MQTT3 client: {}", e.getMessage());
		}

		// force close: the non-forced close() throws CONNECT_IN_PROGRESS while the client is
		// auto-reconnecting, which is exactly when the pool culls and destroys this instance

		try {
			client.close(true);
		} catch (Exception e) {
			log.warn("Failed to close MQTT3 client: {}", e.getMessage());
		}
	}
}
