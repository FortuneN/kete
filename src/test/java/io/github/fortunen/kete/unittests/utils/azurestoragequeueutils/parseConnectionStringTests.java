package io.github.fortunen.kete.unittests.utils.azurestoragequeueutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.AzureStorageQueueUtils;

public class parseConnectionStringTests {

	// =========================================================================
	// SharedKey — AccountName + AccountKey
	// =========================================================================

	@Test
	public void shouldParseSharedKeyConnectionString() {

		// arrange

		var connectionString = "AccountName=myaccount;AccountKey=dGVzdGtleQ==;DefaultEndpointsProtocol=https;EndpointSuffix=core.windows.net";

		// act

		var result = AzureStorageQueueUtils.parseConnectionString(connectionString);

		// assert

		assertThat(result.accountName()).isEqualTo("myaccount");
		assertThat(result.accountKey()).isEqualTo("dGVzdGtleQ==");
		assertThat(result.sasToken()).isNull();
		assertThat(result.url()).isEqualTo("https://myaccount.queue.core.windows.net");
		assertThat(result.useSasAuth()).isFalse();
	}

	@Test
	public void shouldDefaultToHttpsAndCoreWindowsNet() {

		// arrange

		var connectionString = "AccountName=myaccount;AccountKey=dGVzdGtleQ==";

		// act

		var result = AzureStorageQueueUtils.parseConnectionString(connectionString);

		// assert

		assertThat(result.url()).isEqualTo("https://myaccount.queue.core.windows.net");
	}

	@Test
	public void shouldUseHttpProtocolWhenSpecified() {

		// arrange

		var connectionString = "AccountName=myaccount;AccountKey=dGVzdGtleQ==;DefaultEndpointsProtocol=http";

		// act

		var result = AzureStorageQueueUtils.parseConnectionString(connectionString);

		// assert

		assertThat(result.url()).isEqualTo("http://myaccount.queue.core.windows.net");
	}

	@Test
	public void shouldUseCustomEndpointSuffix() {

		// arrange

		var connectionString = "AccountName=myaccount;AccountKey=dGVzdGtleQ==;EndpointSuffix=core.chinacloudapi.cn";

		// act

		var result = AzureStorageQueueUtils.parseConnectionString(connectionString);

		// assert

		assertThat(result.url()).isEqualTo("https://myaccount.queue.core.chinacloudapi.cn");
	}

	@Test
	public void shouldUseQueueEndpointWhenProvided() {

		// arrange

		var connectionString = "AccountName=myaccount;AccountKey=dGVzdGtleQ==;QueueEndpoint=https://myaccount.queue.core.windows.net";

		// act

		var result = AzureStorageQueueUtils.parseConnectionString(connectionString);

		// assert

		assertThat(result.url()).isEqualTo("https://myaccount.queue.core.windows.net");
	}

	// =========================================================================
	// SAS — SharedAccessSignature
	// =========================================================================

	@Test
	public void shouldParseSasConnectionString() {

		// arrange

		var connectionString = "SharedAccessSignature=sv=2024-08-04&ss=q&srt=sco&sp=rwdlacup&se=2030-01-01&sig=abc;QueueEndpoint=https://myaccount.queue.core.windows.net";

		// act

		var result = AzureStorageQueueUtils.parseConnectionString(connectionString);

		// assert

		assertThat(result.sasToken()).isEqualTo("sv=2024-08-04&ss=q&srt=sco&sp=rwdlacup&se=2030-01-01&sig=abc");
		assertThat(result.accountKey()).isNull();
		assertThat(result.url()).isEqualTo("https://myaccount.queue.core.windows.net");
		assertThat(result.useSasAuth()).isTrue();
	}

	@Test
	public void shouldStripLeadingQuestionMarkFromSasToken() {

		// arrange

		var connectionString = "SharedAccessSignature=?sv=2024-08-04&sig=abc;QueueEndpoint=https://myaccount.queue.core.windows.net";

		// act

		var result = AzureStorageQueueUtils.parseConnectionString(connectionString);

		// assert

		assertThat(result.sasToken()).isEqualTo("sv=2024-08-04&sig=abc");
	}

	@Test
	public void shouldParseSasWithAccountName() {

		// arrange

		var connectionString = "AccountName=myaccount;SharedAccessSignature=sv=2024-08-04&sig=abc;QueueEndpoint=https://myaccount.queue.core.windows.net";

		// act

		var result = AzureStorageQueueUtils.parseConnectionString(connectionString);

		// assert

		assertThat(result.accountName()).isEqualTo("myaccount");
		assertThat(result.useSasAuth()).isTrue();
	}

	// =========================================================================
	// Validation — errors
	// =========================================================================

	@Test
	public void shouldThrowWhenNoAuthInConnectionString() {

		// arrange

		var connectionString = "AccountName=myaccount;QueueEndpoint=https://myaccount.queue.core.windows.net";

		// act

		var thrown = catchThrowable(() -> AzureStorageQueueUtils.parseConnectionString(connectionString));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string must contain AccountKey or SharedAccessSignature");
	}

	@Test
	public void shouldThrowWhenBothAccountKeyAndSas() {

		// arrange

		var connectionString = "AccountName=myaccount;AccountKey=dGVzdGtleQ==;SharedAccessSignature=sv=2024-08-04&sig=abc";

		// act

		var thrown = catchThrowable(() -> AzureStorageQueueUtils.parseConnectionString(connectionString));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string must not contain both AccountKey and SharedAccessSignature");
	}

	@Test
	public void shouldThrowWhenSharedKeyWithoutAccountName() {

		// arrange

		var connectionString = "AccountKey=dGVzdGtleQ==;QueueEndpoint=https://myaccount.queue.core.windows.net";

		// act

		var thrown = catchThrowable(() -> AzureStorageQueueUtils.parseConnectionString(connectionString));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string must contain AccountName for shared-key auth");
	}

	@Test
	public void shouldThrowWhenNoAccountNameAndNoQueueEndpoint() {

		// arrange

		var connectionString = "AccountKey=dGVzdGtleQ==";

		// act

		var thrown = catchThrowable(() -> AzureStorageQueueUtils.parseConnectionString(connectionString));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string must contain AccountName for shared-key auth");
	}

	@Test
	public void shouldThrowWhenQueueEndpointDoesNotContainAccountName() {

		// arrange

		var connectionString = "AccountName=myaccount;AccountKey=dGVzdGtleQ==;QueueEndpoint=https://otheraccount.queue.core.windows.net";

		// act

		var thrown = catchThrowable(() -> AzureStorageQueueUtils.parseConnectionString(connectionString));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string QueueEndpoint must contain the AccountName");
	}

	@Test
	public void shouldThrowWhenSasWithoutQueueEndpointAndWithoutAccountName() {

		// arrange

		var connectionString = "SharedAccessSignature=sv=2024-08-04&sig=abc";

		// act

		var thrown = catchThrowable(() -> AzureStorageQueueUtils.parseConnectionString(connectionString));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connection-string must contain AccountName or QueueEndpoint");
	}

	@Test
	public void shouldThrowForNullConnectionString() {

		// act

		var thrown = catchThrowable(() -> AzureStorageQueueUtils.parseConnectionString(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connectionString is required");
	}

	@Test
	public void shouldThrowForEmptyConnectionString() {

		// act

		var thrown = catchThrowable(() -> AzureStorageQueueUtils.parseConnectionString(""));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connectionString is required");
	}

	@Test
	public void shouldThrowForBlankConnectionString() {

		// act

		var thrown = catchThrowable(() -> AzureStorageQueueUtils.parseConnectionString("   "));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connectionString is required");
	}
}
