package io.github.fortunen.kete.unittests.certificateloaders.pemfiletextcertificateloader;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.certificateloaders.PemFileTextCertificateLoader;
import java.security.KeyStore;
import java.util.Map;
import okhttp3.tls.HeldCertificate;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

public class loadKeyStoreTests {

	@Test
	public void shouldLoadCertificateIntoKeyStore() throws Exception {

		// arrange - create a PEM certificate

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var pemContent = heldCert.certificatePem();

		var loader = new PemFileTextCertificateLoader();
		var config = new MapConfiguration(Map.of("text", pemContent));
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

		var loader = new PemFileTextCertificateLoader();
		var config = new MapConfiguration(Map.of("text", pemContent));
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

		var loader = new PemFileTextCertificateLoader();
		var config = new MapConfiguration(Map.of("text", pemContent));
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

		var loader = new PemFileTextCertificateLoader();
		var config = new MapConfiguration(Map.of("text", pemContent));
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
	public void shouldHandleInvalidPemGracefully() throws Exception {

		// arrange - invalid PEM content (no certificate markers)

		var invalidPem = "this is not a valid PEM content";

		var loader = new PemFileTextCertificateLoader();
		var config = new MapConfiguration(Map.of("text", invalidPem));
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

		var loader = new PemFileTextCertificateLoader();
		var config = new MapConfiguration(Map.of("text", pemContent));
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

		var loader = new PemFileTextCertificateLoader();
		var config = new MapConfiguration(Map.of("text", pemContent));
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

		var loader = new PemFileTextCertificateLoader();
		var config = new MapConfiguration(Map.of("text", pemContent));
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
	public void shouldThrowExceptionViaLombokWithCorruptedPem() throws Exception {

		// arrange - create a corrupted PEM that will fail during certificate parsing
		// Using a malformed base64 block inside valid PEM headers

		var corruptedPem = "-----BEGIN CERTIFICATE-----\n"
			+ "MIIB/jCCAWeg\n"  // truncated/corrupted base64
			+ "-----END CERTIFICATE-----";

		var loader = new PemFileTextCertificateLoader();
		var config = new MapConfiguration(Map.of("text", corruptedPem));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		var thrown = org.assertj.core.api.Assertions.catchThrowable(
			() -> loader.loadKeyStore(keyStore, "password".toCharArray())
		);

		// assert - @SneakyThrows allows exception from certificate parsing

		assertThat(thrown)
			.as("Should throw exception via Lombok @SneakyThrows for corrupted PEM content")
			.isNotNull();
	}
}
