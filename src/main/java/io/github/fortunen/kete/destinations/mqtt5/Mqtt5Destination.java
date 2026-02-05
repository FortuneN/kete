package io.github.fortunen.kete.destinations.mqtt5;

import java.util.ArrayList;
import java.util.LinkedHashMap;

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

		// userProperties (message headers take priority over custom headers)

		var headerMap = new LinkedHashMap<String, String>();

		for (var entry : config.getCustomHeadersEntrySet()) {
			headerMap.put(entry.getKey(), entry.getValue());
		}

		headerMap.put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		headerMap.put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());

		var userProperties = new ArrayList<UserProperty>();

		headerMap.forEach((key, value) -> userProperties.add(new UserProperty(key, value)));

		// properties

		var properties = new MqttProperties();

		properties.setContentType(message.contentType());
		properties.setUserProperties(userProperties);

		// mqttMessage

		var mqttMessage = new MqttMessage();

		mqttMessage.setQos(config.getQos());
		mqttMessage.setProperties(properties);
		mqttMessage.setPayload(message.eventBody());
		mqttMessage.setRetained(config.isRetained());

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
			log.warn("Failed to disconnect MQTT5 client: {}", e.getMessage());
		} finally {
			ValidationUtils.tryClose(client, "client");
		}
	}
}
