package io.github.fortunen.kete.unittests.destinationconfigs.azurestoragequeuedestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.azurestoragequeue.AzureStorageQueueDestinationConfig;

public class initializeTests {

	private static final String VALID_ACCOUNT_KEY = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";
	private static final String SHARED_KEY_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=myaccount;AccountKey=" + VALID_ACCOUNT_KEY + ";EndpointSuffix=core.windows.net";

	// =========================================================================
	// Required Fields - Connection String
	// =========================================================================

	@Test
	public void shouldThrowWhenConnectionStringIsMissing() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "queue", "test-queue")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'connection-string' or 'authentication-type' must be set");
	}

	@Test
	public void shouldThrowWhenConnectionStringIsEmpty() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", "", "queue", "test-queue")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'connection-string' or 'authentication-type' must be set");
	}

	@Test
	public void shouldThrowWhenConnectionStringIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "   ");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'connection-string' or 'authentication-type' must be set");
	}

	// =========================================================================
	// Required Fields - Queue
	// =========================================================================

	@Test
	public void shouldThrowWhenQueueIsMissing() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", SHARED_KEY_CONNECTION_STRING)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("queue is required");
	}

	@Test
	public void shouldThrowWhenQueueIsEmpty() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", SHARED_KEY_CONNECTION_STRING, "queue", "")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("queue is required");
	}

	@Test
	public void shouldThrowWhenQueueIsBlank() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", SHARED_KEY_CONNECTION_STRING, "queue", "   ")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("queue is required");
	}

	// =========================================================================
	// Defaults
	// =========================================================================

	@Test
	public void shouldUseDefaultTimeoutSeconds() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", SHARED_KEY_CONNECTION_STRING, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(10);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(10));
	}

	@Test
	public void shouldUseDefaultMessageTtl() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", SHARED_KEY_CONNECTION_STRING, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getMessageTtl()).isEqualTo(0);
	}

	// =========================================================================
	// Custom Configuration
	// =========================================================================

	@Test
	public void shouldUseCustomTimeoutSeconds() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", SHARED_KEY_CONNECTION_STRING);
		map.put("queue", "test-queue");
		map.put("timeout-seconds", 30);
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
	}

	@Test
	public void shouldUseCustomMessageTtl() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", SHARED_KEY_CONNECTION_STRING);
		map.put("queue", "test-queue");
		map.put("message-ttl", -1);
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getMessageTtl()).isEqualTo(-1);
	}

	// =========================================================================
	// Validation - Timeout
	// =========================================================================

	@Test
	public void shouldThrowWhenTimeoutSecondsIsZero() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", SHARED_KEY_CONNECTION_STRING);
		map.put("queue", "test-queue");
		map.put("timeout-seconds", 0);
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("timeout-seconds must be positive");
	}

	@Test
	public void shouldThrowWhenTimeoutSecondsIsNegative() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", SHARED_KEY_CONNECTION_STRING);
		map.put("queue", "test-queue");
		map.put("timeout-seconds", -1);
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("timeout-seconds must be positive");
	}

	// =========================================================================
	// Validation - Message TTL
	// =========================================================================

	@Test
	public void shouldThrowWhenMessageTtlIsLessThanMinusOne() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", SHARED_KEY_CONNECTION_STRING);
		map.put("queue", "test-queue");
		map.put("message-ttl", -2);
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("message-ttl must be -1 or greater");
	}

	// =========================================================================
	// Pre-computed Fields
	// =========================================================================

	@Test
	public void shouldSetHttpClient() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", SHARED_KEY_CONNECTION_STRING, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getHttpClient()).isNotNull();
	}

	@Test
	public void shouldSetConnectionString() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", SHARED_KEY_CONNECTION_STRING, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionString()).isEqualTo(SHARED_KEY_CONNECTION_STRING);
	}

	@Test
	public void shouldTrimQueue() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", SHARED_KEY_CONNECTION_STRING);
		map.put("queue", "  test-queue  ");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getQueue()).isEqualTo("test-queue");
	}

	// =========================================================================
	// Pre-computed Fields — Templating
	// =========================================================================

	@Test
	public void shouldDetectTemplatedQueue() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", SHARED_KEY_CONNECTION_STRING, "queue", "events-${realm}")));

		// act

		config.initialize();

		// assert

		assertThat(config.isQueueTemplated()).isTrue();
	}

	@Test
	public void shouldDetectNonTemplatedQueue() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", SHARED_KEY_CONNECTION_STRING, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.isQueueTemplated()).isFalse();
	}

	// =========================================================================
	// Pre-computed Fields — Message TTL Duration
	// =========================================================================

	@Test
	public void shouldSetMessageTtlDurationToNullWhenDefault() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", SHARED_KEY_CONNECTION_STRING, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getMessageTtlDuration()).isNull();
	}

	@Test
	public void shouldSetMessageTtlDurationWhenMinusOne() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", SHARED_KEY_CONNECTION_STRING);
		map.put("queue", "test-queue");
		map.put("message-ttl", -1);
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getMessageTtlDuration()).isEqualTo(Duration.ofSeconds(-1));
	}

	@Test
	public void shouldSetMessageTtlDurationWhenPositive() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", SHARED_KEY_CONNECTION_STRING);
		map.put("queue", "test-queue");
		map.put("message-ttl", 3600);
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getMessageTtlDuration()).isEqualTo(Duration.ofSeconds(3600));
	}

	// =========================================================================
	// Pre-computed Fields — Queue Client Builder
	// =========================================================================

	@Test
	public void shouldSetQueueClientBuilder() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", SHARED_KEY_CONNECTION_STRING, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getQueueClientBuilder()).isNotNull();
	}

	// =========================================================================
	// Pre-computed Fields — Queue Service Endpoint
	// =========================================================================

	@Test
	public void shouldDeriveQueueServiceEndpointFromAccountName() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", SHARED_KEY_CONNECTION_STRING, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getQueueServiceEndpoint()).isEqualTo("https://myaccount.queue.core.windows.net");
	}

	@Test
	public void shouldUseExplicitQueueEndpoint() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", "QueueEndpoint=http://127.0.0.1:10001/devstoreaccount1;AccountName=devstoreaccount1;AccountKey=" + VALID_ACCOUNT_KEY, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getQueueServiceEndpoint()).isEqualTo("http://127.0.0.1:10001/devstoreaccount1");
	}

	@Test
	public void shouldDeriveQueueServiceEndpointWithCustomSuffix() {

		// arrange

		var connectionString = "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=" + VALID_ACCOUNT_KEY + ";EndpointSuffix=localhost:10001";
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", connectionString, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getQueueServiceEndpoint()).isEqualTo("http://devstoreaccount1.queue.localhost:10001");
	}

	@Test
	public void shouldThrowWhenConnectionStringMissingAccountNameAndQueueEndpoint() {

		// arrange

		var connectionString = "DefaultEndpointsProtocol=https;SharedAccessSignature=sv=2019-07-07&ss=q&srt=sco&sp=rwdlacup&se=2030-01-01T00:00:00Z&st=2020-01-01T00:00:00Z&spr=https&sig=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%3D";
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", connectionString, "queue", "test-queue")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert (Azure SDK validates connection string before our endpoint parsing)

		assertThat(thrown).isNotNull();
	}

	// =========================================================================
	// Trimming — Connection String
	// =========================================================================

	@Test
	public void shouldTrimConnectionString() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "  " + SHARED_KEY_CONNECTION_STRING + "  ");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionString()).isEqualTo(SHARED_KEY_CONNECTION_STRING);
	}

	// =========================================================================
	// content-transfer-encoding
	// =========================================================================

	@Test
	public void shouldDefaultContentTransferEncodingToNull() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", SHARED_KEY_CONNECTION_STRING, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentTransferEncoding()).isNull();
		assertThat(config.getContentTransferEncodingName()).isNull();
	}

	@Test
	public void shouldResolveContentTransferEncoding() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", SHARED_KEY_CONNECTION_STRING);
		map.put("queue", "test-queue");
		map.put("content-transfer-encoding", "base64");
		var config = new AzureStorageQueueDestinationConfig();
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

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", SHARED_KEY_CONNECTION_STRING);
		map.put("queue", "test-queue");
		map.put("content-transfer-encoding", "unknown");
		var config = new AzureStorageQueueDestinationConfig();
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

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", SHARED_KEY_CONNECTION_STRING, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentEncoding()).isNull();
		assertThat(config.getContentEncodingName()).isNull();
	}

	@Test
	public void shouldResolveContentEncoding() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", SHARED_KEY_CONNECTION_STRING);
		map.put("queue", "test-queue");
		map.put("content-encoding", "gzip");
		var config = new AzureStorageQueueDestinationConfig();
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

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", SHARED_KEY_CONNECTION_STRING);
		map.put("queue", "test-queue");
		map.put("content-encoding", "unknown");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("unknown content-encoding: unknown");
	}
}
