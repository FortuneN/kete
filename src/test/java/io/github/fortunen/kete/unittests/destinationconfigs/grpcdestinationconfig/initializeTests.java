package io.github.fortunen.kete.unittests.destinationconfigs.grpcdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.grpc.GrpcDestinationConfig;

public class initializeTests {

	private Map<String, Object> minimalConfig() {
		var map = new HashMap<String, Object>();
		map.put("kind", "grpc");
		map.put("host", "grpc-server.example.com");
		map.put("service", "EventService");
		map.put("method", "SendEvent");
		return map;
	}

	// =========================================================================
	// Required Fields - Host
	// =========================================================================

	@Test
	public void shouldThrowWhenHostIsMissing() {

		// arrange

		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "grpc",
			"service", "EventService",
			"method", "SendEvent"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("host is required");
	}

	@Test
	public void shouldThrowWhenHostIsEmpty() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "grpc");
		map.put("host", "");
		map.put("service", "EventService");
		map.put("method", "SendEvent");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("host is required");
	}

	@Test
	public void shouldThrowWhenHostIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "grpc");
		map.put("host", "   ");
		map.put("service", "EventService");
		map.put("method", "SendEvent");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("host is required");
	}

	// =========================================================================
	// Required Fields - Service
	// =========================================================================

	@Test
	public void shouldThrowWhenServiceIsMissing() {

		// arrange

		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "grpc",
			"host", "grpc-server.example.com",
			"method", "SendEvent"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("service is required");
	}

	@Test
	public void shouldThrowWhenServiceIsEmpty() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "grpc");
		map.put("host", "grpc-server.example.com");
		map.put("service", "");
		map.put("method", "SendEvent");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("service is required");
	}

	@Test
	public void shouldThrowWhenServiceIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "grpc");
		map.put("host", "grpc-server.example.com");
		map.put("service", "   ");
		map.put("method", "SendEvent");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("service is required");
	}

	// =========================================================================
	// Required Fields - Method
	// =========================================================================

	@Test
	public void shouldThrowWhenMethodIsMissing() {

		// arrange

		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "grpc",
			"host", "grpc-server.example.com",
			"service", "EventService"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("method is required");
	}

	@Test
	public void shouldThrowWhenMethodIsEmpty() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "grpc");
		map.put("host", "grpc-server.example.com");
		map.put("service", "EventService");
		map.put("method", "");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("method is required");
	}

	@Test
	public void shouldThrowWhenMethodIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "grpc");
		map.put("host", "grpc-server.example.com");
		map.put("service", "EventService");
		map.put("method", "   ");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("method is required");
	}

	// =========================================================================
	// Defaults
	// =========================================================================

	@Test
	public void shouldDefaultPortTo50051() {

		// arrange

		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(50051);
	}

	@Test
	public void shouldDefaultTimeoutSecondsTo10() {

		// arrange

		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(10);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(10));
	}

	@Test
	public void shouldDefaultUsePlaintextToFalse() {

		// arrange

		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.isUsePlaintext()).isFalse();
	}

	@Test
	public void shouldDefaultContentTransferEncodingToNull() {

		// arrange

		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentTransferEncoding()).isNull();
		assertThat(config.getContentTransferEncodingName()).isNull();
	}

	@Test
	public void shouldDefaultContentEncodingToNull() {

		// arrange

		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentEncoding()).isNull();
		assertThat(config.getContentEncodingName()).isNull();
	}

	// =========================================================================
	// Custom Values
	// =========================================================================

	@Test
	public void shouldConfigureHost() {

		// arrange

		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("grpc-server.example.com");
	}

	@Test
	public void shouldConfigureCustomPort() {

		// arrange

		var map = minimalConfig();
		map.put("port", 9090);
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(9090);
	}

	@Test
	public void shouldConfigureService() {

		// arrange

		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getService()).isEqualTo("EventService");
	}

	@Test
	public void shouldConfigureMethod() {

		// arrange

		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getMethod()).isEqualTo("SendEvent");
	}

	@Test
	public void shouldConfigureCustomTimeoutSeconds() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 30);
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
	}

	@Test
	public void shouldConfigureUsePlaintext() {

		// arrange

		var map = minimalConfig();
		map.put("use-plaintext", true);
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isUsePlaintext()).isTrue();
	}

	// =========================================================================
	// Precomputed Fields
	// =========================================================================

	@Test
	public void shouldPreComputeFullMethodName() {

		// arrange

		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getFullMethodName()).isEqualTo("EventService/SendEvent");
	}

	@Test
	public void shouldPreComputeFullMethodNameWithPackagedService() {

		// arrange

		var map = minimalConfig();
		map.put("service", "com.example.EventService");
		map.put("method", "PushEvent");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getFullMethodName()).isEqualTo("com.example.EventService/PushEvent");
	}

	@Test
	public void shouldPreComputeTimeout() {

		// arrange

		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(10));
	}

	@Test
	public void shouldPreComputeChannelBuilder() {

		// arrange

		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getChannelBuilder()).isNotNull();
	}

	// =========================================================================
	// Port Validation
	// =========================================================================

	@Test
	public void shouldThrowWhenPortIsZero() {

		// arrange

		var map = minimalConfig();
		map.put("port", 0);
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("port");
	}

	@Test
	public void shouldThrowWhenPortIsNegative() {

		// arrange

		var map = minimalConfig();
		map.put("port", -1);
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("port");
	}

	@Test
	public void shouldThrowWhenPortExceeds65535() {

		// arrange

		var map = minimalConfig();
		map.put("port", 70000);
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("port");
	}

	// =========================================================================
	// Timeout Validation
	// =========================================================================

	@Test
	public void shouldThrowWhenTimeoutSecondsIsZero() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 0);
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("timeout-seconds");
	}

	@Test
	public void shouldThrowWhenTimeoutSecondsIsNegative() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", -5);
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("timeout-seconds");
	}

	// =========================================================================
	// Trimming
	// =========================================================================

	@Test
	public void shouldTrimHost() {

		// arrange

		var map = minimalConfig();
		map.put("host", "  grpc-server.example.com  ");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("grpc-server.example.com");
	}

	@Test
	public void shouldTrimService() {

		// arrange

		var map = minimalConfig();
		map.put("service", "  EventService  ");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getService()).isEqualTo("EventService");
	}

	@Test
	public void shouldTrimMethod() {

		// arrange

		var map = minimalConfig();
		map.put("method", "  SendEvent  ");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getMethod()).isEqualTo("SendEvent");
	}

	// =========================================================================
	// Content-Transfer-Encoding
	// =========================================================================

	@Test
	public void shouldResolveContentTransferEncoding() {

		// arrange

		var map = minimalConfig();
		map.put("content-transfer-encoding", "base64");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentTransferEncoding()).isNotNull();
		assertThat(config.getContentTransferEncodingName()).isEqualTo("base64");
	}

	@Test
	public void shouldThrowWhenContentTransferEncodingIsUnknown() {

		// arrange

		var map = minimalConfig();
		map.put("content-transfer-encoding", "unknown-encoding");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("unknown content-transfer-encoding");
	}

	@Test
	public void shouldIgnoreBlankContentTransferEncoding() {

		// arrange

		var map = minimalConfig();
		map.put("content-transfer-encoding", "   ");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentTransferEncoding()).isNull();
		assertThat(config.getContentTransferEncodingName()).isNull();
	}

	// =========================================================================
	// Content-Encoding
	// =========================================================================

	@Test
	public void shouldResolveContentEncoding() {

		// arrange

		var map = minimalConfig();
		map.put("content-encoding", "gzip");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentEncoding()).isNotNull();
		assertThat(config.getContentEncodingName()).isEqualTo("gzip");
	}

	@Test
	public void shouldThrowWhenContentEncodingIsUnknown() {

		// arrange

		var map = minimalConfig();
		map.put("content-encoding", "unknown-compression");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("unknown content-encoding");
	}

	@Test
	public void shouldIgnoreBlankContentEncoding() {

		// arrange

		var map = minimalConfig();
		map.put("content-encoding", "   ");
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentEncoding()).isNull();
		assertThat(config.getContentEncodingName()).isNull();
	}

	// =========================================================================
	// Full Configuration
	// =========================================================================

	@Test
	public void shouldInitializeWithFullConfiguration() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "grpc");
		map.put("host", "grpc-server.example.com");
		map.put("port", 9090);
		map.put("service", "com.example.EventService");
		map.put("method", "PushEvent");
		map.put("timeout-seconds", 30);
		map.put("use-plaintext", true);
		var config = new GrpcDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("grpc-server.example.com");
		assertThat(config.getPort()).isEqualTo(9090);
		assertThat(config.getService()).isEqualTo("com.example.EventService");
		assertThat(config.getMethod()).isEqualTo("PushEvent");
		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
		assertThat(config.isUsePlaintext()).isTrue();
		assertThat(config.getFullMethodName()).isEqualTo("com.example.EventService/PushEvent");
		assertThat(config.getChannelBuilder()).isNotNull();
	}
}
