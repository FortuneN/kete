package io.github.fortunen.kete.destinations.mqtt5;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
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
@Component(name = "mqtt-5")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class Mqtt5Destination extends Destination<Mqtt5DestinationConfig> {

	private int qos;
	private String topic;
	private boolean retained;
	private MqttClient client;
	private boolean isTopicTemplated;
	private List<UserProperty> staticUserProperties;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		var clientId = config.getClientIdPrefix() + "-" + config.getClientIdCounter().incrementAndGet();

		qos = config.getQos();
		topic = config.getTopic();
		retained = config.isRetained();
		isTopicTemplated = config.isTopicTemplated();
		staticUserProperties = config.getStaticUserProperties();
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

		// userProperties (start from precomputed static list, add per-message KETE headers)

		var userProperties = new ArrayList<>(staticUserProperties);

		userProperties.add(new UserProperty(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind()));
		userProperties.add(new UserProperty(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType()));

		// properties

		var properties = new MqttProperties();

		properties.setContentType(message.contentType());
		properties.setUserProperties(userProperties);

		// mqttMessage

		var mqttMessage = new MqttMessage();

		mqttMessage.setQos(qos);
		mqttMessage.setProperties(properties);
		mqttMessage.setPayload(message.eventBody());
		mqttMessage.setRetained(retained);

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
			log.warn("Failed to disconnect MQTT5 client: {}", e.getMessage());
		}

		// force close: the non-forced close() throws CONNECT_IN_PROGRESS while the client is
		// auto-reconnecting, which is exactly when the pool culls and destroys this instance

		try {
			client.close(true);
		} catch (Exception e) {
			log.warn("Failed to close MQTT5 client: {}", e.getMessage());
		}
	}
}
