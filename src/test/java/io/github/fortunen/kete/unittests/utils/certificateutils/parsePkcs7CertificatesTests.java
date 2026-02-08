package io.github.fortunen.kete.unittests.utils.certificateutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.CertificateUtils;
import okhttp3.tls.HeldCertificate;

public class parsePkcs7CertificatesTests {

	@Test
	public void shouldParseSingleCertificateFromPkcs7() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("pkcs7-test").build();
		var pkcs7Bytes = createPkcs7(List.of(heldCert));

		// act

		var result = CertificateUtils.parsePkcs7Certificates(new ByteArrayInputStream(pkcs7Bytes));

		// assert

		assertThat(result)
			.as("Should parse single certificate from PKCS#7")
			.hasSize(1);
		assertThat(result.iterator().next().getSubjectX500Principal().getName())
			.as("Should preserve subject")
			.contains("CN=pkcs7-test");
	}

	@Test
	public void shouldParseMultipleCertificatesFromPkcs7() throws Exception {

		// arrange

		var cert1 = new HeldCertificate.Builder().commonName("pkcs7-cert1").build();
		var cert2 = new HeldCertificate.Builder().commonName("pkcs7-cert2").build();
		var pkcs7Bytes = createPkcs7(List.of(cert1, cert2));

		// act

		var result = CertificateUtils.parsePkcs7Certificates(new ByteArrayInputStream(pkcs7Bytes));

		// assert

		assertThat(result)
			.as("Should parse multiple certificates from PKCS#7")
			.hasSize(2);
	}

	@Test
	public void shouldReturnX509CertificateInstances() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("type-test").build();
		var pkcs7Bytes = createPkcs7(List.of(heldCert));

		// act

		var result = CertificateUtils.parsePkcs7Certificates(new ByteArrayInputStream(pkcs7Bytes));

		// assert

		for (var cert : result) {
			assertThat(cert.getType())
				.as("Should be X.509 type")
				.isEqualTo("X.509");
		}
	}

	@Test
	public void shouldThrowForNullStream() {

		// act

		var thrown = catchThrowable(() -> CertificateUtils.parsePkcs7Certificates(null));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("stream is required");
	}

	@Test
	public void shouldReturnEmptyForEmptyStream() throws Exception {

		// arrange

		var emptyStream = new ByteArrayInputStream(new byte[0]);

		// act

		var result = CertificateUtils.parsePkcs7Certificates(emptyStream);

		// assert

		assertThat(result)
			.as("Should return empty collection for empty stream")
			.isEmpty();
	}

	private byte[] createPkcs7(List<HeldCertificate> heldCerts) throws Exception {
		var javaCerts = heldCerts.stream().map(HeldCertificate::certificate).toList();
		var certStore = new JcaCertStore(javaCerts);
		var generator = new CMSSignedDataGenerator();
		generator.addCertificates(certStore);
		var signedData = generator.generate(new CMSProcessableByteArray(new byte[0]), false);
		return signedData.getEncoded();
	}
}
