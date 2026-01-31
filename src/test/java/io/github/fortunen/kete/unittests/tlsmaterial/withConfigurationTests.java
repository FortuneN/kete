package io.github.fortunen.kete.unittests.tlsmaterial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.TlsMaterial;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.HashMap;
import okhttp3.tls.HeldCertificate;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class withConfigurationTests {

	@TempDir
	Path tempDir;

	@Test
	void shouldThrowWhenConfigurationIsNull() {

		// act & assert

		assertThatThrownBy(() -> TlsMaterial.builder().withConfiguration(null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	void shouldBuildWithEmptyConfiguration() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act

		var tls = TlsMaterial.builder()
			.withConfiguration(config)
			.build();

		// assert

		assertThat(tls.isEnabled())
			.as("Should be disabled by default")
			.isFalse();
	}

	@Test
	void shouldBuildWithEnabledConfiguration() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", true);
		var config = new MapConfiguration(map);

		// act

		var tls = TlsMaterial.builder()
			.withConfiguration(config)
			.build();

		// assert

		assertThat(tls.isEnabled())
			.as("Should be enabled")
			.isTrue();
		assertThat(tls.getKeyStoreAndTrustStoreSSLContext())
			.as("Should have SSL context")
			.isNotNull();
	}

	@Test
	void shouldBuildWithCustomTlsVersion() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", true);
		map.put("version", "TLSv1.3");
		var config = new MapConfiguration(map);

		// act

		var tls = TlsMaterial.builder()
			.withConfiguration(config)
			.build();

		// assert

		assertThat(tls.getVersion())
			.as("Should use custom TLS version")
			.isEqualTo("TLSv1.3");
	}

	@Test
	void shouldBuildWithTrustStoreFromPemBase64Loader() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("test-ca").build();
		var pemContent = heldCert.certificatePem();
		var base64Pem = Base64.getEncoder().encodeToString(pemContent.getBytes());

		var map = new HashMap<String, Object>();
		map.put("enabled", true);
		map.put("trust-store.loader.kind", "pem-file-base64");
		map.put("trust-store.loader.base64", base64Pem);
		var config = new MapConfiguration(map);

		// act

		var tls = TlsMaterial.builder()
			.withConfiguration(config)
			.build();

		// assert

		assertThat(tls.getTrustStore())
			.as("Should have trust store")
			.isNotNull();
		assertThat(tls.getTrustStore().size())
			.as("Should have certificates in trust store")
			.isGreaterThan(0);
	}

	@Test
	void shouldBuildWithTrustStoreFromPemFilePathLoader() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("test-ca").build();
		var pemFile = tempDir.resolve("ca.pem");
		Files.writeString(pemFile, heldCert.certificatePem());

		var map = new HashMap<String, Object>();
		map.put("enabled", true);
		map.put("trust-store.loader.kind", "pem-file-path");
		map.put("trust-store.loader.path", pemFile.toString());
		var config = new MapConfiguration(map);

		// act

		var tls = TlsMaterial.builder()
			.withConfiguration(config)
			.build();

		// assert

		assertThat(tls.getTrustStore())
			.as("Should have trust store")
			.isNotNull();
	}

	@Test
	void shouldBuildWithKeyStoreFromPkcs12FileLoader() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("test-server").build();
		var pkcs12File = tempDir.resolve("server.p12");
		var password = "testpass";

		var keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(null, password.toCharArray());
		keyStore.setKeyEntry(
			"server",
			heldCert.keyPair().getPrivate(),
			password.toCharArray(),
			new Certificate[]{heldCert.certificate()}
		);

		try (var fos = new FileOutputStream(pkcs12File.toFile())) {
			keyStore.store(fos, password.toCharArray());
		}

		var map = new HashMap<String, Object>();
		map.put("enabled", true);
		map.put("key-store.password", password);
		map.put("key-store.key-password", password);
		map.put("key-store.loader.kind", "pkcs12-file-path");
		map.put("key-store.loader.path", pkcs12File.toString());
		var config = new MapConfiguration(map);

		// act

		var tls = TlsMaterial.builder()
			.withConfiguration(config)
			.build();

		// assert

		assertThat(tls.getKeyStore())
			.as("Should have key store")
			.isNotNull();
		assertThat(tls.getKeyStore().containsAlias("server"))
			.as("Should have server key entry")
			.isTrue();
	}

	@Test
	void shouldBuildWithTrustStoreTypeConfiguration() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", true);
		map.put("trust-store.type", "PKCS12");
		var config = new MapConfiguration(map);

		// act

		var tls = TlsMaterial.builder()
			.withConfiguration(config)
			.build();

		// assert

		assertThat(tls.getTrustStoreType())
			.as("Should use configured trust store type")
			.isEqualTo("PKCS12");
	}

	@Test
	void shouldBuildWithKeyStoreTypeConfiguration() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", true);
		map.put("key-store.type", "PKCS12");
		var config = new MapConfiguration(map);

		// act

		var tls = TlsMaterial.builder()
			.withConfiguration(config)
			.build();

		// assert

		assertThat(tls.getKeyStoreType())
			.as("Should use configured key store type")
			.isEqualTo("PKCS12");
	}

	@Test
	void shouldBuildWithCustomAlgorithms() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", true);
		map.put("trust-store.trust-manager-algorithm", "PKIX");
		map.put("key-store.key-manager-algorithm", "SunX509");
		var config = new MapConfiguration(map);

		// act

		var tls = TlsMaterial.builder()
			.withConfiguration(config)
			.build();

		// assert

		assertThat(tls.getTrustManagerAlgorithm())
			.as("Should use configured trust manager algorithm")
			.isEqualTo("PKIX");
		assertThat(tls.getKeyManagerAlgorithm())
			.as("Should use configured key manager algorithm")
			.isEqualTo("SunX509");
	}
}
