package io.github.fortunen.kete.unittests.destinationconfigs.zeromqdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.zeromq.SocketType;

import io.github.fortunen.kete.destinations.zeromq.ZeroMQDestinationConfig;

public class initializeTests {

	// =========================================================================
	// Required Fields - Endpoint
	// =========================================================================

	@Test
	public void shouldThrowWhenEndpointIsMissing() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("endpoint is required");
	}

	@Test
	public void shouldThrowWhenEndpointIsEmpty() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", ""
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("endpoint is required");
	}

	@Test
	public void shouldThrowWhenEndpointIsBlank() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "   "
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("endpoint is required");
	}

	// =========================================================================
	// Socket Type
	// =========================================================================

	@Test
	public void shouldDefaultSocketTypeToPublish() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getSocketType()).isEqualTo(ZeroMQDestinationConfig.PUBLISH);
		assertThat(config.getSocketTypeValue()).isEqualTo(SocketType.PUB);
	}

	@Test
	public void shouldAcceptPushSocketType() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555",
			"socket-type", "PUSH"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getSocketType()).isEqualTo(ZeroMQDestinationConfig.PUSH);
		assertThat(config.getSocketTypeValue()).isEqualTo(SocketType.PUSH);
	}

	@Test
	public void shouldAcceptPublishSocketType() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555",
			"socket-type", "PUBLISH"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getSocketType()).isEqualTo(ZeroMQDestinationConfig.PUBLISH);
		assertThat(config.getSocketTypeValue()).isEqualTo(SocketType.PUB);
	}

	@Test
	public void shouldAcceptSocketTypeCaseInsensitively() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555",
			"socket-type", "push"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getSocketType()).isEqualTo(ZeroMQDestinationConfig.PUSH);
		assertThat(config.getSocketTypeValue()).isEqualTo(SocketType.PUSH);
	}

	@Test
	public void shouldThrowWhenSocketTypeIsInvalid() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555",
			"socket-type", "INVALID"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("socket-type must be one of : PUBLISH or PUSH");
	}

	@Test
	public void shouldTrimSocketType() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555",
			"socket-type", "  PUSH  "
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getSocketType()).isEqualTo(ZeroMQDestinationConfig.PUSH);
	}

	// =========================================================================
	// Connection Mode
	// =========================================================================

	@Test
	public void shouldDefaultConnectionModeToConnect() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionMode()).isEqualTo(ZeroMQDestinationConfig.CONNECT);
	}

	@Test
	public void shouldAcceptBindConnectionMode() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555",
			"connection-mode", "BIND"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionMode()).isEqualTo(ZeroMQDestinationConfig.BIND);
	}

	@Test
	public void shouldAcceptConnectConnectionMode() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555",
			"connection-mode", "CONNECT"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionMode()).isEqualTo(ZeroMQDestinationConfig.CONNECT);
	}

	@Test
	public void shouldAcceptConnectionModeCaseInsensitively() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555",
			"connection-mode", "bind"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionMode()).isEqualTo(ZeroMQDestinationConfig.BIND);
	}

	@Test
	public void shouldThrowWhenConnectionModeIsInvalid() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555",
			"connection-mode", "INVALID"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("connection-mode must be one of : BIND or CONNECT");
	}

	@Test
	public void shouldTrimConnectionMode() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555",
			"connection-mode", "  BIND  "
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionMode()).isEqualTo(ZeroMQDestinationConfig.BIND);
	}

	// =========================================================================
	// Linger
	// =========================================================================

	@Test
	public void shouldNotFlagSendTimeoutWhenNotConfigured() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isHasSendTimeout()).isFalse();
	}

	@Test
	public void shouldAcceptCustomSendTimeout() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555",
			"send-timeout-seconds", "5"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isHasSendTimeout()).isTrue();
		assertThat(config.getSendTimeoutSeconds()).isEqualTo(5);
	}

	@Test
	public void shouldThrowWhenSendTimeoutIsNegative() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555",
			"send-timeout-seconds", "-1"
		)));

		// act

		var thrown = catchThrowable(config::initialize);

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("send-timeout-seconds must be non-negative");
	}

	@Test
	public void shouldDefaultLingerTo1000() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getLinger()).isEqualTo(ZeroMQDestinationConfig.DEFAULT_LINGER);
	}

	@Test
	public void shouldAcceptCustomLinger() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "zeromq");
		map.put("endpoint", "tcp://localhost:5555");
		map.put("linger", 5000);
		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getLinger()).isEqualTo(5000);
	}

	@Test
	public void shouldAcceptLingerOfZero() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "zeromq");
		map.put("endpoint", "tcp://localhost:5555");
		map.put("linger", 0);
		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getLinger()).isEqualTo(0);
	}

	@Test
	public void shouldAcceptLingerOfNegativeOne() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "zeromq");
		map.put("endpoint", "tcp://localhost:5555");
		map.put("linger", -1);
		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getLinger()).isEqualTo(-1);
	}

	@Test
	public void shouldThrowWhenLingerIsLessThanNegativeOne() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "zeromq");
		map.put("endpoint", "tcp://localhost:5555");
		map.put("linger", -2);
		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("linger must be -1 or greater");
	}

	// =========================================================================
	// Successful Initialization - Full Configuration
	// =========================================================================

	@Test
	public void shouldInitializeWithMinimalConfiguration() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "tcp://localhost:5555"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getEndpoint()).isEqualTo("tcp://localhost:5555");
		assertThat(config.getSocketType()).isEqualTo(ZeroMQDestinationConfig.PUBLISH);
		assertThat(config.getSocketTypeValue()).isEqualTo(SocketType.PUB);
		assertThat(config.getConnectionMode()).isEqualTo(ZeroMQDestinationConfig.CONNECT);
		assertThat(config.getLinger()).isEqualTo(ZeroMQDestinationConfig.DEFAULT_LINGER);
	}

	@Test
	public void shouldInitializeWithFullConfiguration() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "zeromq");
		map.put("endpoint", "tcp://localhost:5555");
		map.put("socket-type", "PUSH");
		map.put("connection-mode", "BIND");
		map.put("linger", 3000);
		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEndpoint()).isEqualTo("tcp://localhost:5555");
		assertThat(config.getSocketType()).isEqualTo(ZeroMQDestinationConfig.PUSH);
		assertThat(config.getSocketTypeValue()).isEqualTo(SocketType.PUSH);
		assertThat(config.getConnectionMode()).isEqualTo(ZeroMQDestinationConfig.BIND);
		assertThat(config.getLinger()).isEqualTo(3000);
	}

	@Test
	public void shouldTrimEndpoint() {

		// arrange

		var config = new ZeroMQDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "zeromq",
			"endpoint", "  tcp://localhost:5555  "
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getEndpoint()).isEqualTo("tcp://localhost:5555");
	}
}
