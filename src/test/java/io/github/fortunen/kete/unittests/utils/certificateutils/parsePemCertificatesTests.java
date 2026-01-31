package io.github.fortunen.kete.unittests.utils.certificateutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.CertificateUtils;
import okhttp3.tls.HeldCertificate;
import org.junit.jupiter.api.Test;

public class parsePemCertificatesTests {

	@Test
	public void shouldParseSingleCertificate() {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("test").build();
		var pemContent = heldCert.certificatePem();

		// act

		var result = CertificateUtils.parsePemCertificates(pemContent);

		// assert

		assertThat(result)
			.as("Should parse single certificate")
			.hasSize(1);
		assertThat(result.get(0).getSubjectX500Principal().getName())
			.as("Should have correct subject")
			.contains("CN=test");
	}

	@Test
	public void shouldParseMultipleCertificates() {

		// arrange

		var cert1 = new HeldCertificate.Builder().commonName("cert1").build();
		var cert2 = new HeldCertificate.Builder().commonName("cert2").build();
		var pemContent = cert1.certificatePem() + "\n" + cert2.certificatePem();

		// act

		var result = CertificateUtils.parsePemCertificates(pemContent);

		// assert

		assertThat(result)
			.as("Should parse multiple certificates")
			.hasSize(2);
	}

	@Test
	public void shouldReturnEmptyListForNoCertificates() {

		// arrange

		var pemContent = "not a certificate";

		// act

		var result = CertificateUtils.parsePemCertificates(pemContent);

		// assert

		assertThat(result)
			.as("Should return empty list for no certificates")
			.isEmpty();
	}

	@Test
	public void shouldReturnEmptyListForEmptyContent() {

		// arrange

		var pemContent = "";

		// act

		var result = CertificateUtils.parsePemCertificates(pemContent);

		// assert

		assertThat(result)
			.as("Should return empty list for empty content")
			.isEmpty();
	}
}
