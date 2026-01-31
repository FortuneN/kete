package io.github.fortunen.kete.unittests.certificateloaders.derfilepathcertificateloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.certificateloaders.DerFilePathCertificateLoader;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Map;
import okhttp3.tls.HeldCertificate;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class loadKeyStoreTests {

	@TempDir
	Path tempDir;

	@Test
	public void shouldLoadCertificateFromFile() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var certBytes = heldCert.certificate().getEncoded();
		var certFile = tempDir.resolve("test.der");
		Files.write(certFile, certBytes);

		var loader = new DerFilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", certFile.toString()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, "password".toCharArray());

		// assert

		assertThat(keyStore.containsAlias("cert"))
			.as("KeyStore should contain the certificate with alias 'cert'")
			.isTrue();

		assertThat(keyStore.getCertificate("cert"))
			.as("Certificate should be loaded into KeyStore")
			.isNotNull();

		assertThat(keyStore.isCertificateEntry("cert"))
			.as("Entry should be a certificate entry")
			.isTrue();
	}

	@Test
	public void shouldLoadCertificateWithDefaultAlias() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var certBytes = heldCert.certificate().getEncoded();
		var certFile = tempDir.resolve("test.der");
		Files.write(certFile, certBytes);

		var loader = new DerFilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", certFile.toString()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, "password".toCharArray());

		// assert

		assertThat(keyStore.containsAlias("cert"))
			.as("KeyStore should contain the certificate with alias 'cert'")
			.isTrue();
	}

	@Test
	public void shouldThrowWhenFileNotFound() throws Exception {

		// arrange

		var loader = new DerFilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", "/non/existent/path/cert.der"));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		var thrown = catchThrowable(() -> loader.loadKeyStore(keyStore, "password".toCharArray()));

		// assert

		assertThat(thrown)
			.as("Should throw exception when file not found")
			.isInstanceOf(FileNotFoundException.class);
	}

	@Test
	public void shouldThrowWhenFileContainsInvalidData() throws Exception {

		// arrange

		var invalidFile = tempDir.resolve("invalid.der");
		Files.writeString(invalidFile, "not a valid certificate");

		var loader = new DerFilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", invalidFile.toString()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		var thrown = catchThrowable(() -> loader.loadKeyStore(keyStore, "password".toCharArray()));

		// assert

		assertThat(thrown)
			.as("Should throw exception for invalid certificate data")
			.isNotNull();
	}

	@Test
	public void shouldWorkWithNullPassword() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var certBytes = heldCert.certificate().getEncoded();
		var certFile = tempDir.resolve("test.der");
		Files.write(certFile, certBytes);

		var loader = new DerFilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", certFile.toString()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, null);

		// assert

		assertThat(keyStore.containsAlias("cert"))
			.as("KeyStore should contain the certificate with alias 'cert' even with null password")
			.isTrue();
	}

	@Test
	public void shouldWorkWithEmptyPassword() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var certBytes = heldCert.certificate().getEncoded();
		var certFile = tempDir.resolve("test.der");
		Files.write(certFile, certBytes);

		var loader = new DerFilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", certFile.toString()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, new char[0]);

		// assert

		assertThat(keyStore.containsAlias("cert"))
			.as("KeyStore should contain the certificate with alias 'cert' even with empty password")
			.isTrue();
	}

	@Test
	public void shouldWorkWithWhitespacePassword() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var certBytes = heldCert.certificate().getEncoded();
		var certFile = tempDir.resolve("test.der");
		Files.write(certFile, certBytes);

		var loader = new DerFilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", certFile.toString()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, "   ".toCharArray());

		// assert

		assertThat(keyStore.containsAlias("cert"))
			.as("KeyStore should contain the certificate with alias 'cert' even with whitespace password")
			.isTrue();
	}
}
