package io.github.fortunen.kete.unittests.certificateloaders.pkcs12filepathcertificateloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.certificateloaders.Pkcs12FilePathCertificateLoader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Map;
import okhttp3.tls.HeldCertificate;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class loadKeyStoreTests {

	private static final String TEST_PASSWORD = "testpass";

	@TempDir
	Path tempDir;

	@Test
	public void shouldLoadPkcs12KeyStoreFromFile() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();

		// Create and save a PKCS12 keystore
		var pkcs12File = tempDir.resolve("test.p12");
		var pkcs12KeyStore = KeyStore.getInstance("PKCS12");
		pkcs12KeyStore.load(null, TEST_PASSWORD.toCharArray());
		pkcs12KeyStore.setKeyEntry(
			"test-key",
			heldCert.keyPair().getPrivate(),
			TEST_PASSWORD.toCharArray(),
			new Certificate[] { heldCert.certificate() }
		);
		try (var fos = new FileOutputStream(pkcs12File.toFile())) {
			pkcs12KeyStore.store(fos, TEST_PASSWORD.toCharArray());
		}

		var loader = new Pkcs12FilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", pkcs12File.toString()));
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

		// Create and save a PKCS12 keystore with chain
		var pkcs12File = tempDir.resolve("chain.p12");
		var pkcs12KeyStore = KeyStore.getInstance("PKCS12");
		pkcs12KeyStore.load(null, TEST_PASSWORD.toCharArray());
		pkcs12KeyStore.setKeyEntry(
			"chain-key",
			endEntity.keyPair().getPrivate(),
			TEST_PASSWORD.toCharArray(),
			new Certificate[] { endEntity.certificate(), rootCa.certificate() }
		);
		try (var fos = new FileOutputStream(pkcs12File.toFile())) {
			pkcs12KeyStore.store(fos, TEST_PASSWORD.toCharArray());
		}

		var loader = new Pkcs12FilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", pkcs12File.toString()));
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
	public void shouldThrowWhenFileNotFound() throws Exception {

		// arrange

		var loader = new Pkcs12FilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", "/non/existent/path/keystore.p12"));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		var thrown = catchThrowable(() -> loader.loadKeyStore(keyStore, TEST_PASSWORD.toCharArray()));

		// assert

		assertThat(thrown)
			.as("Should throw exception when file not found")
			.isInstanceOf(FileNotFoundException.class);
	}

	@Test
	public void shouldThrowWhenFileContainsInvalidData() throws Exception {

		// arrange

		var invalidFile = tempDir.resolve("invalid.p12");
		Files.writeString(invalidFile, "not a valid pkcs12 keystore");

		var loader = new Pkcs12FilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", invalidFile.toString()));
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

		var pkcs12File = tempDir.resolve("test.p12");
		var pkcs12KeyStore = KeyStore.getInstance("PKCS12");
		pkcs12KeyStore.load(null, TEST_PASSWORD.toCharArray());
		pkcs12KeyStore.setKeyEntry(
			"test-key",
			heldCert.keyPair().getPrivate(),
			TEST_PASSWORD.toCharArray(),
			new Certificate[] { heldCert.certificate() }
		);
		try (var fos = new FileOutputStream(pkcs12File.toFile())) {
			pkcs12KeyStore.store(fos, TEST_PASSWORD.toCharArray());
		}

		var loader = new Pkcs12FilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", pkcs12File.toString()));
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

		var pkcs12File = tempDir.resolve("nopass.p12");
		var pkcs12KeyStore = KeyStore.getInstance("PKCS12");
		pkcs12KeyStore.load(null, null);
		pkcs12KeyStore.setKeyEntry(
			"test-key",
			heldCert.keyPair().getPrivate(),
			null,
			new Certificate[] { heldCert.certificate() }
		);
		try (var fos = new FileOutputStream(pkcs12File.toFile())) {
			pkcs12KeyStore.store(fos, null);
		}

		var loader = new Pkcs12FilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", pkcs12File.toString()));
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

		var pkcs12File = tempDir.resolve("emptypass.p12");
		var pkcs12KeyStore = KeyStore.getInstance("PKCS12");
		pkcs12KeyStore.load(null, null);
		pkcs12KeyStore.setKeyEntry(
			"test-key",
			heldCert.keyPair().getPrivate(),
			null,
			new Certificate[] { heldCert.certificate() }
		);
		try (var fos = new FileOutputStream(pkcs12File.toFile())) {
			pkcs12KeyStore.store(fos, null);
		}

		var loader = new Pkcs12FilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", pkcs12File.toString()));
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

		var pkcs12File = tempDir.resolve("whitespacepass.p12");
		var pkcs12KeyStore = KeyStore.getInstance("PKCS12");
		pkcs12KeyStore.load(null, null);
		pkcs12KeyStore.setKeyEntry(
			"test-key",
			heldCert.keyPair().getPrivate(),
			null,
			new Certificate[] { heldCert.certificate() }
		);
		try (var fos = new FileOutputStream(pkcs12File.toFile())) {
			pkcs12KeyStore.store(fos, null);
		}

		var loader = new Pkcs12FilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", pkcs12File.toString()));
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
