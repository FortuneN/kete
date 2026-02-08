package io.github.fortunen.kete.unittests.utils.azurestoragequeueutilsutils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.AzureStorageQueueUtils;

public class buildStringToSignTests {

	@Test
	public void shouldBuildCorrectStringToSign() {

		// arrange

		var contentLength = 123;
		var date = "Sat, 08 Feb 2026 12:00:00 GMT";
		var apiVersion = "2024-08-04";
		var canonicalResource = "/myaccount/my-queue/messages";

		// act

		var result = AzureStorageQueueUtils.buildStringToSign(contentLength, date, apiVersion, canonicalResource);

		// assert

		assertThat(result).isEqualTo(
			"POST\n\n\n" +
			"123" +
			"\n\napplication/xml\n\n\n\n\n\n" +
			"x-ms-date:Sat, 08 Feb 2026 12:00:00 GMT\n" +
			"x-ms-version:2024-08-04\n" +
			"/myaccount/my-queue/messages"
		);
	}

	@Test
	public void shouldIncludeContentLengthZero() {

		// arrange

		var contentLength = 0;
		var date = "Mon, 01 Jan 2024 00:00:00 GMT";
		var apiVersion = "2024-08-04";
		var canonicalResource = "/acc/q/messages";

		// act

		var result = AzureStorageQueueUtils.buildStringToSign(contentLength, date, apiVersion, canonicalResource);

		// assert

		assertThat(result).contains("POST\n\n\n0\n\napplication/xml");
	}

	@Test
	public void shouldIncludeCanonicalResourceWithMessageTtl() {

		// arrange

		var contentLength = 50;
		var date = "Mon, 01 Jan 2024 00:00:00 GMT";
		var apiVersion = "2024-08-04";
		var canonicalResource = "/myaccount/my-queue/messages\nmessagettl:-1";

		// act

		var result = AzureStorageQueueUtils.buildStringToSign(contentLength, date, apiVersion, canonicalResource);

		// assert

		assertThat(result).endsWith("/myaccount/my-queue/messages\nmessagettl:-1");
	}

	@Test
	public void shouldProduceConsistentOutputForSameInputs() {

		// arrange

		var contentLength = 100;
		var date = "Sat, 08 Feb 2026 12:00:00 GMT";
		var apiVersion = "2024-08-04";
		var canonicalResource = "/acc/q/messages";

		// act

		var result1 = AzureStorageQueueUtils.buildStringToSign(contentLength, date, apiVersion, canonicalResource);
		var result2 = AzureStorageQueueUtils.buildStringToSign(contentLength, date, apiVersion, canonicalResource);

		// assert

		assertThat(result1).isEqualTo(result2);
	}
}
