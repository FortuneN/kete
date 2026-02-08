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

	// =========================================================================
	// Required Fields - Account Name
	// =========================================================================

	@Test
	public void shouldThrowWhenAccountNameIsMissing() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queues", "account-key", VALID_ACCOUNT_KEY, "queue", "test-queue")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("account-name is required");
	}

	@Test
	public void shouldThrowWhenAccountNameIsEmpty() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queues", "account-name", "", "account-key", VALID_ACCOUNT_KEY, "queue", "test-queue")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("account-name is required");
	}

	@Test
	public void shouldThrowWhenAccountNameIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queues");
		map.put("account-name", "   ");
		map.put("account-key", VALID_ACCOUNT_KEY);
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("account-name is required");
	}

	// =========================================================================
	// Required Fields - Account Key
	// =========================================================================

	@Test
	public void shouldThrowWhenAccountKeyIsMissing() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queues", "account-name", "myaccount", "queue", "test-queue")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("account-key is required");
	}

	@Test
	public void shouldThrowWhenAccountKeyIsEmpty() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queues", "account-name", "myaccount", "account-key", "", "queue", "test-queue")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("account-key is required");
	}

	// =========================================================================
	// Required Fields - Queue
	// =========================================================================

	@Test
	public void shouldThrowWhenQueueIsMissing() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queues", "account-name", "myaccount", "account-key", VALID_ACCOUNT_KEY)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("queue is required");
	}

	@Test
	public void shouldThrowWhenQueueIsEmpty() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queues", "account-name", "myaccount", "account-key", VALID_ACCOUNT_KEY, "queue", "")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("queue is required");
	}

	// =========================================================================
	// Defaults
	// =========================================================================

	@Test
	public void shouldUseDefaultUrlFromAccountName() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queues", "account-name", "myaccount", "account-key", VALID_ACCOUNT_KEY, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("https://myaccount.queue.core.windows.net");
	}

	@Test
	public void shouldUseDefaultTimeoutSeconds() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queues", "account-name", "myaccount", "account-key", VALID_ACCOUNT_KEY, "queue", "test-queue")));

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
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queues", "account-name", "myaccount", "account-key", VALID_ACCOUNT_KEY, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getMessageTtl()).isEqualTo(0);
	}

	// =========================================================================
	// Custom Configuration
	// =========================================================================

	@Test
	public void shouldUseCustomUrl() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queues");
		map.put("account-name", "devstoreaccount1");
		map.put("account-key", VALID_ACCOUNT_KEY);
		map.put("queue", "test-queue");
		map.put("url", "http://localhost:10001/devstoreaccount1");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("http://localhost:10001/devstoreaccount1");
	}

	@Test
	public void shouldStripTrailingSlashFromUrl() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queues");
		map.put("account-name", "devstoreaccount1");
		map.put("account-key", VALID_ACCOUNT_KEY);
		map.put("queue", "test-queue");
		map.put("url", "http://localhost:10001/devstoreaccount1/");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("http://localhost:10001/devstoreaccount1");
	}

	@Test
	public void shouldUseCustomTimeoutSeconds() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queues");
		map.put("account-name", "myaccount");
		map.put("account-key", VALID_ACCOUNT_KEY);
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
		map.put("kind", "azure-storage-queues");
		map.put("account-name", "myaccount");
		map.put("account-key", VALID_ACCOUNT_KEY);
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
		map.put("kind", "azure-storage-queues");
		map.put("account-name", "myaccount");
		map.put("account-key", VALID_ACCOUNT_KEY);
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
		map.put("kind", "azure-storage-queues");
		map.put("account-name", "myaccount");
		map.put("account-key", VALID_ACCOUNT_KEY);
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
		map.put("kind", "azure-storage-queues");
		map.put("account-name", "myaccount");
		map.put("account-key", VALID_ACCOUNT_KEY);
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
	// Validation - URL
	// =========================================================================

	@Test
	public void shouldThrowWhenUrlIsInvalid() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queues");
		map.put("account-name", "myaccount");
		map.put("account-key", VALID_ACCOUNT_KEY);
		map.put("queue", "test-queue");
		map.put("url", "not-a-valid-url");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("url must be a valid absolute URL");
	}

	// =========================================================================
	// Pre-computed Fields
	// =========================================================================

	@Test
	public void shouldSetClientBuilder() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queues", "account-name", "myaccount", "account-key", VALID_ACCOUNT_KEY, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getClientBuilder()).isNotNull();
	}

	@Test
	public void shouldTrimAccountName() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queues");
		map.put("account-name", "  myaccount  ");
		map.put("account-key", VALID_ACCOUNT_KEY);
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getAccountName()).isEqualTo("myaccount");
	}

	@Test
	public void shouldTrimQueue() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queues");
		map.put("account-name", "myaccount");
		map.put("account-key", VALID_ACCOUNT_KEY);
		map.put("queue", "  test-queue  ");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getQueue()).isEqualTo("test-queue");
	}
}
