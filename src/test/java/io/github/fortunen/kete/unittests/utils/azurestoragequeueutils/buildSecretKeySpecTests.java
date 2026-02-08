package io.github.fortunen.kete.unittests.utils.azurestoragequeueutils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.AzureStorageQueueUtils;

public class buildSecretKeySpecTests {

	@Test
	public void shouldReturnHmacSha256SecretKeySpec() {

		// arrange

		var base64Key = "dGVzdGtleQ==";

		// act

		var result = AzureStorageQueueUtils.buildSecretKeySpec(base64Key);

		// assert

		assertThat(result.getAlgorithm()).isEqualTo("HmacSHA256");
	}

	@Test
	public void shouldDecodeBase64KeyIntoBytes() {

		// arrange

		var base64Key = "dGVzdGtleQ=="; // "testkey"

		// act

		var result = AzureStorageQueueUtils.buildSecretKeySpec(base64Key);

		// assert

		assertThat(result.getEncoded()).isEqualTo("testkey".getBytes());
	}

	@Test
	public void shouldProduceDifferentSpecsForDifferentKeys() {

		// arrange

		var key1 = "dGVzdGtleQ=="; // "testkey"
		var key2 = "b3RoZXJrZXk="; // "otherkey"

		// act

		var spec1 = AzureStorageQueueUtils.buildSecretKeySpec(key1);
		var spec2 = AzureStorageQueueUtils.buildSecretKeySpec(key2);

		// assert

		assertThat(spec1.getEncoded()).isNotEqualTo(spec2.getEncoded());
	}

	@Test
	public void shouldWorkWithWellKnownTestKey() {

		// act

		var result = AzureStorageQueueUtils.buildSecretKeySpec(AzureStorageQueueUtils.WELL_KNOWN_ACCOUNT_KEY);

		// assert

		assertThat(result.getEncoded()).hasSize(64);
		assertThat(result.getAlgorithm()).isEqualTo("HmacSHA256");
	}

}
