package io.github.fortunen.kete.unittests.utils.certificateutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.security.KeyPairGenerator;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.CertificateUtils;

public class convertPkcs8ToPkcs1PemTests {

	@Test
	public void shouldConvertRsaPrivateKeyToPkcs1Pem() throws Exception {

		// arrange

		var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		var keyPair = keyPairGenerator.generateKeyPair();
		var privateKey = keyPair.getPrivate();

		// act

		var result = CertificateUtils.convertPkcs8ToPkcs1Pem(privateKey);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).startsWith("-----BEGIN RSA PRIVATE KEY-----\n");
		assertThat(result).endsWith("-----END RSA PRIVATE KEY-----\n");
		assertThat(result).contains("\n");
	}

	@Test
	public void shouldProduceDifferentOutputThanPkcs8Input() throws Exception {

		// arrange

		var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		var keyPair = keyPairGenerator.generateKeyPair();
		var privateKey = keyPair.getPrivate();

		// act

		var result = CertificateUtils.convertPkcs8ToPkcs1Pem(privateKey);

		// assert

		assertThat(result).doesNotContain("BEGIN PRIVATE KEY");
		assertThat(result).doesNotContain("END PRIVATE KEY");
	}

	@Test
	public void shouldProduceValidBase64Content() throws Exception {

		// arrange

		var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		var keyPair = keyPairGenerator.generateKeyPair();
		var privateKey = keyPair.getPrivate();

		// act

		var result = CertificateUtils.convertPkcs8ToPkcs1Pem(privateKey);

		// assert

		var base64Content = result
			.replace("-----BEGIN RSA PRIVATE KEY-----\n", "")
			.replace("\n-----END RSA PRIVATE KEY-----\n", "")
			.replace("\n", "");

		assertThat(base64Content).matches("[A-Za-z0-9+/=]+");
	}

	@Test
	public void shouldFormatBase64With64CharacterLines() throws Exception {

		// arrange

		var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		var keyPair = keyPairGenerator.generateKeyPair();
		var privateKey = keyPair.getPrivate();

		// act

		var result = CertificateUtils.convertPkcs8ToPkcs1Pem(privateKey);

		// assert

		var lines = result.split("\n");

		for (int i = 1; i < lines.length - 1; i++) {
			if (i < lines.length - 2) {
				assertThat(lines[i].length()).isEqualTo(64);
			}
		}
	}

	@Test
	public void shouldThrowWhenPrivateKeyIsNotRsa() throws Exception {

		// arrange

		var keyPairGenerator = KeyPairGenerator.getInstance("EC");
		keyPairGenerator.initialize(256);
		var keyPair = keyPairGenerator.generateKeyPair();
		var ecPrivateKey = keyPair.getPrivate();

		// act

		var thrown = catchThrowable(() -> {
			CertificateUtils.convertPkcs8ToPkcs1Pem(ecPrivateKey);
		});

		// assert

		assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
		assertThat(thrown.getMessage()).isEqualTo("Only RSA private keys can be converted to PKCS#1 format");
	}

	@Test
	public void shouldWorkWithSmallRsaKeySize() throws Exception {

		// arrange

		var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(1024);
		var keyPair = keyPairGenerator.generateKeyPair();
		var privateKey = keyPair.getPrivate();

		// act

		var result = CertificateUtils.convertPkcs8ToPkcs1Pem(privateKey);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).startsWith("-----BEGIN RSA PRIVATE KEY-----\n");
		assertThat(result).endsWith("-----END RSA PRIVATE KEY-----\n");
	}

	@Test
	public void shouldWorkWithLargeRsaKeySize() throws Exception {

		// arrange

		var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(4096);
		var keyPair = keyPairGenerator.generateKeyPair();
		var privateKey = keyPair.getPrivate();

		// act

		var result = CertificateUtils.convertPkcs8ToPkcs1Pem(privateKey);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).startsWith("-----BEGIN RSA PRIVATE KEY-----\n");
		assertThat(result).endsWith("-----END RSA PRIVATE KEY-----\n");
	}

	@Test
	public void shouldProduceConsistentOutputForSameKey() throws Exception {

		// arrange

		var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		var keyPair = keyPairGenerator.generateKeyPair();
		var privateKey = keyPair.getPrivate();

		// act

		var result1 = CertificateUtils.convertPkcs8ToPkcs1Pem(privateKey);
		var result2 = CertificateUtils.convertPkcs8ToPkcs1Pem(privateKey);

		// assert

		assertThat(result1).isEqualTo(result2);
	}

	@Test
	public void shouldProduceDifferentOutputForDifferentKeys() throws Exception {

		// arrange

		var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		var keyPair1 = keyPairGenerator.generateKeyPair();
		var keyPair2 = keyPairGenerator.generateKeyPair();

		// act

		var result1 = CertificateUtils.convertPkcs8ToPkcs1Pem(keyPair1.getPrivate());
		var result2 = CertificateUtils.convertPkcs8ToPkcs1Pem(keyPair2.getPrivate());

		// assert

		assertThat(result1).isNotEqualTo(result2);
	}

	@Test
	public void shouldThrowForNullPrivateKey() {

		// act

		var thrown = catchThrowable(() -> CertificateUtils.convertPkcs8ToPkcs1Pem(null));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("privateKey is required");
	}
}
