package io.github.fortunen.kete.unittests.tlsmaterial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.TlsMaterial;
import org.junit.jupiter.api.Test;

class getKeyStoreAndTrustStoreSSLContextTests {

	@Test
	void shouldReturnNonNullWhenTlsEnabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("getKeyStoreAndTrustStoreSSLContext() should return non-null when TLS is enabled")
			.isNotNull();
	}

	@Test
	void shouldReturnNullWhenTlsDisabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("getKeyStoreAndTrustStoreSSLContext() should return null when TLS is disabled")
			.isNull();
	}

	@Test
	void shouldNotThrowNPEWhenAccessingOnEnabledTls() {

		// act & assert

		assertThatCode(() -> {
			var tls = TlsMaterial.builder()
				.withEnabled(true)
				.build();
			var context = tls.getKeyStoreAndTrustStoreSSLContext();
			assertThat(context).isNotNull();
		})
			.as("Should not throw NPE when accessing getKeyStoreAndTrustStoreSSLContext() on enabled TLS")
			.doesNotThrowAnyException();
	}

	@Test
	void shouldThrowNPEWhenAccessingSocketFactoryOnNullContext() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		// act & assert

		assertThatThrownBy(() -> {
			tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory();
		})
			.as("Should throw NPE when accessing socket factory on null context")
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void shouldHandleNullContextGracefullyInTests() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("getKeyStoreAndTrustStoreSSLContext() should return null when disabled")
			.isNull();

		if (tls.getKeyStoreAndTrustStoreSSLContext() != null) {
			assertThat(tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory()).isNotNull();
		}
	}

	@Test
	void shouldRequireEnabledBeforeCheckingSocketFactory() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// act & assert

		assertThat(tls.isEnabled())
			.as("TLS should be enabled")
			.isTrue();

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("getKeyStoreAndTrustStoreSSLContext() should return non-null when enabled")
			.isNotNull();

		assertThatCode(() -> {
			var factory = tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory();
			assertThat(factory).isNotNull();
		})
			.as("Should not throw NPE when properly checked")
			.doesNotThrowAnyException();
	}

	@Test
	void shouldDocumentCorrectUsagePatternForTestBase() {

		// arrange

		var tlsCorrect = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		var tlsIncorrect = TlsMaterial.builder()
			.build();

		// assert

		assertThat(tlsCorrect.isEnabled())
			.as("Correct: TLS must be enabled")
			.isTrue();

		assertThat(tlsCorrect.getKeyStoreAndTrustStoreSSLContext())
			.as("Correct: getKeyStoreAndTrustStoreSSLContext() must return non-null")
			.isNotNull();

		assertThatCode(() -> {
			tlsCorrect.getKeyStoreAndTrustStoreSSLContext().getSocketFactory();
		})
			.as("Correct: Should not throw NPE")
			.doesNotThrowAnyException();

		assertThat(tlsIncorrect.isEnabled())
			.as("Incorrect: TLS is not enabled")
			.isFalse();

		assertThat(tlsIncorrect.getKeyStoreAndTrustStoreSSLContext())
			.as("Incorrect: getKeyStoreAndTrustStoreSSLContext() returns null")
			.isNull();

		assertThatThrownBy(() -> {
			tlsIncorrect.getKeyStoreAndTrustStoreSSLContext().getSocketFactory();
		})
			.as("Incorrect: Will throw NPE because getKeyStoreAndTrustStoreSSLContext() returns null")
			.isInstanceOf(NullPointerException.class)
			.hasMessageContaining("Cannot invoke \"javax.net.ssl.SSLContext.getSocketFactory()\"");
	}

	@Test
	void shouldIndicateWhenSafeToCallSocketFactory() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		if (tls.isEnabled() && tls.getKeyStoreAndTrustStoreSSLContext() != null) {
			assertThat(tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory())
				.as("Socket factory should be accessible when guarded by null check")
				.isNotNull();
		}
	}

	@Test
	void shouldReturnValidContextWithSystemTrustedCertificates() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("getKeyStoreAndTrustStoreSSLContext() should return non-null")
			.isNotNull();

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext().getProtocol())
			.as("SSL context should use TLS protocol")
			.isEqualTo("TLS");
	}

	@Test
	void shouldReturnConsistentContextOnMultipleCalls() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// act

		var context1 = tls.getKeyStoreAndTrustStoreSSLContext();
		var context2 = tls.getKeyStoreAndTrustStoreSSLContext();

		// assert

		assertThat(context1)
			.as("getKeyStoreAndTrustStoreSSLContext() should return non-null")
			.isNotNull();

		assertThat(context2)
			.as("Second call should return same SSL context")
			.isSameAs(context1);
	}

	@Test
	void shouldReturnNonNullSocketFactoryOnMultipleCalls() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// act

		var factory1 = tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory();
		var factory2 = tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory();

		// assert

		assertThat(factory1)
			.as("Socket factory should not be null")
			.isNotNull();

		assertThat(factory2)
			.as("Socket factory should not be null on subsequent calls")
			.isNotNull();

		assertThat(factory1.getClass())
			.as("Socket factories should be of same type")
			.isEqualTo(factory2.getClass());
	}

	@Test
	void shouldNotThrowNPEWhenGettingSocketFactoryFromNonNullContext() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// act & assert

		assertThatCode(() -> {
			var socketFactory = tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory();
			assertThat(socketFactory).isNotNull();
		})
			.as("Should not throw NPE when getting socket factory from valid SSL context")
			.doesNotThrowAnyException();
	}
}
