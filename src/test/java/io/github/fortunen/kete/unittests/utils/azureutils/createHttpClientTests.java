package io.github.fortunen.kete.unittests.utils.azureutils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import com.azure.core.http.HttpClient;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.utils.AzureUtils;

class createHttpClientTests {

	@Test
	void shouldReturnHttpClientWhenTlsDisabled() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		// act

		var client = AzureUtils.createHttpClient(tls, Duration.ofSeconds(10));

		// assert

		assertThat(client).isNotNull();
		assertThat(client).isInstanceOf(HttpClient.class);
	}

	@Test
	void shouldReturnHttpClientWhenTlsEnabled() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// act

		var client = AzureUtils.createHttpClient(tls, Duration.ofSeconds(10));

		// assert

		assertThat(client).isNotNull();
		assertThat(client).isInstanceOf(HttpClient.class);
	}

	@Test
	void shouldReturnHttpClientWhenTlsEnabledWithServerHostNames() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withServerHostNames(new String[]{"localhost"})
			.withKeyPassword("changeit")
			.build();

		// act

		var client = AzureUtils.createHttpClient(tls, Duration.ofSeconds(10));

		// assert

		assertThat(client).isNotNull();
	}

	@Test
	void shouldRespectTimeoutParameter() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		// act

		var client = AzureUtils.createHttpClient(tls, Duration.ofSeconds(30));

		// assert

		assertThat(client).isNotNull();
	}

	@Test
	void shouldReturnHttpClientWithShortTimeout() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		// act

		var client = AzureUtils.createHttpClient(tls, Duration.ofMillis(500));

		// assert

		assertThat(client).isNotNull();
	}
}
