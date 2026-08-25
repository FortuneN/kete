package io.github.fortunen.kete.unittests.destinationconfigs.gcpcloudtasksdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.gcpcloudtasks.GcpCloudTasksDestinationConfig;

public class initializeTests {

	private Map<String, Object> minimalConfig() {
		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-cloud-tasks");
		map.put("project", "my-project");
		map.put("location", "us-central1");
		map.put("queue", "test-queue");
		map.put("target-url", "http://localhost:8080/handler");
		map.put("endpoint", "localhost:9090");
		return map;
	}

	// =========================================================================
	// Required Fields - Project
	// =========================================================================

	@Test
	public void shouldThrowWhenProjectIsMissing() {

		// arrange

		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-cloud-tasks",
			"location", "us-central1",
			"queue", "test-queue",
			"target-url", "http://localhost:8080/handler",
			"endpoint", "localhost:9090"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("project is required");
	}

	@Test
	public void shouldThrowWhenProjectIsEmpty() {

		// arrange

		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-cloud-tasks",
			"project", "",
			"location", "us-central1",
			"queue", "test-queue",
			"target-url", "http://localhost:8080/handler",
			"endpoint", "localhost:9090"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("project is required");
	}

	@Test
	public void shouldThrowWhenProjectIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-cloud-tasks");
		map.put("project", "   ");
		map.put("location", "us-central1");
		map.put("queue", "test-queue");
		map.put("target-url", "http://localhost:8080/handler");
		map.put("endpoint", "localhost:9090");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("project is required");
	}

	// =========================================================================
	// Required Fields - Location
	// =========================================================================

	@Test
	public void shouldThrowWhenLocationIsMissing() {

		// arrange

		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-cloud-tasks",
			"project", "my-project",
			"queue", "test-queue",
			"target-url", "http://localhost:8080/handler",
			"endpoint", "localhost:9090"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("location is required");
	}

	@Test
	public void shouldThrowWhenLocationIsEmpty() {

		// arrange

		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-cloud-tasks",
			"project", "my-project",
			"location", "",
			"queue", "test-queue",
			"target-url", "http://localhost:8080/handler",
			"endpoint", "localhost:9090"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("location is required");
	}

	// =========================================================================
	// Required Fields - Queue
	// =========================================================================

	@Test
	public void shouldThrowWhenQueueIsMissing() {

		// arrange

		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-cloud-tasks",
			"project", "my-project",
			"location", "us-central1",
			"target-url", "http://localhost:8080/handler",
			"endpoint", "localhost:9090"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("queue is required");
	}

	@Test
	public void shouldThrowWhenQueueIsEmpty() {

		// arrange

		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-cloud-tasks",
			"project", "my-project",
			"location", "us-central1",
			"queue", "",
			"target-url", "http://localhost:8080/handler",
			"endpoint", "localhost:9090"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("queue is required");
	}

	// =========================================================================
	// Required Fields - Target URL
	// =========================================================================

	@Test
	public void shouldThrowWhenTargetUrlIsMissing() {

		// arrange

		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-cloud-tasks",
			"project", "my-project",
			"location", "us-central1",
			"queue", "test-queue",
			"endpoint", "localhost:9090"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("target-url is required and must be a valid absolute URL");
	}

	@Test
	public void shouldThrowWhenTargetUrlIsInvalid() {

		// arrange

		var map = minimalConfig();
		map.put("target-url", "not-a-url");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("target-url is required and must be a valid absolute URL");
	}

	// =========================================================================
	// Defaults
	// =========================================================================

	@Test
	public void shouldDefaultEndpointToGcpCloudTasksApi() {

		// arrange

		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-cloud-tasks",
			"project", "my-project",
			"location", "us-central1",
			"queue", "test-queue",
			"target-url", "http://localhost:8080/handler"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(config.getEndpoint()).isEqualTo("cloudtasks.googleapis.com:443");
		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("credentials are required for cloudtasks.googleapis.com:443");
	}

	@Test
	public void shouldDefaultTimeoutSecondsTo10() {

		// arrange

		var map = minimalConfig();
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(10);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(10));
		assertThat(config.isHasTimeoutSeconds()).isFalse();
	}

	@Test
	public void shouldDefaultHttpMethodToPost() {

		// arrange

		var map = minimalConfig();
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getHttpMethod()).isEqualTo("POST");
	}

	@Test
	public void shouldDefaultAuthToNullWhenNoCredentials() {

		// arrange

		var map = minimalConfig();
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getCredentials()).isNull();
		assertThat(config.isAuthenticated()).isFalse();
	}

	// =========================================================================
	// Custom Configuration
	// =========================================================================

	@Test
	public void shouldUseCustomEndpoint() {

		// arrange

		var map = minimalConfig();
		map.put("endpoint", "localhost:9999");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEndpoint()).isEqualTo("localhost:9999");
	}

	@Test
	public void shouldUseCustomTimeoutSeconds() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 30);
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
	}

	@Test
	public void shouldUseCustomHttpMethod() {

		// arrange

		var map = minimalConfig();
		map.put("http-method", "PUT");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getHttpMethod()).isEqualTo("PUT");
	}

	@Test
	public void shouldUpperCaseHttpMethod() {

		// arrange

		var map = minimalConfig();
		map.put("http-method", "put");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getHttpMethod()).isEqualTo("PUT");
	}

	@Test
	public void shouldSetProjectAndLocationAndQueue() {

		// arrange

		var map = minimalConfig();
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getProject()).isEqualTo("my-project");
		assertThat(config.getLocation()).isEqualTo("us-central1");
		assertThat(config.getQueue()).isEqualTo("test-queue");
	}

	// =========================================================================
	// Timeout Validation
	// =========================================================================

	@Test
	public void shouldThrowWhenTimeoutSecondsIsZero() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 0);
		var config = new GcpCloudTasksDestinationConfig();
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
		var config = new GcpCloudTasksDestinationConfig();
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
	public void shouldDefaultUsePlaintextToFalse() {

		// arrange

		var map = minimalConfig();
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isUsePlaintext()).isFalse();
	}

	@Test
	public void shouldPreComputeDefaultTimeout() {

		// arrange

		var map = minimalConfig();
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(10));
	}

	@Test
	public void shouldPreComputeCustomTimeout() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 30);
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
	}

	@Test
	public void shouldPreComputeAuthenticatedFalseWhenNoCredentials() {

		// arrange

		var map = minimalConfig();
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isAuthenticated()).isFalse();
	}

	// =========================================================================
	// Credentials Required for Default URL
	// =========================================================================

	@Test
	public void shouldThrowWhenDefaultEndpointAndNoCredentials() {

		// arrange

		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-cloud-tasks",
			"project", "my-project",
			"location", "us-central1",
			"queue", "test-queue",
			"target-url", "http://localhost:8080/handler"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("credentials are required for cloudtasks.googleapis.com:443");
	}

	@Test
	public void shouldAllowNoCredentialsWithCustomEndpoint() {

		// arrange

		var map = minimalConfig();
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getCredentials()).isNull();
		assertThat(config.isAuthenticated()).isFalse();
	}

	@Test
	public void shouldUsePlaintextWhenConfigured() {

		// arrange

		var map = minimalConfig();
		map.put("use-plaintext", true);
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isUsePlaintext()).isTrue();
	}

	// =========================================================================
	// Credentials Mutual Exclusivity
	// =========================================================================

	@Test
	public void shouldThrowWhenBothCredentialsFilePathAndBase64AreProvided() {

		// arrange

		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "gcp-cloud-tasks",
			"project", "my-project",
			"location", "us-central1",
			"queue", "test-queue",
			"target-url", "http://localhost:8080/handler",
			"credentials-file-path", "/some/path.json",
			"credentials-file-base64", "dGVzdA=="
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("credentials-file-path, credentials-file-text, and credentials-file-base64 are mutually exclusive");
	}

	@Test
	public void shouldThrowWhenBothCredentialsFilePathAndTextAreProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-cloud-tasks");
		map.put("project", "my-project");
		map.put("location", "us-central1");
		map.put("queue", "test-queue");
		map.put("target-url", "http://localhost:8080/handler");
		map.put("credentials-file-path", "/some/path.json");
		map.put("credentials-file-text", "{\"client_email\":\"x\"}");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("credentials-file-path, credentials-file-text, and credentials-file-base64 are mutually exclusive");
	}

	@Test
	public void shouldThrowWhenBothCredentialsFileTextAndBase64AreProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-cloud-tasks");
		map.put("project", "my-project");
		map.put("location", "us-central1");
		map.put("queue", "test-queue");
		map.put("target-url", "http://localhost:8080/handler");
		map.put("credentials-file-text", "{\"client_email\":\"x\"}");
		map.put("credentials-file-base64", "dGVzdA==");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("credentials-file-path, credentials-file-text, and credentials-file-base64 are mutually exclusive");
	}

	@Test
	public void shouldThrowWhenAllThreeCredentialsSourcesAreProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-cloud-tasks");
		map.put("project", "my-project");
		map.put("location", "us-central1");
		map.put("queue", "test-queue");
		map.put("target-url", "http://localhost:8080/handler");
		map.put("credentials-file-path", "/some/path.json");
		map.put("credentials-file-text", "{\"client_email\":\"x\"}");
		map.put("credentials-file-base64", "dGVzdA==");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("credentials-file-path, credentials-file-text, and credentials-file-base64 are mutually exclusive");
	}

	// =========================================================================
	// Pre-computed Fields - parentPathPrefix
	// =========================================================================

	@Test
	public void shouldPreComputeParentPathPrefix() {

		// arrange

		var map = minimalConfig();
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getParentPathPrefix()).isEqualTo("projects/my-project/locations/us-central1/queues/");
	}

	@Test
	public void shouldPreComputeParentPathPrefixWithDifferentValues() {

		// arrange

		var map = minimalConfig();
		map.put("project", "other-project");
		map.put("location", "europe-west1");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getParentPathPrefix()).isEqualTo("projects/other-project/locations/europe-west1/queues/");
	}

	// =========================================================================
	// Pre-computed Fields - isQueueTemplated
	// =========================================================================

	@Test
	public void shouldSetIsQueueTemplatedFalseForLiteralQueue() {

		// arrange

		var map = minimalConfig();
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isQueueTemplated()).isFalse();
	}

	@Test
	public void shouldSetIsQueueTemplatedTrueForTemplatedQueue() {

		// arrange

		var map = minimalConfig();
		map.put("queue", "${realmId}-queue");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isQueueTemplated()).isTrue();
	}

	// =========================================================================
	// Pre-computed Fields - channelBuilder
	// =========================================================================

	@Test
	public void shouldPreComputeChannelBuilder() {

		// arrange

		var map = minimalConfig();
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getChannelBuilder()).isNotNull();
	}

	// =========================================================================
	// Trimming
	// =========================================================================

	@Test
	public void shouldTrimProject() {

		// arrange

		var map = minimalConfig();
		map.put("project", "  my-project  ");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getProject()).isEqualTo("my-project");
	}

	@Test
	public void shouldTrimLocation() {

		// arrange

		var map = minimalConfig();
		map.put("location", "  us-central1  ");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getLocation()).isEqualTo("us-central1");
	}

	@Test
	public void shouldTrimQueue() {

		// arrange

		var map = minimalConfig();
		map.put("queue", "  test-queue  ");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getQueue()).isEqualTo("test-queue");
	}

	@Test
	public void shouldTrimEndpoint() {

		// arrange

		var map = minimalConfig();
		map.put("endpoint", "  localhost:9090  ");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEndpoint()).isEqualTo("localhost:9090");
	}

	@Test
	public void shouldTrimHttpMethod() {

		// arrange

		var map = minimalConfig();
		map.put("http-method", "  put  ");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getHttpMethod()).isEqualTo("PUT");
	}

	// =========================================================================
	// Blank Validation
	// =========================================================================

	@Test
	public void shouldThrowWhenLocationIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-cloud-tasks");
		map.put("project", "my-project");
		map.put("location", "   ");
		map.put("queue", "test-queue");
		map.put("target-url", "http://localhost:8080/handler");
		map.put("endpoint", "localhost:9090");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("location is required");
	}

	@Test
	public void shouldThrowWhenQueueIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-cloud-tasks");
		map.put("project", "my-project");
		map.put("location", "us-central1");
		map.put("queue", "   ");
		map.put("target-url", "http://localhost:8080/handler");
		map.put("endpoint", "localhost:9090");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("queue is required");
	}

	@Test
	public void shouldThrowWhenTargetUrlIsEmpty() {

		// arrange

		var map = minimalConfig();
		map.put("target-url", "");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("target-url is required and must be a valid absolute URL");
	}

	@Test
	public void shouldThrowWhenTargetUrlIsBlank() {

		// arrange

		var map = minimalConfig();
		map.put("target-url", "   ");
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("target-url is required and must be a valid absolute URL");
	}

	// =========================================================================
	// content-transfer-encoding
	// =========================================================================

	@Test
	public void shouldDefaultContentTransferEncodingToNull() {

		// arrange

		var config = new GcpCloudTasksDestinationConfig();
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
		var config = new GcpCloudTasksDestinationConfig();
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
		var config = new GcpCloudTasksDestinationConfig();
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

		var config = new GcpCloudTasksDestinationConfig();
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
		var config = new GcpCloudTasksDestinationConfig();
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
		var config = new GcpCloudTasksDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("unknown content-encoding: unknown");
	}
}
