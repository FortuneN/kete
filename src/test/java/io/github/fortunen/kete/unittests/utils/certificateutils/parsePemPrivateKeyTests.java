package io.github.fortunen.kete.unittests.utils.certificateutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.utils.CertificateUtils;
import okhttp3.tls.HeldCertificate;
import org.junit.jupiter.api.Test;

public class parsePemPrivateKeyTests {

	@Test
	public void shouldParsePrivateKey() {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("test").build();
		var pemContent = heldCert.certificatePem() + "\n" + heldCert.privateKeyPkcs8Pem();

		// act

		var result = CertificateUtils.parsePemPrivateKey(pemContent);

		// assert

		assertThat(result)
			.as("Should parse private key")
			.isNotNull();
		assertThat(result.getAlgorithm())
			.as("Should have correct algorithm")
		.isIn("RSA", "EC", "ECDSA");
	}

	@Test
	public void shouldReturnNullForNoPrivateKey() {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("test").build();
		var pemContent = heldCert.certificatePem();

		// act

		var result = CertificateUtils.parsePemPrivateKey(pemContent);

		// assert

		assertThat(result)
			.as("Should return null when no private key present")
			.isNull();
	}

	@Test
	public void shouldReturnNullForEmptyContent() {

		// act

		var result = CertificateUtils.parsePemPrivateKey("");

		// assert

		assertThat(result)
			.as("Should return null for empty content")
			.isNull();
	}

	@Test
	public void shouldReturnNullForInvalidPemContent() {

		// act

		var result = CertificateUtils.parsePemPrivateKey("not a pem content");

		// assert

		assertThat(result)
			.as("Should return null for invalid PEM content")
			.isNull();
	}

	@Test
	public void shouldThrowForMalformedPrivateKey() {

		// arrange

		var malformedPem = "-----BEGIN PRIVATE KEY-----\ninvalid-base64-content-that-will-fail-to-decode\n-----END PRIVATE KEY-----";

		// act & assert

		assertThatThrownBy(() -> CertificateUtils.parsePemPrivateKey(malformedPem))
			.as("Should throw for malformed private key with invalid base64")
			.isInstanceOf(RuntimeException.class);
	}

	@Test
	public void shouldThrowForEncryptedPrivateKey() {

		// arrange

		// Encrypted private key PEM header - BouncyCastle will either:
		// 1. Recognize it as PKCS8EncryptedPrivateKeyInfo and we throw IllegalStateException
		// 2. Fail to parse corrupted structure and throw PEMException
		// Either way, it must not silently return null or succeed
		var encryptedPem = """
			-----BEGIN ENCRYPTED PRIVATE KEY-----
			MIICojAcBgoqhkiG9w0BDAEDMA4ECHl5dl+p10BKAQIDAV0EggKAHZYvP3pT+GWf
			tbVkKnxCzLlXeW7ykr6G7cN0yf2LBGLb2M8HmxjF+lGmMKvGY7s5M6PVbPF5mYp6
			Xk5FKy5vbqbqnrPl3hP7B+tQfYdNj3Ff1oj3pP3v4P3P7GzA9fvP3P3P7GzA9fvP
			3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P
			7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA
			9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP
			3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P
			7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA
			9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP
			3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P
			7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA
			9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP
			3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P
			7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA9fvP3P3P7GzA
			-----END ENCRYPTED PRIVATE KEY-----
			""";

		// act & assert

		assertThatThrownBy(() -> CertificateUtils.parsePemPrivateKey(encryptedPem))
			.as("Should throw for encrypted private key")
			.isInstanceOf(Exception.class);
	}

	@Test
	public void shouldThrowForNullContent() {

		// act & assert

		assertThatThrownBy(() -> CertificateUtils.parsePemPrivateKey(null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("pemContent is required");
	}
}
