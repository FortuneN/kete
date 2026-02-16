package io.github.fortunen.kete.unittests.destinationconfigs.azurewebpubsubdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.azurewebpubsub.AzureWebPubSubDestinationConfig;

public class initializeTests {

	private static final String VALID_CONNECTION_STRING = "Endpoint=https://mywebpubsub.webpubsub.azure.com;AccessKey=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=;Version=1.0;";

	private Map<String, Object> minimalConfig() {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-webpubsub");
		map.put("connection-string", VALID_CONNECTION_STRING);
		map.put("hub", "test-hub");
		return map;
	}

	// =========================================================================
	// Required Fields - Connection String
	// =========================================================================

	@Test
	public void shouldThrowWhenConnectionStringIsMissing() {

		// arrange

		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-webpubsub", "hub", "test-hub")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'connection-string' or 'authentication-type' must be set");
	}

	@Test
	public void shouldThrowWhenConnectionStringIsEmpty() {

		// arrange

		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-webpubsub", "connection-string", "", "hub", "test-hub")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'connection-string' or 'authentication-type' must be set");
	}

	@Test
	public void shouldThrowWhenConnectionStringIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-webpubsub");
		map.put("connection-string", "   ");
		map.put("hub", "test-hub");
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'connection-string' or 'authentication-type' must be set");
	}

	// =========================================================================
	// Required Fields - Hub
	// =========================================================================

	@Test
	public void shouldThrowWhenHubIsMissing() {

		// arrange

		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-webpubsub", "connection-string", VALID_CONNECTION_STRING)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("hub is required");
	}

	@Test
	public void shouldThrowWhenHubIsEmpty() {

		// arrange

		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-webpubsub", "connection-string", VALID_CONNECTION_STRING, "hub", "")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("hub is required");
	}

	@Test
	public void shouldThrowWhenHubIsBlank() {

		// arrange

		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-webpubsub", "connection-string", VALID_CONNECTION_STRING, "hub", "   ")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("hub is required");
	}

	// =========================================================================
	// Defaults
	// =========================================================================

	@Test
	public void shouldUseDefaultTimeoutSeconds() {

		// arrange

		var map = minimalConfig();
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(10);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(10));
	}

	@Test
	public void shouldDefaultGroupToEmpty() {

		// arrange

		var map = minimalConfig();
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getGroup()).isEmpty();
		assertThat(config.isHasGroup()).isFalse();
	}

	// =========================================================================
	// Custom Configuration
	// =========================================================================

	@Test
	public void shouldUseCustomTimeoutSeconds() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 30);
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
	}

	@Test
	public void shouldUseCustomGroup() {

		// arrange

		var map = minimalConfig();
		map.put("group", "my-group");
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getGroup()).isEqualTo("my-group");
		assertThat(config.isHasGroup()).isTrue();
	}

	@Test
	public void shouldTrimHub() {

		// arrange

		var map = minimalConfig();
		map.put("hub", "  test-hub  ");
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getHub()).isEqualTo("test-hub");
	}

	@Test
	public void shouldTrimGroup() {

		// arrange

		var map = minimalConfig();
		map.put("group", "  my-group  ");
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getGroup()).isEqualTo("my-group");
	}

	// =========================================================================
	// Validation - Timeout
	// =========================================================================

	@Test
	public void shouldThrowWhenTimeoutSecondsIsZero() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 0);
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("timeout-seconds must be positive");
	}

	@Test
	public void shouldThrowWhenTimeoutSecondsIsNegative() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", -1);
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("timeout-seconds must be positive");
	}

	// =========================================================================
	// Pre-computed Fields
	// =========================================================================

	@Test
	public void shouldSetHttpClient() {

		// arrange

		var map = minimalConfig();
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getHttpClient()).isNotNull();
	}

	@Test
	public void shouldSetWebPubSubClientBuilder() {

		// arrange

		var map = minimalConfig();
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getWebPubSubClientBuilder()).isNotNull();
	}

	@Test
	public void shouldSetConnectionString() {

		// arrange

		var map = minimalConfig();
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionString()).isEqualTo(VALID_CONNECTION_STRING);
	}

	// =========================================================================
	// Pre-computed Fields — Templating
	// =========================================================================

	@Test
	public void shouldDetectTemplatedHub() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-webpubsub");
		map.put("connection-string", VALID_CONNECTION_STRING);
		map.put("hub", "events-${realm}");
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isHubTemplated()).isTrue();
	}

	@Test
	public void shouldDetectNonTemplatedHub() {

		// arrange

		var map = minimalConfig();
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isHubTemplated()).isFalse();
	}

	@Test
	public void shouldDetectTemplatedGroup() {

		// arrange

		var map = minimalConfig();
		map.put("group", "${realm}-group");
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isGroupTemplated()).isTrue();
	}

	@Test
	public void shouldDetectNonTemplatedGroup() {

		// arrange

		var map = minimalConfig();
		map.put("group", "static-group");
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isGroupTemplated()).isFalse();
	}

	// =========================================================================
	// Trimming — Connection String
	// =========================================================================

	@Test
	public void shouldTrimConnectionString() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-webpubsub");
		map.put("connection-string", "  " + VALID_CONNECTION_STRING + "  ");
		map.put("hub", "test-hub");
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionString()).isEqualTo(VALID_CONNECTION_STRING);
	}

	// =========================================================================
	// content-transfer-encoding
	// =========================================================================

	@Test
	public void shouldDefaultContentTransferEncodingToNull() {

		// arrange

		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentTransferEncoding()).isNull();
		assertThat(config.getContentTransferEncodingName()).isNull();
	}

	@Test
	public void shouldResolveContentTransferEncoding() {

		// arrange

		var map = minimalConfig();
		map.put("content-transfer-encoding", "base64");
		var config = new AzureWebPubSubDestinationConfig();
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
		map.put("content-transfer-encoding", "unknown");
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("unknown content-transfer-encoding: unknown");
	}

	// =========================================================================
	// content-encoding
	// =========================================================================

	@Test
	public void shouldDefaultContentEncodingToNull() {

		// arrange

		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentEncoding()).isNull();
		assertThat(config.getContentEncodingName()).isNull();
	}

	@Test
	public void shouldResolveContentEncoding() {

		// arrange

		var map = minimalConfig();
		map.put("content-encoding", "gzip");
		var config = new AzureWebPubSubDestinationConfig();
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
		map.put("content-encoding", "unknown");
		var config = new AzureWebPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("unknown content-encoding: unknown");
	}
}