package io.github.fortunen.kete.unittests.certificateloaders.pkcs12filebase64certificateloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.certificateloaders.Pkcs12FileBase64CertificateLoader;
import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.util.Base64;
import java.util.Map;
import okhttp3.tls.HeldCertificate;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

public class loadKeyStoreTests {

	private static final String TEST_PASSWORD = "testpass";

	@Test
	public void shouldLoadPkcs12KeyStoreFromBase64() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();

		// Create a PKCS12 keystore with the certificate and private key
		var pkcs12KeyStore = KeyStore.getInstance("PKCS12");
		pkcs12KeyStore.load(null, TEST_PASSWORD.toCharArray());
		pkcs12KeyStore.setKeyEntry(
			"test-key",
			heldCert.keyPair().getPrivate(),
			TEST_PASSWORD.toCharArray(),
			new java.security.cert.Certificate[] { heldCert.certificate() }
		);

		// Export to base64
		var baos = new ByteArrayOutputStream();
		pkcs12KeyStore.store(baos, TEST_PASSWORD.toCharArray());
		var base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

		var loader = new Pkcs12FileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		loader.loadKeyStore(keyStore, TEST_PASSWORD.toCharArray());

		// assert

		assertThat(keyStore.containsAlias("test-key"))
			.as("KeyStore should contain the key entry")
			.isTrue();

		assertThat(keyStore.isKeyEntry("test-key"))
			.as("Entry should be a key entry")
			.isTrue();

		assertThat(keyStore.getCertificate("test-key"))
			.as("Certificate should be available")
			.isNotNull();
	}

	@Test
	public void shouldLoadPkcs12WithCertificateChain() throws Exception {

		// arrange

		var rootCa = new HeldCertificate.Builder()
			.certificateAuthority(1)
			.commonName("Root CA")
			.build();

		var endEntity = new HeldCertificate.Builder()
			.signedBy(rootCa)
			.commonName("End Entity")
			.build();

		// Create a PKCS12 keystore with the chain
		var pkcs12KeyStore = KeyStore.getInstance("PKCS12");
		pkcs12KeyStore.load(null, TEST_PASSWORD.toCharArray());
		pkcs12KeyStore.setKeyEntry(
			"chain-key",
			endEntity.keyPair().getPrivate(),
			TEST_PASSWORD.toCharArray(),
			new java.security.cert.Certificate[] { endEntity.certificate(), rootCa.certificate() }
		);

		// Export to base64
		var baos = new ByteArrayOutputStream();
		pkcs12KeyStore.store(baos, TEST_PASSWORD.toCharArray());
		var base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

		var loader = new Pkcs12FileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		loader.loadKeyStore(keyStore, TEST_PASSWORD.toCharArray());

		// assert

		assertThat(keyStore.containsAlias("chain-key"))
			.as("KeyStore should contain the key entry")
			.isTrue();

		var chain = keyStore.getCertificateChain("chain-key");
		assertThat(chain)
			.as("Certificate chain should have 2 certificates")
			.hasSize(2);
	}

	@Test
	public void shouldThrowWhenBase64IsInvalid() throws Exception {

		// arrange

		var loader = new Pkcs12FileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", "not-valid-pkcs12-data"));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		var thrown = catchThrowable(() -> loader.loadKeyStore(keyStore, TEST_PASSWORD.toCharArray()));

		// assert

		assertThat(thrown)
			.as("Should throw exception for invalid PKCS12 data")
			.isNotNull();
	}

	@Test
	public void shouldThrowWhenPasswordIsIncorrect() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();

		var pkcs12KeyStore = KeyStore.getInstance("PKCS12");
		pkcs12KeyStore.load(null, TEST_PASSWORD.toCharArray());
		pkcs12KeyStore.setKeyEntry(
			"test-key",
			heldCert.keyPair().getPrivate(),
			TEST_PASSWORD.toCharArray(),
			new java.security.cert.Certificate[] { heldCert.certificate() }
		);

		var baos = new ByteArrayOutputStream();
		pkcs12KeyStore.store(baos, TEST_PASSWORD.toCharArray());
		var base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

		var loader = new Pkcs12FileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		var thrown = catchThrowable(() -> loader.loadKeyStore(keyStore, "wrong-password".toCharArray()));

		// assert

		assertThat(thrown)
			.as("Should throw exception for incorrect password")
			.isNotNull();
	}

	@Test
	public void shouldLoadWithNullPassword() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();

		// Create a PKCS12 keystore with no password
		var pkcs12KeyStore = KeyStore.getInstance("PKCS12");
		pkcs12KeyStore.load(null, null);
		pkcs12KeyStore.setKeyEntry(
			"test-key",
			heldCert.keyPair().getPrivate(),
			null,
			new java.security.cert.Certificate[] { heldCert.certificate() }
		);

		var baos = new ByteArrayOutputStream();
		pkcs12KeyStore.store(baos, null);
		var base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

		var loader = new Pkcs12FileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		loader.loadKeyStore(keyStore, null);

		// assert

		assertThat(keyStore.containsAlias("test-key"))
			.as("KeyStore should contain the key entry with null password")
			.isTrue();
	}

	@Test
	public void shouldLoadWithEmptyPassword() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();

		// Create a PKCS12 keystore with no password
		var pkcs12KeyStore = KeyStore.getInstance("PKCS12");
		pkcs12KeyStore.load(null, null);
		pkcs12KeyStore.setKeyEntry(
			"test-key",
			heldCert.keyPair().getPrivate(),
			null,
			new java.security.cert.Certificate[] { heldCert.certificate() }
		);

		var baos = new ByteArrayOutputStream();
		pkcs12KeyStore.store(baos, null);
		var base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

		var loader = new Pkcs12FileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		loader.loadKeyStore(keyStore, new char[0]);

		// assert

		assertThat(keyStore.containsAlias("test-key"))
			.as("KeyStore should contain the key entry with empty password")
			.isTrue();
	}

	@Test
	public void shouldLoadWithWhitespacePassword() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();

		// Create a PKCS12 keystore with no password
		var pkcs12KeyStore = KeyStore.getInstance("PKCS12");
		pkcs12KeyStore.load(null, null);
		pkcs12KeyStore.setKeyEntry(
			"test-key",
			heldCert.keyPair().getPrivate(),
			null,
			new java.security.cert.Certificate[] { heldCert.certificate() }
		);

		var baos = new ByteArrayOutputStream();
		pkcs12KeyStore.store(baos, null);
		var base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

		var loader = new Pkcs12FileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		loader.loadKeyStore(keyStore, "   ".toCharArray());

		// assert

		assertThat(keyStore.containsAlias("test-key"))
			.as("KeyStore should contain the key entry with whitespace password")
			.isTrue();
	}
}
