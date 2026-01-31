package io.github.fortunen.kete.destinations.mqtt5;

import java.util.ArrayList;

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

@Data
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

		// mqttMessage

		var mqttMessage = new MqttMessage(message.eventBody());

		mqttMessage.setQos(config.getQos());
		mqttMessage.setRetained(config.isRetained());

		// headers

		if (config.isMessageHeadersEnabled()) {

			var properties = new MqttProperties();
			var userProperties = new ArrayList<UserProperty>();

			properties.setContentType(message.contentType());
			userProperties.add(new UserProperty(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType()));
			userProperties.add(new UserProperty(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind()));

			mqttMessage.setProperties(properties);
			properties.setUserProperties(userProperties);
		}

		// publish

		client.publish(actualTopic, mqttMessage);
	}

	@Override
	@SneakyThrows
	public void close() {
		ValidationUtils.tryClose(client, "client");
	}
}
