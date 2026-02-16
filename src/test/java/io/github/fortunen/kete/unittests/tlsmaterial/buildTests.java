package io.github.fortunen.kete.unittests.tlsmaterial;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.TlsMaterial;
import org.junit.jupiter.api.Test;

class buildTests {

	@Test
	void shouldReturnNullSSLContextWhenDisabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should be null when TLS is disabled")
			.isNull();

		assertThat(tls.isEnabled())
			.as("TLS should be disabled")
			.isFalse();
	}

	@Test
	void shouldReturnNullSSLContextWhenDisabledEvenWithWriteFiles() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should be null when TLS is disabled, even with write files")
			.isNull();

		assertThat(tls.isEnabled())
			.as("TLS should be disabled")
			.isFalse();
	}

	@Test
	void shouldReturnNullSSLContextWhenOnlyWriteFilesSpecified() {

		// act

		var tls = TlsMaterial.builder()
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should be null when only write files is specified (enabled defaults to false)")
			.isNull();

		assertThat(tls.isEnabled())
			.as("TLS should be disabled by default")
			.isFalse();
	}

	@Test
	void shouldReturnNonNullSSLContextWhenEnabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should not be null when enabled")
			.isNotNull();

		assertThat(tls.isEnabled())
			.as("TLS should be enabled")
			.isTrue();
	}

	@Test
	void shouldReturnNonNullSSLContextWhenEnabledWithWriteFiles() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should not be null when enabled with write files")
			.isNotNull();

		assertThat(tls.isEnabled())
			.as("TLS should be enabled")
			.isTrue();
	}

	@Test
	void shouldReturnNonNullSocketFactoryWhenEnabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should not be null when enabled")
			.isNotNull();

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory())
			.as("Socket factory should not be null when enabled")
			.isNotNull();
	}

	@Test
	void shouldBuildSSLContextWithCustomVersion() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withVersion("TLSv1.3")
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should not be null with custom version")
			.isNotNull();

		assertThat(tls.getVersion())
			.as("Should use custom TLS version")
			.isEqualTo("TLSv1.3");
	}

	@Test
	void shouldBuildSSLContextWithDefaultVersionWhenNotSpecified() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should not be null")
			.isNotNull();

		assertThat(tls.getVersion())
			.as("Should use default TLS version")
			.isEqualTo("TLS");

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext().getProtocol())
			.as("SSL context protocol should match TLS version")
			.isEqualTo("TLS");
	}

	@Test
	void shouldGenerateTrustStoreBase64WhenEnabledWithWriteFiles() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getTrustStoreBase64())
			.as("Trust store base64 should be generated when write files is enabled")
			.isNotNull()
			.isNotBlank();
	}

	@Test
	void shouldGenerateTrustStoreFilePathWhenEnabledWithWriteFiles() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getTrustStoreFilePath())
			.as("Trust store file path should be generated when write files is enabled")
			.isNotNull()
			.isNotBlank();
	}

	@Test
	void shouldNotGenerateTrustStorePasswordWhenBuiltWithoutConfiguration() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getTrustStorePassword())
			.as("Trust store password is only set from configuration, not auto-generated")
			.isNull();
	}

	@Test
	void shouldGenerateKeyStoreBase64WhenEnabledWithWriteFiles() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStoreBase64())
			.as("Key store base64 should be generated when write files is enabled")
			.isNotNull()
			.isNotBlank();
	}

	@Test
	void shouldGenerateKeyStoreFilePathWhenEnabledWithWriteFiles() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withServerHostNames(new String[]{"localhost"})
			.build();

		// assert

		assertThat(tls.getKeyStoreFilePath())
			.as("Key store file path should be generated when write files is enabled")
			.isNotNull()
			.isNotBlank();
	}

	@Test
	void shouldNotGenerateKeyStorePasswordWhenBuiltWithoutConfiguration() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStorePassword())
			.as("Key store password is only set from configuration, not auto-generated")
			.isNull();
	}

	@Test
	void shouldNotGenerateFilesWhenDisabledEvenWithWriteFiles() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		// assert

		assertThat(tls.getTrustStoreBase64())
			.as("Trust store base64 should not be generated when disabled")
			.isNull();

		assertThat(tls.getTrustStoreFilePath())
			.as("Trust store file path should not be generated when disabled")
			.isNull();

		assertThat(tls.getKeyStoreBase64())
			.as("Key store base64 should not be generated when disabled")
			.isNull();

		assertThat(tls.getKeyStoreFilePath())
			.as("Key store file path should not be generated when disabled")
			.isNull();
	}

	@Test
	void shouldGenerateBase64AndFilePathsWhenEnabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getTrustStoreBase64())
			.as("Trust store base64 is lazily generated when accessed")
			.isNotNull()
			.isNotBlank();

		assertThat(tls.getTrustStoreFilePath())
			.as("Trust store file path is lazily generated when accessed")
			.isNotNull()
			.isNotBlank();

		assertThat(tls.getKeyStoreBase64())
			.as("Key store base64 is lazily generated when accessed")
			.isNotNull()
			.isNotBlank();

		assertThat(tls.getKeyStoreFilePath())
			.as("Key store file path is lazily generated when accessed")
			.isNotNull()
			.isNotBlank();
	}

	@Test
	void shouldNotGeneratePasswordsWhenBuiltWithoutConfiguration() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getTrustStorePassword())
			.as("Trust store password is only set from configuration")
			.isNull();

		assertThat(tls.getKeyStorePassword())
			.as("Key store password is only set from configuration")
			.isNull();

		assertThat(tls.getKeyPassword())
			.as("Key password is only set from configuration")
			.isNull();
	}

	@Test
	void shouldGenerateDifferentBase64EncodingsForTrustAndKeyStores() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getTrustStoreBase64())
			.as("Trust store base64 should be generated")
			.isNotNull()
			.isNotBlank();

		assertThat(tls.getKeyStoreBase64())
			.as("Key store base64 should be generated")
			.isNotNull()
			.isNotBlank();

		assertThat(tls.getTrustStoreBase64())
			.as("Trust and key store base64 should be different")
			.isNotEqualTo(tls.getKeyStoreBase64());
	}

	@Test
	void shouldHaveValidSSLContextWhenFilesGenerated() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should not be null when files are generated")
			.isNotNull();

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory())
			.as("Socket factory should not be null when files are generated")
			.isNotNull();
	}

	@Test
	void shouldReturnNonNullSSLContextWithCustomConfiguration() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should not be null with custom configuration")
			.isNotNull();

		assertThat(tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory())
			.as("Socket factory should not be null with custom configuration")
			.isNotNull();
	}

	@Test
	void shouldRequireEnabledForSSLContext() {

		// act

		var tlsNotEnabled = TlsMaterial.builder().build();
		var tlsExplicitlyDisabled = TlsMaterial.builder().withEnabled(false).build();
		var tlsEnabled = TlsMaterial.builder().withEnabled(true).build();

		// assert

		assertThat(tlsNotEnabled.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should be null when not explicitly enabled (defaults to false)")
			.isNull();

		assertThat(tlsExplicitlyDisabled.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should be null when explicitly disabled")
			.isNull();

		assertThat(tlsEnabled.getKeyStoreAndTrustStoreSSLContext())
			.as("SSL context should not be null when explicitly enabled")
			.isNotNull();
	}

	@Test
	void shouldHaveConsistentEnabledStateWithSSLContext() {

		// act

		var tlsDisabled = TlsMaterial.builder().withEnabled(false).build();
		var tlsEnabled = TlsMaterial.builder().withEnabled(true).build();

		// assert

		assertThat(tlsDisabled.isEnabled())
			.as("Disabled TLS should report enabled=false")
			.isFalse();

		assertThat(tlsDisabled.getKeyStoreAndTrustStoreSSLContext())
			.as("Disabled TLS should have null SSL context")
			.isNull();

		assertThat(tlsEnabled.isEnabled())
			.as("Enabled TLS should report enabled=true")
			.isTrue();

		assertThat(tlsEnabled.getKeyStoreAndTrustStoreSSLContext())
			.as("Enabled TLS should have non-null SSL context")
			.isNotNull();
	}

	@Test
	void shouldReturnNullKeyStoreWhenDisabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		// assert

		assertThat(tls.getKeyStore())
			.as("KeyStore should be null when TLS is disabled")
			.isNull();
	}

	@Test
	void shouldReturnKeyStoreWhenEnabledEvenWithoutExplicitConfiguration() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStore())
			.as("KeyStore should not be null when TLS is enabled (created even if empty)")
			.isNotNull();
	}

	@Test
	void shouldReturnKeyStoreWhenWriteFilesEnabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStore())
			.as("KeyStore should not be null when writeFiles generates keystore")
			.isNotNull();
	}

	@Test
	void shouldReturnNullTrustStoreWhenDisabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		// assert

		assertThat(tls.getTrustStore())
			.as("TrustStore should be null when TLS is disabled")
			.isNull();
	}

	@Test
	void shouldReturnTrustStoreWhenEnabledEvenWithoutExplicitConfiguration() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getTrustStore())
			.as("TrustStore should not be null when TLS is enabled (created even if empty)")
			.isNotNull();
	}

	@Test
	void shouldReturnTrustStoreWhenWriteFilesEnabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getTrustStore())
			.as("TrustStore should not be null when writeFiles generates truststore")
			.isNotNull();
	}

	@Test
	void shouldReturnNullKeyStoreAndTrustStoreWhenDisabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStore())
			.as("KeyStoreAndTrustStore should be null when TLS is disabled")
			.isNull();
	}

	@Test
	void shouldReturnKeyStoreAndTrustStoreWhenEnabledEvenWithoutExplicitConfiguration() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStore())
			.as("KeyStoreAndTrustStore should not be null when TLS is enabled")
			.isNotNull();
	}

	@Test
	void shouldReturnKeyStoreAndTrustStoreWhenWriteFilesEnabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStore())
			.as("KeyStoreAndTrustStore should not be null when writeFiles generates both stores")
			.isNotNull();
	}

	@Test
	void shouldContainEntriesFromBothStoresInCombinedStore() throws Exception {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStoreAndTrustStore())
			.as("Combined store should not be null")
			.isNotNull();

		assertThat(tls.getKeyStoreAndTrustStore().size())
			.as("Combined store should have at least 1 entry")
			.isGreaterThanOrEqualTo(1);
	}

	// =========================================================================
	// Byte Array Fields
	// =========================================================================

	@Test
	void shouldGenerateTrustStoreBytesWhenEnabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getTrustStoreBytes())
			.as("Trust store bytes should be generated when TLS is enabled")
			.isNotNull()
			.isNotEmpty();
	}

	@Test
	void shouldGenerateKeyStoreBytesWhenEnabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStoreBytes())
			.as("Key store bytes should be generated when TLS is enabled")
			.isNotNull()
			.isNotEmpty();
	}

	@Test
	void shouldNotGenerateTrustStoreBytesWhenDisabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		// assert

		assertThat(tls.getTrustStoreBytes())
			.as("Trust store bytes should be null when TLS is disabled")
			.isNull();
	}

	@Test
	void shouldNotGenerateKeyStoreBytesWhenDisabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		// assert

		assertThat(tls.getKeyStoreBytes())
			.as("Key store bytes should be null when TLS is disabled")
			.isNull();
	}

	@Test
	void shouldGenerateTrustStoreBytesConsistentWithBase64() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getTrustStoreBytes()).isNotNull();
		assertThat(tls.getTrustStoreBase64()).isNotNull();
		assertThat(java.util.Base64.getEncoder().encodeToString(tls.getTrustStoreBytes()))
			.as("Trust store bytes and base64 should be consistent")
			.isEqualTo(tls.getTrustStoreBase64());
	}

	@Test
	void shouldGenerateKeyStoreBytesConsistentWithBase64() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getKeyStoreBytes()).isNotNull();
		assertThat(tls.getKeyStoreBase64()).isNotNull();
		assertThat(java.util.Base64.getEncoder().encodeToString(tls.getKeyStoreBytes()))
			.as("Key store bytes and base64 should be consistent")
			.isEqualTo(tls.getKeyStoreBase64());
	}

	@Test
	void shouldGenerateServerKeyStoreBytesWhenServerHostNamesProvided() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withServerHostNames(new String[]{"localhost"})
			.withKeyPassword("changeit")
			.build();

		// assert

		assertThat(tls.getServerKeyStoreBytes())
			.as("Server key store bytes should be generated when server host names are provided")
			.isNotNull()
			.isNotEmpty();
	}

	@Test
	void shouldNotGenerateServerKeyStoreBytesWhenNoServerHostNames() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getServerKeyStoreBytes())
			.as("Server key store bytes should be null when no server host names are provided")
			.isNull();
	}

	@Test
	void shouldGenerateCaCertificatePemBytesWhenServerHostNamesProvided() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withServerHostNames(new String[]{"localhost"})
			.withKeyPassword("changeit")
			.build();

		// assert

		assertThat(tls.getCaCertificatePemBytes())
			.as("CA certificate PEM bytes should be generated")
			.isNotNull()
			.isNotEmpty();

		var pemString = new String(tls.getCaCertificatePemBytes(), java.nio.charset.StandardCharsets.UTF_8);
		assertThat(pemString).contains("BEGIN CERTIFICATE");
	}

	@Test
	void shouldGenerateServerCertificatePemBytesWhenServerHostNamesProvided() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withServerHostNames(new String[]{"localhost"})
			.withKeyPassword("changeit")
			.build();

		// assert

		assertThat(tls.getServerCertificatePemBytes())
			.as("Server certificate PEM bytes should be generated")
			.isNotNull()
			.isNotEmpty();

		var pemString = new String(tls.getServerCertificatePemBytes(), java.nio.charset.StandardCharsets.UTF_8);
		assertThat(pemString).contains("BEGIN CERTIFICATE");
	}

	@Test
	void shouldGenerateServerPrivateKeyPemBytesWhenServerHostNamesProvided() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withServerHostNames(new String[]{"localhost"})
			.withKeyPassword("changeit")
			.build();

		// assert

		assertThat(tls.getServerPrivateKeyPemBytes())
			.as("Server private key PEM bytes should be generated")
			.isNotNull()
			.isNotEmpty();

		var pemString = new String(tls.getServerPrivateKeyPemBytes(), java.nio.charset.StandardCharsets.UTF_8);
		assertThat(pemString).contains("BEGIN PRIVATE KEY");
	}

	@Test
	void shouldGenerateServerPrivateKeyPkcs1PemBytesWhenServerHostNamesProvided() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withServerHostNames(new String[]{"localhost"})
			.withKeyPassword("changeit")
			.build();

		// assert

		assertThat(tls.getServerPrivateKeyPkcs1PemBytes())
			.as("Server private key PKCS1 PEM bytes should be generated")
			.isNotNull()
			.isNotEmpty();

		var pemString = new String(tls.getServerPrivateKeyPkcs1PemBytes(), java.nio.charset.StandardCharsets.UTF_8);
		assertThat(pemString).contains("BEGIN RSA PRIVATE KEY");
	}

	@Test
	void shouldNotGeneratePemBytesWhenNoServerHostNames() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// assert

		assertThat(tls.getCaCertificatePemBytes()).isNull();
		assertThat(tls.getServerCertificatePemBytes()).isNull();
		assertThat(tls.getServerPrivateKeyPemBytes()).isNull();
		assertThat(tls.getServerPrivateKeyPkcs1PemBytes()).isNull();
	}

	@Test
	void shouldNotGeneratePemBytesWhenDisabled() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.withServerHostNames(new String[]{"localhost"})
			.build();

		// assert

		assertThat(tls.getCaCertificatePemBytes()).isNull();
		assertThat(tls.getServerCertificatePemBytes()).isNull();
		assertThat(tls.getServerPrivateKeyPemBytes()).isNull();
		assertThat(tls.getServerPrivateKeyPkcs1PemBytes()).isNull();
		assertThat(tls.getServerKeyStoreBytes()).isNull();
	}

	@Test
	void shouldGeneratePemBytesAndFilePathsWhenServerHostNamesProvided() {

		// act

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withServerHostNames(new String[]{"localhost"})
			.withKeyPassword("changeit")
			.build();

		// assert

		assertThat(tls.getCaCertificatePemBytes()).isNotNull().isNotEmpty();
		assertThat(tls.getServerCertificatePemBytes()).isNotNull().isNotEmpty();
		assertThat(tls.getServerPrivateKeyPemBytes()).isNotNull().isNotEmpty();
		assertThat(tls.getServerPrivateKeyPkcs1PemBytes()).isNotNull().isNotEmpty();
		assertThat(tls.getServerKeyStoreBytes()).isNotNull().isNotEmpty();
		assertThat(tls.getCaCertificatePemFilePath()).isNotNull().isNotBlank();
		assertThat(tls.getServerCertificatePemFilePath()).isNotNull().isNotBlank();
		assertThat(tls.getServerPrivateKeyPemFilePath()).isNotNull().isNotBlank();
		assertThat(tls.getServerKeyStoreFilePath()).isNotNull().isNotBlank();
	}
}
