package io.github.fortunen.kete.unittests.destinationconfigs.azurestoragequeuedestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.azurestoragequeue.AzureStorageQueueDestinationConfig;
import io.github.fortunen.kete.utils.AzureStorageQueueUtils;

public class initializeTests {

	private static final String VALID_ACCOUNT_KEY = AzureStorageQueueUtils.WELL_KNOWN_ACCOUNT_KEY;
	private static final String SHARED_KEY_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=myaccount;AccountKey=" + VALID_ACCOUNT_KEY + ";EndpointSuffix=core.windows.net";
	private static final String EMULATOR_CONNECTION_STRING = "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=" + VALID_ACCOUNT_KEY + ";QueueEndpoint=http://127.0.0.1:10001/devstoreaccount1";

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

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string is required");
	}

	@Test
	public void shouldThrowWhenConnectionStringIsEmpty() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", "", "queue", "test-queue")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string is required");
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

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string is required");
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
	public void shouldSetClientBuilder() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "connection-string", SHARED_KEY_CONNECTION_STRING, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getClientBuilder()).isNotNull();
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
	// Connection String - Shared Key
	// =========================================================================

	@Test
	public void shouldAcceptSharedKeyConnectionString() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "DefaultEndpointsProtocol=https;AccountName=myaccount;AccountKey=" + VALID_ACCOUNT_KEY + ";EndpointSuffix=core.windows.net");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		var info = config.getConnectionStringInfo();
		assertThat(info.useSasAuth()).isFalse();
		assertThat(info.accountName()).isEqualTo("myaccount");
		assertThat(info.accountKey()).isEqualTo(VALID_ACCOUNT_KEY);
		assertThat(info.url()).isEqualTo("https://myaccount.queue.core.windows.net");
	}

	@Test
	public void shouldAcceptSharedKeyConnectionStringWithExplicitQueueEndpoint() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "AccountName=myaccount;AccountKey=" + VALID_ACCOUNT_KEY + ";QueueEndpoint=https://myaccount.queue.core.windows.net");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionStringInfo().url()).isEqualTo("https://myaccount.queue.core.windows.net");
	}

	@Test
	public void shouldDefaultProtocolToHttpsInConnectionString() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "AccountName=myaccount;AccountKey=" + VALID_ACCOUNT_KEY);
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionStringInfo().url()).isEqualTo("https://myaccount.queue.core.windows.net");
	}

	// =========================================================================
	// Connection String - SAS
	// =========================================================================

	@Test
	public void shouldAcceptSasConnectionString() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "QueueEndpoint=https://myaccount.queue.core.windows.net;SharedAccessSignature=sv=2024-08-04&ss=q&srt=o&sp=a&se=2026-01-01T00:00:00Z&sig=test");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		var info = config.getConnectionStringInfo();
		assertThat(info.useSasAuth()).isTrue();
		assertThat(info.sasToken()).isEqualTo("sv=2024-08-04&ss=q&srt=o&sp=a&se=2026-01-01T00:00:00Z&sig=test");
		assertThat(info.url()).isEqualTo("https://myaccount.queue.core.windows.net");
	}

	@Test
	public void shouldAcceptSasConnectionStringWithAccountName() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "AccountName=myaccount;SharedAccessSignature=sv=2024-08-04&ss=q&sig=test");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		var info = config.getConnectionStringInfo();
		assertThat(info.useSasAuth()).isTrue();
		assertThat(info.url()).isEqualTo("https://myaccount.queue.core.windows.net");
	}

	// =========================================================================
	// Connection String - Validation
	// =========================================================================

	@Test
	public void shouldThrowWhenConnectionStringHasNeitherKeyNorSas() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "AccountName=myaccount");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string must contain AccountKey or SharedAccessSignature");
	}

	@Test
	public void shouldThrowWhenConnectionStringHasBothKeyAndSas() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "AccountName=myaccount;AccountKey=" + VALID_ACCOUNT_KEY + ";SharedAccessSignature=sv=2024-08-04&sig=test");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string must not contain both AccountKey and SharedAccessSignature");
	}

	@Test
	public void shouldThrowWhenConnectionStringMissingAccountNameForSharedKey() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "AccountKey=" + VALID_ACCOUNT_KEY);
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string must contain AccountName for shared-key auth");
	}

	@Test
	public void shouldThrowWhenConnectionStringQueueEndpointMismatchesAccountName() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "AccountName=myaccount;AccountKey=" + VALID_ACCOUNT_KEY + ";QueueEndpoint=https://otheraccount.queue.core.windows.net");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string QueueEndpoint must contain the AccountName");
	}

	@Test
	public void shouldThrowWhenSasConnectionStringMissingEndpointAndAccountName() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "SharedAccessSignature=sv=2024-08-04&sig=test");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string must contain AccountName or QueueEndpoint");
	}

	// =========================================================================
	// Connection String - Emulator
	// =========================================================================

	@Test
	public void shouldAcceptEmulatorFullConnectionString() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", EMULATOR_CONNECTION_STRING);
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		var info = config.getConnectionStringInfo();
		assertThat(info.useSasAuth()).isFalse();
		assertThat(info.accountName()).isEqualTo("devstoreaccount1");
		assertThat(info.url()).isEqualTo("http://127.0.0.1:10001/devstoreaccount1");
	}

	// =========================================================================
	// URL Handling
	// =========================================================================

	@Test
	public void shouldStripTrailingSlashFromUrl() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "AccountName=myaccount;AccountKey=" + VALID_ACCOUNT_KEY + ";QueueEndpoint=https://myaccount.queue.core.windows.net/");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionStringInfo().url()).isEqualTo("https://myaccount.queue.core.windows.net");
	}

	@Test
	public void shouldDeriveUrlFromConnectionStringAccountName() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "DefaultEndpointsProtocol=https;AccountName=myaccount;AccountKey=" + VALID_ACCOUNT_KEY);
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionStringInfo().url()).isEqualTo("https://myaccount.queue.core.windows.net");
	}
}
