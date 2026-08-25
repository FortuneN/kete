package io.github.fortunen.kete.unittests.destinationconfigs.gcppubsubdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.gcppubsub.GcpPubSubDestinationConfig;

public class initializeTests {

	// =========================================================================
	// Required Fields - Project
	// =========================================================================

	@Test
	public void shouldThrowWhenProjectIsMissing() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"topic", "test-topic"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("project is required");
	}

	@Test
	public void shouldThrowWhenProjectIsEmpty() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "",
			"topic", "test-topic"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("project is required");
	}

	@Test
	public void shouldThrowWhenProjectIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-pubsub");
		map.put("project", "   ");
		map.put("topic", "test-topic");
		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("project is required");
	}

	// =========================================================================
	// Required Fields - Topic
	// =========================================================================

	@Test
	public void shouldThrowWhenTopicIsMissing() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("topic is required");
	}

	@Test
	public void shouldThrowWhenTopicIsEmpty() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", ""
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("topic is required");
	}

	@Test
	public void shouldThrowWhenTopicIsBlank() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "   "
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("topic is required");
	}

	// =========================================================================
	// Defaults
	// =========================================================================

	@Test
	public void shouldDefaultUrlToGcpPubSubApi() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(config.getUrl()).isEqualTo("https://pubsub.googleapis.com");
		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials are required for https://pubsub.googleapis.com");
	}

	@Test
	public void shouldDefaultTimeoutSecondsTo10() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"url", "http://localhost:8085"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(10);
	}

	@Test
	public void shouldDefaultAuthToNullWhenNoCredentials() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"url", "http://localhost:8085"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getCredentials()).isNull();
		assertThat(config.isAuthenticated()).isFalse();
	}

	@Test
	public void shouldDefaultOrderingKeyToEmpty() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"url", "http://localhost:8085"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getOrderingKey()).isEmpty();
	}

	// =========================================================================
	// Custom Configuration
	// =========================================================================

	@Test
	public void shouldUseCustomUrl() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"url", "http://localhost:8085"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("http://localhost:8085");
	}

	@Test
	public void shouldUseCustomTimeoutSeconds() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"url", "http://localhost:8085",
			"timeout-seconds", "30"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
		assertThat(config.isHasTimeoutSeconds()).isTrue();
	}

	@Test
	public void shouldNotFlagTimeoutSecondsWhenNotConfigured() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"url", "http://localhost:8085"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(10);
		assertThat(config.isHasTimeoutSeconds()).isFalse();
	}

	@Test
	public void shouldUseCustomOrderingKey() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"url", "http://localhost:8085",
			"ordering-key", "my-key"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getOrderingKey()).isEqualTo("my-key");
	}

	@Test
	public void shouldSetProjectAndTopic() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-gcp-project",
			"topic", "keycloak-events",
			"url", "http://localhost:8085"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProject()).isEqualTo("my-gcp-project");
		assertThat(config.getTopic()).isEqualTo("keycloak-events");
	}

	@Test
	public void shouldSupportTopicTemplates() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "events-${realmLowerCase}",
			"url", "http://localhost:8085"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTopic()).isEqualTo("events-${realmLowerCase}");
	}

	@Test
	public void shouldCreateHttpTransport() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"url", "http://localhost:8085"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getHttpTransport()).isNotNull();
	}

	// =========================================================================
	// URL Validation
	// =========================================================================

	@Test
	public void shouldThrowWhenUrlIsInvalid() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"url", "not-a-url"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("url must be a valid absolute URL");
	}

	// =========================================================================
	// Timeout Validation
	// =========================================================================

	@Test
	public void shouldThrowWhenTimeoutSecondsIsZero() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"url", "http://localhost:8085",
			"timeout-seconds", "0"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("timeout-seconds must be positive");
	}

	@Test
	public void shouldThrowWhenTimeoutSecondsIsNegative() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"url", "http://localhost:8085",
			"timeout-seconds", "-5"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("timeout-seconds must be positive");
	}

	// =========================================================================
	// Pre-computed Fields
	// =========================================================================

	@Test
	public void shouldPreComputeDefaultTimeout() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"url", "http://localhost:8085"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(10));
	}

	@Test
	public void shouldPreComputeCustomTimeout() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"url", "http://localhost:8085",
			"timeout-seconds", "30"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
	}

	@Test
	public void shouldPreComputeAuthenticatedFalseWhenNoCredentials() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"url", "http://localhost:8085"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isAuthenticated()).isFalse();
	}

	// =========================================================================
	// Credentials Required for Default URL
	// =========================================================================

	@Test
	public void shouldThrowWhenDefaultUrlAndNoCredentials() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials are required for https://pubsub.googleapis.com");
	}

	@Test
	public void shouldAllowNoCredentialsWithCustomUrl() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"url", "http://localhost:8085"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getCredentials()).isNull();
		assertThat(config.isAuthenticated()).isFalse();
	}

	// =========================================================================
	// Credentials Mutual Exclusivity
	// =========================================================================

	@Test
	public void shouldThrowWhenBothCredentialsFilePathAndBase64AreProvided() {

		// arrange

		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-pubsub",
			"project", "my-project",
			"topic", "my-topic",
			"credentials-file-path", "/some/path.json",
			"credentials-file-base64", "dGVzdA=="
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials-file-path, credentials-file-text, and credentials-file-base64 are mutually exclusive");
	}

	@Test
	public void shouldThrowWhenBothCredentialsFilePathAndTextAreProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-pubsub");
		map.put("project", "my-project");
		map.put("topic", "my-topic");
		map.put("credentials-file-path", "/some/path.json");
		map.put("credentials-file-text", "{\"client_email\":\"x\"}");
		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials-file-path, credentials-file-text, and credentials-file-base64 are mutually exclusive");
	}

	@Test
	public void shouldThrowWhenBothCredentialsFileTextAndBase64AreProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-pubsub");
		map.put("project", "my-project");
		map.put("topic", "my-topic");
		map.put("credentials-file-text", "{\"client_email\":\"x\"}");
		map.put("credentials-file-base64", "dGVzdA==");
		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials-file-path, credentials-file-text, and credentials-file-base64 are mutually exclusive");
	}

	@Test
	public void shouldThrowWhenAllThreeCredentialsSourcesAreProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-pubsub");
		map.put("project", "my-project");
		map.put("topic", "my-topic");
		map.put("credentials-file-path", "/some/path.json");
		map.put("credentials-file-text", "{\"client_email\":\"x\"}");
		map.put("credentials-file-base64", "dGVzdA==");
		var config = new GcpPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials-file-path, credentials-file-text, and credentials-file-base64 are mutually exclusive");
	}
}
