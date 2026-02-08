package io.github.fortunen.kete.unittests.utils.azurestoragequeueutilsutils;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.AzureStorageQueueUtils;

public class computeSignatureTests {

	private static final String TEST_KEY_BASE64 = Base64.getEncoder().encodeToString("test-secret-key-1234567890".getBytes(StandardCharsets.UTF_8));

	@Test
	public void shouldComputeBase64EncodedHmacSha256() {

		// arrange

		var secretKeySpec = AzureStorageQueueUtils.buildSecretKeySpec(TEST_KEY_BASE64);
		var stringToSign = "POST\n\n\n100\n\napplication/xml\n\n\n\n\n\nx-ms-date:Mon, 01 Jan 2024 00:00:00 GMT\nx-ms-version:2024-08-04\n/acc/q/messages";

		// act

		var result = AzureStorageQueueUtils.computeSignature(secretKeySpec, stringToSign);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isNotBlank();
		assertThat(Base64.getDecoder().decode(result)).hasSize(32); // HMAC-SHA256 produces 32 bytes
	}

	@Test
	public void shouldProduceDeterministicOutput() {

		// arrange

		var secretKeySpec = AzureStorageQueueUtils.buildSecretKeySpec(TEST_KEY_BASE64);
		var stringToSign = "some-content-to-sign";

		// act

		var result1 = AzureStorageQueueUtils.computeSignature(secretKeySpec, stringToSign);
		var result2 = AzureStorageQueueUtils.computeSignature(secretKeySpec, stringToSign);

		// assert

		assertThat(result1).isEqualTo(result2);
	}

	@Test
	public void shouldProduceDifferentSignaturesForDifferentInputs() {

		// arrange

		var secretKeySpec = AzureStorageQueueUtils.buildSecretKeySpec(TEST_KEY_BASE64);

		// act

		var sig1 = AzureStorageQueueUtils.computeSignature(secretKeySpec, "input-a");
		var sig2 = AzureStorageQueueUtils.computeSignature(secretKeySpec, "input-b");

		// assert

		assertThat(sig1).isNotEqualTo(sig2);
	}

	@Test
	public void shouldProduceDifferentSignaturesForDifferentKeys() {

		// arrange

		var key1 = AzureStorageQueueUtils.buildSecretKeySpec(Base64.getEncoder().encodeToString("key-one".getBytes(StandardCharsets.UTF_8)));
		var key2 = AzureStorageQueueUtils.buildSecretKeySpec(Base64.getEncoder().encodeToString("key-two".getBytes(StandardCharsets.UTF_8)));
		var stringToSign = "same-content";

		// act

		var sig1 = AzureStorageQueueUtils.computeSignature(key1, stringToSign);
		var sig2 = AzureStorageQueueUtils.computeSignature(key2, stringToSign);

		// assert

		assertThat(sig1).isNotEqualTo(sig2);
	}

	@Test
	public void shouldProduceValidBase64Output() {

		// arrange

		var secretKeySpec = AzureStorageQueueUtils.buildSecretKeySpec(TEST_KEY_BASE64);
		var stringToSign = "test-content";

		// act

		var result = AzureStorageQueueUtils.computeSignature(secretKeySpec, stringToSign);

		// assert

		assertThat(result).matches("^[A-Za-z0-9+/]+=*$");
	}

	@Test
	public void shouldWorkWithWellKnownTestKey() {

		// arrange

		var testKey = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";
		var secretKeySpec = AzureStorageQueueUtils.buildSecretKeySpec(testKey);
		var stringToSign = "POST\n\n\n50\n\napplication/xml\n\n\n\n\n\nx-ms-date:Mon, 01 Jan 2024 00:00:00 GMT\nx-ms-version:2024-08-04\n/devstoreaccount1/my-queue/messages";

		// act

		var result = AzureStorageQueueUtils.computeSignature(secretKeySpec, stringToSign);

		// assert

		assertThat(result).isNotNull().isNotBlank();
		assertThat(Base64.getDecoder().decode(result)).hasSize(32);
	}
}
