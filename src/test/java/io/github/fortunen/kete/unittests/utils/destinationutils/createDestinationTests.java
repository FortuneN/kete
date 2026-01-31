package io.github.fortunen.kete.unittests.utils.destinationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.destinations.http.HttpDestination;
import io.github.fortunen.kete.destinations.http.HttpDestinationConfig;
import io.github.fortunen.kete.destinations.kafka.KafkaDestination;
import io.github.fortunen.kete.destinations.kafka.KafkaDestinationConfig;
import io.github.fortunen.kete.utils.DestinationUtils;

public class createDestinationTests {

	@Test
	public void shouldThrowWhenConfigurationIsNull() {

		// act

		var thrown = catchThrowable(() -> DestinationUtils.createDestination(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("configuration is required");
	}

	@Test
	public void shouldThrowWhenDestinationKindIsNull() {

		// arrange

		var config = mock(DestinationConfig.class);
		when(config.getDestinationKind()).thenReturn(null);

		// act

		var thrown = catchThrowable(() -> DestinationUtils.createDestination(config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("key is required");
	}

	@Test
	public void shouldThrowWhenDestinationKindNotFound() {

		// arrange

		var config = mock(DestinationConfig.class);
		when(config.getDestinationKind()).thenReturn("unknown-kind");

		// act

		var thrown = catchThrowable(() -> DestinationUtils.createDestination(config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("destination kind 'unknown-kind' not found");
	}

	@Test
	public void shouldCreateHttpDestination() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setDestinationKind("http");

		// act

		var result = DestinationUtils.createDestination(config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(HttpDestination.class);
		assertThat(result.getConfig()).isSameAs(config);
	}

	@Test
	public void shouldCreateKafkaDestination() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setDestinationKind("kafka");

		// act

		var result = DestinationUtils.createDestination(config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(KafkaDestination.class);
		assertThat(result.getConfig()).isSameAs(config);
	}

	@Test
	public void shouldSetConfigOnDestination() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setDestinationKind("http");

		// act

		var result = DestinationUtils.createDestination(config);

		// assert

		assertThat(result.getConfig()).isSameAs(config);
	}
}
