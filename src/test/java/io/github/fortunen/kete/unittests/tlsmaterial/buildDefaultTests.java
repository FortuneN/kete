package io.github.fortunen.kete.unittests.tlsmaterial;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.TlsMaterial;
import org.junit.jupiter.api.Test;

class buildDefaultTests {

	@Test
	void shouldReturnNonNullSSLContext() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should not be null")
			.isNotNull();
	}

	@Test
	void shouldReturnNonNullSocketFactory() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should not be null")
			.isNotNull();

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory())
			.as("Socket factory should not be null")
			.isNotNull();
	}

	@Test
	void shouldBeEnabledByDefault() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.isEnabled())
			.as("TLS should be enabled by default")
			.isTrue();
	}

	@Test
	void shouldReturnMaterialWithNullSSLContextWhenDisabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		// assert

		assertThat(tls.isEnabled())
			.as("TLS should be disabled")
			.isFalse();
		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should be null when disabled")
			.isNull();
	}
}
