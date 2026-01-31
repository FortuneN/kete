package io.github.fortunen.kete.unittests.certificateloaders.derfilebase64certificateloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.certificateloaders.DerFileBase64CertificateLoader;
import java.security.KeyStore;
import java.util.Base64;
import java.util.Map;
import okhttp3.tls.HeldCertificate;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

public class loadKeyStoreTests {

	@Test
	public void shouldLoadCertificateIntoKeyStore() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var certBytes = heldCert.certificate().getEncoded();
		var base64Cert = Base64.getEncoder().encodeToString(certBytes);

		var loader = new DerFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64Cert));
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
		var base64Cert = Base64.getEncoder().encodeToString(certBytes);

		var loader = new DerFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64Cert));
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
	public void shouldThrowWhenBase64IsInvalid() throws Exception {

		// arrange

		var loader = new DerFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", "not-valid-der-certificate-data"));
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
		var base64Cert = Base64.getEncoder().encodeToString(certBytes);

		var loader = new DerFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64Cert));
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
		var base64Cert = Base64.getEncoder().encodeToString(certBytes);

		var loader = new DerFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64Cert));
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
		var base64Cert = Base64.getEncoder().encodeToString(certBytes);

		var loader = new DerFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", base64Cert));
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
