package io.github.fortunen.kete.unittests.destinantionconfigs.stompdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.stomp.StompDestinationConfig;

public class initializeTests {

	// =========================================================================
	// Required Fields - Host
	// =========================================================================

	@Test
	public void shouldThrowWhenHostIsMissing() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"destination", "/queue/events"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("host is required");
	}

	@Test
	public void shouldThrowWhenHostIsEmpty() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "",
			"destination", "/queue/events"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("host is required");
	}

	@Test
	public void shouldThrowWhenHostIsBlank() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "   ",
			"destination", "/queue/events"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("host is required");
	}

	// =========================================================================
	// Required Fields - Destination
	// =========================================================================

	@Test
	public void shouldThrowWhenDestinationIsMissing() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("destination is required");
	}

	@Test
	public void shouldThrowWhenDestinationIsEmpty() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", ""
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("destination is required");
	}

	@Test
	public void shouldThrowWhenDestinationIsBlank() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "   "
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("destination is required");
	}

	// =========================================================================
	// Basic Configuration
	// =========================================================================

	@Test
	public void shouldInitializeWithMinimalConfiguration() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("localhost");
		assertThat(config.getDestination()).isEqualTo("/queue/events");
	}

	// =========================================================================
	// Port Configuration
	// =========================================================================

	@Test
	public void shouldUseDefaultPort() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(61613);
	}

	@Test
	public void shouldUseCustomPort() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"port", "61614",
			"destination", "/queue/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(61614);
	}

	@Test
	public void shouldThrowWhenPortIsTooHigh() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"port", "65536",
			"destination", "/queue/events"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("port must be between 1 and 65535");
	}

	@Test
	public void shouldThrowWhenPortIsTooLow() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"port", "0",
			"destination", "/queue/events"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("port must be between 1 and 65535");
	}

	// =========================================================================
	// Virtual Host Configuration
	// =========================================================================

	@Test
	public void shouldDefaultVirtualHostToHost() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getVirtualHost()).isEqualTo("localhost");
	}

	@Test
	public void shouldUseCustomVirtualHost() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events",
			"virtual-host", "/vhost1"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getVirtualHost()).isEqualTo("/vhost1");
	}

	// =========================================================================
	// Authentication Configuration
	// =========================================================================

	@Test
	public void shouldHaveEmptyUsernameByDefault() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUsername()).isEmpty();
	}

	@Test
	public void shouldHaveEmptyPasswordByDefault() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPassword()).isEmpty();
	}

	@Test
	public void shouldParseUsername() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events",
			"username", "admin"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUsername()).isEqualTo("admin");
	}

	@Test
	public void shouldParsePassword() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events",
			"password", "secret"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPassword()).isEqualTo("secret");
	}

	// =========================================================================
	// Receipt Configuration
	// =========================================================================

	@Test
	public void shouldDisableReceiptByDefault() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isReceiptEnabled()).isFalse();
	}

	@Test
	public void shouldEnableReceipt() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events",
			"receipt-enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isReceiptEnabled()).isTrue();
	}

	// =========================================================================
	// Heart-Beat Configuration
	// =========================================================================

	@Test
	public void shouldUseDefaultHeartBeatOutgoing() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getHeartBeatOutgoing()).isEqualTo(30000);
	}

	@Test
	public void shouldUseDefaultHeartBeatIncoming() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getHeartBeatIncoming()).isEqualTo(30000);
	}

	@Test
	public void shouldDisableHeartBeatWhenSetToZero() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events",
			"heart-beat-outgoing", "0",
			"heart-beat-incoming", "0"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getHeartBeatOutgoing()).isEqualTo(0);
		assertThat(config.getHeartBeatIncoming()).isEqualTo(0);
	}

	@Test
	public void shouldParseHeartBeatOutgoing() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events",
			"heart-beat-outgoing", "10000"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getHeartBeatOutgoing()).isEqualTo(10000);
	}

	@Test
	public void shouldParseHeartBeatIncoming() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events",
			"heart-beat-incoming", "10000"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getHeartBeatIncoming()).isEqualTo(10000);
	}

	@Test
	public void shouldThrowWhenHeartBeatOutgoingIsNegative() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events",
			"heart-beat-outgoing", "-1"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("heart-beat-outgoing must be non-negative");
	}

	@Test
	public void shouldThrowWhenHeartBeatIncomingIsNegative() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events",
			"heart-beat-incoming", "-1"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("heart-beat-incoming must be non-negative");
	}

	// =========================================================================
	// Read Timeout Configuration
	// =========================================================================

	@Test
	public void shouldUseDefaultReadTimeout() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getReadTimeoutMillis()).isEqualTo(30000);
	}

	@Test
	public void shouldParseReadTimeout() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events",
			"read-timeout-millis", "30000"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getReadTimeoutMillis()).isEqualTo(30000);
	}

	@Test
	public void shouldThrowWhenReadTimeoutIsZero() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events",
			"read-timeout-millis", "0"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("read-timeout-millis must be greater than 0");
	}

	@Test
	public void shouldThrowWhenReadTimeoutIsNegative() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events",
			"read-timeout-millis", "-1"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("read-timeout-millis must be greater than 0");
	}

	// =========================================================================
	// TLS Configuration
	// =========================================================================

	@Test
	public void shouldDisableTlsByDefault() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTls().isEnabled()).isFalse();
	}

	@Test
	public void shouldEnableTls() {

		// arrange

		var config = new StompDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination", "/queue/events",
			"tls.enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTls().isEnabled()).isTrue();
	}
}
