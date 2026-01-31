package io.github.fortunen.kete.unittests.utils.iocutils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.destinations.amqp091.Amqp091Destination;
import io.github.fortunen.kete.destinations.amqp1.Amqp1Destination;
import io.github.fortunen.kete.destinations.http.HttpDestination;
import io.github.fortunen.kete.destinations.kafka.KafkaDestination;
import io.github.fortunen.kete.destinations.mqtt3.Mqtt3Destination;
import io.github.fortunen.kete.destinations.mqtt5.Mqtt5Destination;
import io.github.fortunen.kete.utils.IocUtils;

public class getDestinationTests {

	@Test
	public void shouldGetHttpDestination() {

		// act

		var result = IocUtils.get("http", Destination.class);

		// assert

		assertThat(result)
			.as("Should return an HTTP destination")
			.isNotNull()
			.isInstanceOf(HttpDestination.class);
	}

	@Test
	public void shouldGetKafkaDestination() {

		// act

		var result = IocUtils.get("kafka", Destination.class);

		// assert

		assertThat(result)
			.as("Should return a Kafka destination")
			.isNotNull()
			.isInstanceOf(KafkaDestination.class);
	}

	@Test
	public void shouldGetAmqp1Destination() {

		// act

		var result = IocUtils.get("amqp-1", Destination.class);

		// assert

		assertThat(result)
			.as("Should return an AMQP destination")
			.isNotNull()
			.isInstanceOf(Amqp1Destination.class);
	}

	@Test
	public void shouldGetAmqp091Destination() {

		// act

		var result = IocUtils.get("amqp-0.9.1", Destination.class);

		// assert

		assertThat(result)
			.as("Should return an AMQP destination")
			.isNotNull()
			.isInstanceOf(Amqp091Destination.class);
	}

	@Test
	public void shouldGetMqtt3Destination() {

		// act

		var result = IocUtils.get("mqtt-3", Destination.class);

		// assert

		assertThat(result)
			.as("Should return an MQTT destination")
			.isNotNull()
			.isInstanceOf(Mqtt3Destination.class);
	}

	@Test
	public void shouldGetMqtt5Destination() {

		// act

		var result = IocUtils.get("mqtt-5", Destination.class);
		// assert

		assertThat(result)
			.as("Should return an MQTT destination")
			.isNotNull()
			.isInstanceOf(Mqtt5Destination.class);
	}

	@Test
	public void shouldReturnNewInstanceEachTimeForTransientDestinations() {

		// act

		var first = IocUtils.get("http", Destination.class);
		var second = IocUtils.get("http", Destination.class);

		// assert

		assertThat(first)
			.as("Should return different instances for transient destinations")
			.isNotSameAs(second);
	}
}
