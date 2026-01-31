package io.github.fortunen.kete.unittests.utils.certificateutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.certificateloaders.DerFileBase64CertificateLoader;
import io.github.fortunen.kete.certificateloaders.PemFileBase64CertificateLoader;
import io.github.fortunen.kete.certificateloaders.Pkcs12FileBase64CertificateLoader;
import io.github.fortunen.kete.utils.CertificateUtils;
import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import okhttp3.tls.HeldCertificate;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

public class createCertificateLoaderTests {

	@Test
	public void shouldCreatePemFileBase64CertificateLoader() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var pemCert = heldCert.certificatePem();

		var config = new MapConfiguration(Map.of(
			"kind", "pem-file-base64",
			"base64", Base64.getEncoder().encodeToString(pemCert.getBytes())
		));

		// act

		var result = CertificateUtils.createCertificateLoader(config);

		// assert

		assertThat(result)
			.as("Should return a PEM file base64 certificate loader")
			.isNotNull()
			.isInstanceOf(PemFileBase64CertificateLoader.class);
	}

	@Test
	public void shouldCreateDerFileBase64CertificateLoader() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var certBytes = heldCert.certificate().getEncoded();

		var config = new MapConfiguration(Map.of(
			"kind", "der-file-base64",
			"base64", Base64.getEncoder().encodeToString(certBytes)
		));

		// act

		var result = CertificateUtils.createCertificateLoader(config);

		// assert

		assertThat(result)
			.as("Should return a DER file base64 certificate loader")
			.isNotNull()
			.isInstanceOf(DerFileBase64CertificateLoader.class);
	}

	@Test
	public void shouldCreatePkcs12FileBase64CertificateLoader() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();

		// Create a PKCS12 keystore
		var pkcs12KeyStore = KeyStore.getInstance("PKCS12");
		pkcs12KeyStore.load(null, "test".toCharArray());
		pkcs12KeyStore.setKeyEntry(
			"test-key",
			heldCert.keyPair().getPrivate(),
			"test".toCharArray(),
			new Certificate[] { heldCert.certificate() }
		);

		var baos = new ByteArrayOutputStream();
		pkcs12KeyStore.store(baos, "test".toCharArray());
		var base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

		var config = new MapConfiguration(Map.of(
			"kind", "pkcs12-file-base64",
			"base64", base64
		));

		// act

		var result = CertificateUtils.createCertificateLoader(config);

		// assert

		assertThat(result)
			.as("Should return a PKCS12 file base64 certificate loader")
			.isNotNull()
			.isInstanceOf(Pkcs12FileBase64CertificateLoader.class);
	}

	@Test
	public void shouldThrowWhenConfigurationIsNull() {

		// act

		var thrown = catchThrowable(() -> CertificateUtils.createCertificateLoader(null));

		// assert

		assertThat(thrown)
			.as("Should throw exception when configuration is null")
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	public void shouldThrowWhenKindIsMissing() {

		// arrange

		var config = new MapConfiguration(Map.of("base64", "some-data"));

		// act

		var thrown = catchThrowable(() -> CertificateUtils.createCertificateLoader(config));

		// assert

		assertThat(thrown)
			.as("Should throw exception when kind is missing")
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("kind is required");
	}

	@Test
	public void shouldThrowWhenKindIsBlank() {

		// arrange

		var config = new MapConfiguration(Map.of(
			"kind", "   ",
			"base64", "some-data"
		));

		// act

		var thrown = catchThrowable(() -> CertificateUtils.createCertificateLoader(config));

		// assert

		assertThat(thrown)
			.as("Should throw exception when kind is blank")
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("kind is required");
	}

	@Test
	public void shouldThrowWhenKindIsUnknown() {

		// arrange

		var config = new MapConfiguration(Map.of(
			"kind", "unknown-loader-type",
			"base64", "some-data"
		));

		// act

		var thrown = catchThrowable(() -> CertificateUtils.createCertificateLoader(config));

		// assert

		assertThat(thrown)
			.as("Should throw exception when kind is unknown")
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("certificate loader kind 'unknown-loader-type' not found");
	}

	@Test
	public void shouldInitializeTheLoaderAfterCreation() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var certBytes = heldCert.certificate().getEncoded();

		var config = new MapConfiguration(Map.of(
			"kind", "der-file-base64",
			"base64", Base64.getEncoder().encodeToString(certBytes)
		));

		// act

		var result = CertificateUtils.createCertificateLoader(config);

		// assert - verify the loader was initialized by checking it can load a keystore

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		result.loadKeyStore(keyStore, null);

		assertThat(keyStore.containsAlias("cert"))
			.as("Loader should be initialized and able to load with auto-generated alias")
			.isTrue();
	}

	@Test
	public void shouldSetConfigurationOnTheLoader() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var certBytes = heldCert.certificate().getEncoded();

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "der-file-base64");
		configMap.put("base64", Base64.getEncoder().encodeToString(certBytes));
		var config = new MapConfiguration(configMap);

		// act

		var result = CertificateUtils.createCertificateLoader(config);

		// assert

		assertThat(result.getConfiguration())
			.as("Loader should have configuration set")
			.isNotNull();
	}
}
