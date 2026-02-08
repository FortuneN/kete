package io.github.fortunen.kete.unittests.certificateloaders.jksfilebase64certificateloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.certificateloaders.JksFileBase64CertificateLoader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.Map;
import okhttp3.tls.HeldCertificate;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

class loadKeyStoreTests {

	@Test
	void shouldLoadKeystoreFromBase64() throws Exception {

		// arrange - create a PKCS12 keystore and encode it as base64

		var loader = new JksFileBase64CertificateLoader();
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

		var baos = new ByteArrayOutputStream();
		sourceKeyStore.store(baos, "test".toCharArray());
		var base64Content = Base64.getEncoder().encodeToString(baos.toByteArray());

		var config = new MapConfiguration(Map.of("base64", base64Content));
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
	void shouldThrowIllegalArgumentExceptionViaLombok() throws Exception {

		// arrange - set invalid base64 that cannot be decoded properly

		var loader = new JksFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", "!!!invalid-base64!!!"));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		var thrown = catchThrowable(() -> loader.loadKeyStore(keyStore, "test".toCharArray()));

		// assert - @SneakyThrows allows checked exception without declaring

		assertThat(thrown)
			.as("Should throw exception via Lombok @SneakyThrows when base64 is invalid")
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldThrowIOExceptionViaLombok() throws Exception {

		// arrange - create valid base64 but with invalid keystore content

		var loader = new JksFileBase64CertificateLoader();
		var invalidContent = "not a valid keystore content";
		var base64Content = Base64.getEncoder().encodeToString(invalidContent.getBytes());

		var config = new MapConfiguration(Map.of("base64", base64Content));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance("PKCS12");

		// act

		var thrown = catchThrowable(() -> loader.loadKeyStore(keyStore, "test".toCharArray()));

		// assert - @SneakyThrows allows checked IOException without declaring

		assertThat(thrown)
			.as("Should throw exception via Lombok @SneakyThrows when keystore content is invalid")
			.isInstanceOf(IOException.class);
	}

	@Test
	void shouldLoadWithNullPassword() throws Exception {

		// arrange - create a PKCS12 keystore with no password and encode as base64

		var loader = new JksFileBase64CertificateLoader();
		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();

		var sourceKeyStore = KeyStore.getInstance("PKCS12");
		sourceKeyStore.load(null, null);
		sourceKeyStore.setCertificateEntry("test-cert", heldCert.certificate());

		var baos = new ByteArrayOutputStream();
		sourceKeyStore.store(baos, null);
		var base64Content = Base64.getEncoder().encodeToString(baos.toByteArray());

		var config = new MapConfiguration(Map.of("base64", base64Content));
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

		// arrange - create a PKCS12 keystore with no password and encode as base64

		var loader = new JksFileBase64CertificateLoader();
		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();

		var sourceKeyStore = KeyStore.getInstance("PKCS12");
		sourceKeyStore.load(null, null);
		sourceKeyStore.setCertificateEntry("test-cert", heldCert.certificate());

		var baos = new ByteArrayOutputStream();
		sourceKeyStore.store(baos, null);
		var base64Content = Base64.getEncoder().encodeToString(baos.toByteArray());

		var config = new MapConfiguration(Map.of("base64", base64Content));
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

		// arrange - create a PKCS12 keystore with no password and encode as base64

		var loader = new JksFileBase64CertificateLoader();
		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();

		var sourceKeyStore = KeyStore.getInstance("PKCS12");
		sourceKeyStore.load(null, null);
		sourceKeyStore.setCertificateEntry("test-cert", heldCert.certificate());

		var baos = new ByteArrayOutputStream();
		sourceKeyStore.store(baos, null);
		var base64Content = Base64.getEncoder().encodeToString(baos.toByteArray());

		var config = new MapConfiguration(Map.of("base64", base64Content));
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
