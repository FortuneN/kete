package io.github.fortunen.kete.unittests.certificateloaders.pemfilebase64certificateloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.certificateloaders.PemFileBase64CertificateLoader;
import java.security.KeyStore;
import java.util.Base64;
import java.util.Map;
import okhttp3.tls.HeldCertificate;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

public class loadKeyStoreTests {

	@Test
	public void shouldLoadCertificateIntoKeyStore() throws Exception {

		// arrange - create a PEM certificate and encode it as base64

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var pemContent = heldCert.certificatePem();
		var base64Pem = Base64.getEncoder().encodeToString(pemContent.getBytes());

		var loader = new PemFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64Pem));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, "password".toCharArray());

		// assert

		assertThat(keyStore.containsAlias("key-0"))
			.as("KeyStore should contain the certificate with alias 'key-0'")
			.isTrue();

		assertThat(keyStore.getCertificate("key-0"))
			.as("Certificate should be loaded into KeyStore")
			.isNotNull();

		assertThat(keyStore.isCertificateEntry("key-0"))
			.as("Entry should be a certificate entry")
			.isTrue();
	}

	@Test
	public void shouldLoadCertificateWithDefaultAlias() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var pemContent = heldCert.certificatePem();
		var base64Pem = Base64.getEncoder().encodeToString(pemContent.getBytes());

		var loader = new PemFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64Pem));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, "password".toCharArray());

		// assert

		assertThat(keyStore.containsAlias("key-0"))
			.as("KeyStore should contain the certificate with default alias 'key-0'")
			.isTrue();
	}

	@Test
	public void shouldLoadCertificateWithPrivateKey() throws Exception {

		// arrange - create PEM with both certificate and private key

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert-with-key")
			.build();
		var pemContent = heldCert.certificatePem() + "\n" + heldCert.privateKeyPkcs8Pem();
		var base64Pem = Base64.getEncoder().encodeToString(pemContent.getBytes());

		var loader = new PemFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64Pem));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, "password".toCharArray());

		// assert

		assertThat(keyStore.containsAlias("key"))
			.as("KeyStore should contain the key entry with alias 'key'")
			.isTrue();

		assertThat(keyStore.isKeyEntry("key"))
			.as("Entry should be a key entry (not just certificate)")
			.isTrue();

		assertThat(keyStore.getKey("key", "password".toCharArray()))
			.as("Private key should be retrievable")
			.isNotNull();
	}

	@Test
	public void shouldLoadMultipleCertificates() throws Exception {

		// arrange - create multiple certificates

		var cert1 = new HeldCertificate.Builder().commonName("cert1").build();
		var cert2 = new HeldCertificate.Builder().commonName("cert2").build();
		var pemContent = cert1.certificatePem() + "\n" + cert2.certificatePem();
		var base64Pem = Base64.getEncoder().encodeToString(pemContent.getBytes());

		var loader = new PemFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64Pem));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, "password".toCharArray());

		// assert

		assertThat(keyStore.containsAlias("key-0"))
			.as("KeyStore should contain first certificate")
			.isTrue();

		assertThat(keyStore.containsAlias("key-1"))
			.as("KeyStore should contain second certificate")
			.isTrue();
	}

	@Test
	public void shouldThrowWhenBase64IsInvalidPem() throws Exception {

		// arrange - invalid PEM content (no certificate markers)

		var invalidPem = "this is not a valid PEM content";
		var base64Pem = Base64.getEncoder().encodeToString(invalidPem.getBytes());

		var loader = new PemFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64Pem));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, "password".toCharArray());

		// assert - no certificates loaded when PEM is invalid

		assertThat(keyStore.size())
			.as("KeyStore should be empty when no valid certificates found")
			.isZero();
	}

	@Test
	public void shouldWorkWithNullPassword() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var pemContent = heldCert.certificatePem();
		var base64Pem = Base64.getEncoder().encodeToString(pemContent.getBytes());

		var loader = new PemFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64Pem));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, null);

		// assert

		assertThat(keyStore.containsAlias("key-0"))
			.as("KeyStore should contain the certificate even with null password")
			.isTrue();
	}

	@Test
	public void shouldHandleEmptyPassword() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var pemContent = heldCert.certificatePem() + "\n" + heldCert.privateKeyPkcs8Pem();
		var base64Pem = Base64.getEncoder().encodeToString(pemContent.getBytes());

		var loader = new PemFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64Pem));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, new char[0]);

		// assert

		assertThat(keyStore.containsAlias("key"))
			.as("KeyStore should contain the key entry even with empty password")
			.isTrue();
	}

	@Test
	public void shouldHandleWhitespacePassword() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var pemContent = heldCert.certificatePem() + "\n" + heldCert.privateKeyPkcs8Pem();
		var base64Content = Base64.getEncoder().encodeToString(pemContent.getBytes());

		var loader = new PemFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64Content));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, "   ".toCharArray());

		// assert

		assertThat(keyStore.containsAlias("key"))
			.as("KeyStore should contain the key entry even with whitespace password")
			.isTrue();
	}

	@Test
	public void shouldThrowIllegalArgumentExceptionViaLombok() throws Exception {

		// arrange - set invalid base64 that cannot be decoded

		var loader = new PemFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", "!!!invalid-base64!!!"));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		var thrown = catchThrowable(
			() -> loader.loadKeyStore(keyStore, "password".toCharArray())
		);

		// assert - @SneakyThrows allows IllegalArgumentException from Base64 decoder

		assertThat(thrown)
			.as("Should throw IllegalArgumentException via Lombok @SneakyThrows for invalid base64")
			.isInstanceOf(IllegalArgumentException.class);
	}
}
