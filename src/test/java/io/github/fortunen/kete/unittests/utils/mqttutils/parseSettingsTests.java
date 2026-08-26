package io.github.fortunen.kete.unittests.utils.mqttutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.utils.MqttUtils;

public class parseSettingsTests {

	private static MapConfiguration configuration(Map<String, Object> values) {
		return new MapConfiguration(new HashMap<>(values));
	}

	@Test
	public void shouldDefaultToTcpOnPort1883() {

		// act

		var settings = MqttUtils.parseSettings(configuration(Map.of("host", "broker.example", "topic", "events")), TlsMaterial.builder().build());

		// assert

		assertThat(settings.transportType()).isEqualTo("tcp");
		assertThat(settings.url()).isEqualTo("tcp://broker.example:1883");
		assertThat(settings.qos()).isEqualTo(MqttUtils.DEFAULT_QOS);
		assertThat(settings.retained()).isFalse();
		assertThat(settings.cleanSession()).isTrue();
		assertThat(settings.hasCleanSession()).isFalse();
		assertThat(settings.hasKeepAliveInterval()).isFalse();
		assertThat(settings.maxInflight()).isEqualTo(MqttUtils.DEFAULT_MAX_INFLIGHT);
		assertThat(settings.clientIdPrefix()).isNotBlank();
	}

	@Test
	public void shouldUseWebSocketSchemeAndPort() {

		// act

		var settings = MqttUtils.parseSettings(configuration(Map.of("host", "broker.example", "topic", "events", "transport-type", "websocket", "keep-alive-interval-seconds", 15)), TlsMaterial.builder().build());

		// assert

		assertThat(settings.url()).isEqualTo("ws://broker.example:8000");
		assertThat(settings.keepAliveIntervalSeconds()).isEqualTo(15);
		assertThat(settings.hasKeepAliveInterval()).isTrue();
	}

	@Test
	public void shouldRejectQosOutOfRange() {

		// act

		var thrown = catchThrowable(() -> MqttUtils.parseSettings(configuration(Map.of("host", "broker.example", "topic", "events", "qos", 3)), TlsMaterial.builder().build()));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("qos must be 0, 1 or 2");
	}

	@Test
	public void shouldRejectUnknownTransportType() {

		// act

		var thrown = catchThrowable(() -> MqttUtils.parseSettings(configuration(Map.of("host", "broker.example", "topic", "events", "transport-type", "udp")), TlsMaterial.builder().build()));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("transport-type must be 'tcp' or 'websocket'");
	}
}
