package io.github.fortunen.kete.unittests.tlsmaterial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.TlsMaterial;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class updateKafkaConfigurationTests {

	@Test
	void shouldThrowWhenConfigurationIsNull() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		// act & assert

		assertThatThrownBy(() -> tls.updateKafkaConfiguration(null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	void shouldNotModifyConfigurationWhenTlsDisabled() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(false)
			.build();

		var properties = new Properties();
		properties.put("security.protocol", "SSL");

		// act

		tls.updateKafkaConfiguration(properties);

		// assert

		assertThat(properties)
			.hasSize(1)
			.containsEntry("security.protocol", "SSL");
	}

	@Test
	void shouldThrowWhenSecurityProtocolMissing() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		var properties = new Properties();

		// act & assert

		assertThatThrownBy(() -> tls.updateKafkaConfiguration(properties))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage(".destination.security.protocol is required");
	}

	@Test
	void shouldThrowWhenSecurityProtocolBlank() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		var properties = new Properties();
		properties.put("security.protocol", "  ");

		// act & assert

		assertThatThrownBy(() -> tls.updateKafkaConfiguration(properties))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage(".destination.security.protocol is required");
	}

	@Test
	void shouldThrowWhenSecurityProtocolIsInvalid() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.build();

		var properties = new Properties();
		properties.put("security.protocol", "PLAINTEXT");

		// act & assert

		assertThatThrownBy(() -> tls.updateKafkaConfiguration(properties))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage(".destination.security.protocol must be 'SSL' or 'SASL_SSL' when TLS is enabled");
	}

	@Test
	void shouldAcceptSslSecurityProtocol() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withWriteFiles(true)
			.withServerHostNames(new String[]{"localhost", "127.0.0.1", "host.docker.internal"})
			.build();

		var properties = new Properties();
		properties.put("security.protocol", "SSL");

		// act

		tls.updateKafkaConfiguration(properties);

		// assert - should have updated properties

		assertThat(properties.getProperty("ssl.protocol"))
			.isEqualTo(TlsMaterial.DEFAULT_TLS_VERSION);
	}

	@Test
	void shouldAcceptSaslSslSecurityProtocol() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withWriteFiles(true)
			.withServerHostNames(new String[]{"localhost", "127.0.0.1", "host.docker.internal"})
			.build();

		var properties = new Properties();
		properties.put("security.protocol", "SASL_SSL");

		// act

		tls.updateKafkaConfiguration(properties);

		// assert - should have updated properties

		assertThat(properties.getProperty("ssl.protocol"))
			.isEqualTo(TlsMaterial.DEFAULT_TLS_VERSION);
	}

	@Test
	void shouldSetSslProtocolFromVersion() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withVersion("TLSv1.3")
			.withWriteFiles(true)
			.withServerHostNames(new String[]{"localhost", "127.0.0.1", "host.docker.internal"})
			.build();

		var properties = new Properties();
		properties.put("security.protocol", "SSL");

		// act

		tls.updateKafkaConfiguration(properties);

		// assert

		assertThat(properties.getProperty("ssl.protocol"))
			.isEqualTo("TLSv1.3");
	}

	@Test
	void shouldSetTrustStoreProperties() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withWriteFiles(true)
			.withTrustStoreType("PKCS12")
			.withTrustStorePassword("trustPass")
			.withTrustManagerAlgorithm("PKIX")
			.withServerHostNames(new String[]{"localhost", "127.0.0.1", "host.docker.internal"})
			.build();

		var properties = new Properties();
		properties.put("security.protocol", "SSL");

		// act

		tls.updateKafkaConfiguration(properties);

		// assert

		assertThat(properties.getProperty("ssl.truststore.type"))
			.isEqualTo("PKCS12");
		assertThat(properties.getProperty("ssl.truststore.location"))
			.isNotBlank();
		assertThat(properties.getProperty("ssl.truststore.password"))
			.isEqualTo("trustPass");
		assertThat(properties.getProperty("ssl.trustmanager.algorithm"))
			.isEqualTo("PKIX");
	}

	@Test
	void shouldSetKeyStoreProperties() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withWriteFiles(true)
			.withKeyStoreType("PKCS12")
			.withKeyStorePassword("keyStorePass")
			.withKeyPassword("keyPass")
			.withKeyManagerAlgorithm("PKIX")
			.withServerHostNames(new String[]{"localhost", "127.0.0.1", "host.docker.internal"})
			.build();

		var properties = new Properties();
		properties.put("security.protocol", "SSL");

		// act

		tls.updateKafkaConfiguration(properties);

		// assert

		assertThat(properties.getProperty("ssl.keystore.type"))
			.isEqualTo("PKCS12");
		assertThat(properties.getProperty("ssl.keystore.location"))
			.isNotBlank();
		assertThat(properties.getProperty("ssl.keystore.password"))
			.isEqualTo("keyStorePass");
		assertThat(properties.getProperty("ssl.key.password"))
			.isEqualTo("keyPass");
		assertThat(properties.getProperty("ssl.keymanager.algorithm"))
			.isEqualTo("PKIX");
	}

	@Test
	void shouldNotSetPropertiesWhenValuesAreBlank() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withWriteFiles(false) // no file paths
			.build();

		var properties = new Properties();
		properties.put("security.protocol", "SSL");

		// act

		tls.updateKafkaConfiguration(properties);

		// assert

		assertThat(properties.getProperty("ssl.truststore.location"))
			.isNull();
		assertThat(properties.getProperty("ssl.keystore.location"))
			.isNull();
	}

	@Test
	void shouldSetAllPropertiesWhenFullyConfigured() {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withVersion("TLSv1.2")
			.withWriteFiles(true)
			.withTrustStoreType("JKS")
			.withTrustStorePassword("trust123")
			.withTrustManagerAlgorithm("SunX509")
			.withKeyStoreType("JKS")
			.withKeyStorePassword("keyStore123")
			.withKeyPassword("key123")
			.withKeyManagerAlgorithm("SunX509")
			.withServerHostNames(new String[]{"localhost", "127.0.0.1", "host.docker.internal"})
			.build();

		var properties = new Properties();
		properties.put("security.protocol", "SSL");

		// act

		tls.updateKafkaConfiguration(properties);

		// assert

		assertThat(properties.getProperty("ssl.protocol")).isEqualTo("TLSv1.2");
		assertThat(properties.getProperty("ssl.truststore.type")).isEqualTo("JKS");
		assertThat(properties.getProperty("ssl.truststore.location")).isNotBlank();
		assertThat(properties.getProperty("ssl.truststore.password")).isEqualTo("trust123");
		assertThat(properties.getProperty("ssl.trustmanager.algorithm")).isEqualTo("SunX509");
		assertThat(properties.getProperty("ssl.keystore.type")).isEqualTo("JKS");
		assertThat(properties.getProperty("ssl.keystore.location")).isNotBlank();
		assertThat(properties.getProperty("ssl.keystore.password")).isEqualTo("keyStore123");
		assertThat(properties.getProperty("ssl.key.password")).isEqualTo("key123");
		assertThat(properties.getProperty("ssl.keymanager.algorithm")).isEqualTo("SunX509");
	}
}
