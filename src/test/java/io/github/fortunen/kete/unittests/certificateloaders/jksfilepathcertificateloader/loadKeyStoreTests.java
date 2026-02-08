package io.github.fortunen.kete.unittests.certificateloaders.jksfilepathcertificateloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.certificateloaders.JksFilePathCertificateLoader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Map;
import okhttp3.tls.HeldCertificate;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class loadKeyStoreTests {

	@TempDir
	Path tempDir;

	@Test
	void shouldLoadKeystoreFromPath() throws Exception {

		// arrange - create a PKCS12 keystore file

		var loader = new JksFilePathCertificateLoader();
		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();

		var sourceKeyStore = KeyStore.getInstance("PKCS12");
		sourceKeyStore.load(null, "test".toCharArray());
		sourceKeyStore.setKeyEntry(
			"test-key",
			heldCert.keyPair().getPrivate(),
			"test".toCharArray(),
			new Certificate[] { heldCert.certificate() }
		);

		var keystoreFile = tempDir.resolve("test.p12");
		try (var fos = Files.newOutputStream(keystoreFile)) {
			sourceKeyStore.store(fos, "test".toCharArray());
		}

		var config = new MapConfiguration(Map.of("path", keystoreFile.toString()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		loader.loadKeyStore(keyStore, "test".toCharArray());

		// assert

		assertThat(keyStore.containsAlias("test-key"))
			.as("KeyStore should contain the loaded key entry")
			.isTrue();
	}

	@Test
	void shouldThrowFileNotFoundExceptionViaLombok() throws Exception {

		// arrange - set a non-existent file path

		var loader = new JksFilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", "/non/existent/path/keystore.jks"));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		var thrown = catchThrowable(() -> loader.loadKeyStore(keyStore, "test".toCharArray()));

		// assert - @SneakyThrows allows FileNotFoundException without declaring

		assertThat(thrown)
			.as("Should throw FileNotFoundException via Lombok @SneakyThrows when file not found")
			.isInstanceOf(FileNotFoundException.class);
	}

	@Test
	void shouldThrowIOExceptionViaLombok() throws Exception {

		// arrange - create a file with invalid keystore content

		var loader = new JksFilePathCertificateLoader();
		var invalidFile = tempDir.resolve("invalid.jks");
		Files.writeString(invalidFile, "not a valid keystore content");

		var config = new MapConfiguration(Map.of("path", invalidFile.toString()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		var thrown = catchThrowable(() -> loader.loadKeyStore(keyStore, "test".toCharArray()));

		// assert - @SneakyThrows allows IOException without declaring

		assertThat(thrown)
			.as("Should throw IOException via Lombok @SneakyThrows when keystore content is invalid")
			.isInstanceOf(IOException.class);
	}

	@Test
	void shouldLoadWithNullPassword() throws Exception {

		// arrange - create a PKCS12 keystore file with no password

		var loader = new JksFilePathCertificateLoader();
		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();

		var sourceKeyStore = KeyStore.getInstance("PKCS12");
		sourceKeyStore.load(null, null);
		sourceKeyStore.setCertificateEntry("test-cert", heldCert.certificate());

		var keystoreFile = tempDir.resolve("test-no-password.p12");
		try (var fos = Files.newOutputStream(keystoreFile)) {
			sourceKeyStore.store(fos, null);
		}

		var config = new MapConfiguration(Map.of("path", keystoreFile.toString()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		loader.loadKeyStore(keyStore, null);

		// assert

		assertThat(keyStore.containsAlias("test-cert"))
			.as("KeyStore should contain the certificate entry with null password")
			.isTrue();
	}

	@Test
	void shouldLoadWithEmptyPassword() throws Exception {

		// arrange - create a PKCS12 keystore file with no password

		var loader = new JksFilePathCertificateLoader();
		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();

		var sourceKeyStore = KeyStore.getInstance("PKCS12");
		sourceKeyStore.load(null, null);
		sourceKeyStore.setCertificateEntry("test-cert", heldCert.certificate());

		var keystoreFile = tempDir.resolve("test-empty-password.p12");
		try (var fos = Files.newOutputStream(keystoreFile)) {
			sourceKeyStore.store(fos, null);
		}

		var config = new MapConfiguration(Map.of("path", keystoreFile.toString()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		loader.loadKeyStore(keyStore, new char[0]);

		// assert

		assertThat(keyStore.containsAlias("test-cert"))
			.as("KeyStore should contain the certificate entry with empty password")
			.isTrue();
	}

	@Test
	void shouldLoadWithWhitespacePassword() throws Exception {

		// arrange - create a PKCS12 keystore file with no password

		var loader = new JksFilePathCertificateLoader();
		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();

		var sourceKeyStore = KeyStore.getInstance("PKCS12");
		sourceKeyStore.load(null, null);
		sourceKeyStore.setCertificateEntry("test-cert", heldCert.certificate());

		var keystoreFile = tempDir.resolve("test-whitespace-password.p12");
		try (var fos = Files.newOutputStream(keystoreFile)) {
			sourceKeyStore.store(fos, null);
		}

		var config = new MapConfiguration(Map.of("path", keystoreFile.toString()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		loader.loadKeyStore(keyStore, "   ".toCharArray());

		// assert

		assertThat(keyStore.containsAlias("test-cert"))
			.as("KeyStore should contain the certificate entry with whitespace password")
			.isTrue();
	}
}
