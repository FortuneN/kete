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
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "account-key", VALID_ACCOUNT_KEY, "queue", "test-queue")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("account-name is required");
	}

	@Test
	public void shouldThrowWhenAccountNameIsEmpty() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "account-name", "", "account-key", VALID_ACCOUNT_KEY, "queue", "test-queue")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("account-name is required");
	}

	@Test
	public void shouldThrowWhenAccountNameIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
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
	// Required Fields - Authentication
	// =========================================================================

	@Test
	public void shouldThrowWhenNeitherAccountKeyNorSasTokenIsProvided() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "account-name", "myaccount", "queue", "test-queue")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("either account-key, sas-token, or connection-string is required");
	}

	@Test
	public void shouldThrowWhenBothAccountKeyAndSasTokenAreProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("account-name", "myaccount");
		map.put("account-key", VALID_ACCOUNT_KEY);
		map.put("sas-token", "sv=2024-08-04&ss=q&srt=o&sp=a&se=2026-01-01T00:00:00Z&sig=test");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("account-key and sas-token are mutually exclusive");
	}

	@Test
	public void shouldThrowWhenAccountKeyIsEmpty() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("account-name", "myaccount");
		map.put("account-key", "");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("either account-key, sas-token, or connection-string is required");
	}

	// =========================================================================

	@Test
	public void shouldThrowWhenQueueIsMissing() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "account-name", "myaccount", "account-key", VALID_ACCOUNT_KEY)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("queue is required");
	}

	@Test
	public void shouldThrowWhenQueueIsEmpty() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "account-name", "myaccount", "account-key", VALID_ACCOUNT_KEY, "queue", "")));

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
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "account-name", "myaccount", "account-key", VALID_ACCOUNT_KEY, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("https://myaccount.queue.core.windows.net");
	}

	@Test
	public void shouldUseDefaultTimeoutSeconds() {

		// arrange

		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "account-name", "myaccount", "account-key", VALID_ACCOUNT_KEY, "queue", "test-queue")));

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
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "account-name", "myaccount", "account-key", VALID_ACCOUNT_KEY, "queue", "test-queue")));

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
		map.put("kind", "azure-storage-queue");
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
		map.put("kind", "azure-storage-queue");
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
		map.put("kind", "azure-storage-queue");
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
		map.put("kind", "azure-storage-queue");
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
		map.put("kind", "azure-storage-queue");
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
		map.put("kind", "azure-storage-queue");
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
		map.put("kind", "azure-storage-queue");
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
		map.put("kind", "azure-storage-queue");
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
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-storage-queue", "account-name", "myaccount", "account-key", VALID_ACCOUNT_KEY, "queue", "test-queue")));

		// act

		config.initialize();

		// assert

		assertThat(config.getClientBuilder()).isNotNull();
	}

	@Test
	public void shouldTrimAccountName() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
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
		map.put("kind", "azure-storage-queue");
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

	// =========================================================================
	// SAS Token Authentication
	// =========================================================================

	@Test
	public void shouldAcceptSasTokenWithUrl() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("sas-token", "sv=2024-08-04&ss=q&srt=o&sp=a&se=2026-01-01T00:00:00Z&sig=test");
		map.put("url", "https://myaccount.queue.core.windows.net");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isUseSasAuth()).isTrue();
		assertThat(config.getSasToken()).isEqualTo("sv=2024-08-04&ss=q&srt=o&sp=a&se=2026-01-01T00:00:00Z&sig=test");
		assertThat(config.getUrl()).isEqualTo("https://myaccount.queue.core.windows.net");
	}

	@Test
	public void shouldStripLeadingQuestionMarkFromSasToken() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("sas-token", "?sv=2024-08-04&ss=q&srt=o&sp=a&se=2026-01-01T00:00:00Z&sig=test");
		map.put("url", "https://myaccount.queue.core.windows.net");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getSasToken()).isEqualTo("sv=2024-08-04&ss=q&srt=o&sp=a&se=2026-01-01T00:00:00Z&sig=test");
	}

	@Test
	public void shouldAcceptSasTokenWithAccountNameForDefaultUrl() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("account-name", "myaccount");
		map.put("sas-token", "sv=2024-08-04&ss=q&srt=o&sp=a&se=2026-01-01T00:00:00Z&sig=test");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isUseSasAuth()).isTrue();
		assertThat(config.getUrl()).isEqualTo("https://myaccount.queue.core.windows.net");
	}

	@Test
	public void shouldThrowWhenSasTokenWithoutUrlAndWithoutAccountName() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("sas-token", "sv=2024-08-04&ss=q&srt=o&sp=a&se=2026-01-01T00:00:00Z&sig=test");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("url or account-name is required");
	}

	// =========================================================================
	// Validation - URL must contain Account Name
	// =========================================================================

	@Test
	public void shouldThrowWhenUrlDoesNotContainAccountName() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("account-name", "myaccount");
		map.put("account-key", VALID_ACCOUNT_KEY);
		map.put("queue", "test-queue");
		map.put("url", "https://otheraccount.queue.core.windows.net");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("url must contain the account-name");
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

		assertThat(config.isUseSasAuth()).isFalse();
		assertThat(config.getAccountName()).isEqualTo("myaccount");
		assertThat(config.getAccountKey()).isEqualTo(VALID_ACCOUNT_KEY);
		assertThat(config.getUrl()).isEqualTo("https://myaccount.queue.core.windows.net");
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

		assertThat(config.getUrl()).isEqualTo("https://myaccount.queue.core.windows.net");
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

		assertThat(config.getUrl()).isEqualTo("https://myaccount.queue.core.windows.net");
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

		assertThat(config.isUseSasAuth()).isTrue();
		assertThat(config.getSasToken()).isEqualTo("sv=2024-08-04&ss=q&srt=o&sp=a&se=2026-01-01T00:00:00Z&sig=test");
		assertThat(config.getUrl()).isEqualTo("https://myaccount.queue.core.windows.net");
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

		assertThat(config.isUseSasAuth()).isTrue();
		assertThat(config.getUrl()).isEqualTo("https://myaccount.queue.core.windows.net");
	}

	// =========================================================================
	// Connection String - Emulator
	// =========================================================================

	@Test
	public void shouldAcceptEmulatorConnectionString() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "UseDevelopmentStorage=true");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isUseSasAuth()).isFalse();
		assertThat(config.getAccountName()).isEqualTo("devstoreaccount1");
		assertThat(config.getAccountKey()).isEqualTo(VALID_ACCOUNT_KEY);
		assertThat(config.getUrl()).isEqualTo("http://127.0.0.1:10001/devstoreaccount1");
	}

	@Test
	public void shouldAcceptEmulatorConnectionStringWithProxy() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "UseDevelopmentStorage=true;DevelopmentStorageProxyUri=http://myhost");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("http://myhost:10001/devstoreaccount1");
	}

	@Test
	public void shouldStripTrailingSlashFromEmulatorProxyUri() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "UseDevelopmentStorage=true;DevelopmentStorageProxyUri=http://myhost/");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("http://myhost:10001/devstoreaccount1");
	}

	// =========================================================================
	// Connection String - Mutual Exclusivity
	// =========================================================================

	@Test
	public void shouldThrowWhenConnectionStringAndAccountNameBothProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "UseDevelopmentStorage=true");
		map.put("account-name", "myaccount");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string and account-name are mutually exclusive");
	}

	@Test
	public void shouldThrowWhenConnectionStringAndAccountKeyBothProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "UseDevelopmentStorage=true");
		map.put("account-key", VALID_ACCOUNT_KEY);
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string and account-key are mutually exclusive");
	}

	@Test
	public void shouldThrowWhenConnectionStringAndSasTokenBothProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "UseDevelopmentStorage=true");
		map.put("sas-token", "sv=2024-08-04&sig=test");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string and sas-token are mutually exclusive");
	}

	@Test
	public void shouldThrowWhenConnectionStringAndUrlBothProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "UseDevelopmentStorage=true");
		map.put("url", "https://myaccount.queue.core.windows.net");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string and url are mutually exclusive");
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

	@Test
	public void shouldAcceptEmulatorFullConnectionString() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=" + VALID_ACCOUNT_KEY + ";QueueEndpoint=http://127.0.0.1:10001/devstoreaccount1");
		map.put("queue", "test-queue");
		var config = new AzureStorageQueueDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isUseSasAuth()).isFalse();
		assertThat(config.getAccountName()).isEqualTo("devstoreaccount1");
		assertThat(config.getUrl()).isEqualTo("http://127.0.0.1:10001/devstoreaccount1");
	}
}
