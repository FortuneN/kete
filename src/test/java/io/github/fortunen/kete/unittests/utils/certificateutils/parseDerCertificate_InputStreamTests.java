package io.github.fortunen.kete.unittests.utils.certificateutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import io.github.fortunen.kete.utils.CertificateUtils;
import okhttp3.tls.HeldCertificate;
import org.junit.jupiter.api.Test;

public class parseDerCertificate_InputStreamTests {

	@Test
	public void shouldParseCertificateFromInputStream() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("stream-test").build();
		var stream = new ByteArrayInputStream(heldCert.certificate().getEncoded());

		// act

		var result = CertificateUtils.parseDerCertificate(stream);

		// assert

		assertThat(result)
			.as("Should parse certificate from InputStream")
			.isNotNull();
		assertThat(result.getSubjectX500Principal().getName())
			.as("Should preserve subject")
			.contains("CN=stream-test");
	}

	@Test
	public void shouldPreserveIssuerFromInputStream() throws Exception {

		// arrange

		var ca = new HeldCertificate.Builder().commonName("ca-stream").certificateAuthority(1).build();
		var leaf = new HeldCertificate.Builder().commonName("leaf-stream").signedBy(ca).build();
		var stream = new ByteArrayInputStream(leaf.certificate().getEncoded());

		// act

		var result = CertificateUtils.parseDerCertificate(stream);

		// assert

		assertThat(result.getIssuerX500Principal().getName())
			.as("Should preserve issuer")
			.contains("CN=ca-stream");
	}

	@Test
	public void shouldReturnX509Type() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("type-check").build();
		var stream = new ByteArrayInputStream(heldCert.certificate().getEncoded());

		// act

		var result = CertificateUtils.parseDerCertificate(stream);

		// assert

		assertThat(result.getType())
			.as("Should be X.509 type")
			.isEqualTo("X.509");
	}

	@Test
	public void shouldThrowForNullStream() {

		// act

		var thrown = catchThrowable(() -> CertificateUtils.parseDerCertificate((InputStream) null));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("stream is required");
	}

	@Test
	public void shouldThrowForEmptyStream() {

		// arrange

		var emptyStream = new ByteArrayInputStream(new byte[0]);

		// act

		var thrown = catchThrowable(() -> CertificateUtils.parseDerCertificate(emptyStream));

		// assert

		assertThat(thrown)
			.as("Should throw for empty stream")
			.isNotNull();
	}

	@Test
	public void shouldThrowForInvalidStreamData() {

		// arrange

		var invalidStream = new ByteArrayInputStream(new byte[] { 0x00, 0x01, 0x02 });

		// act

		var thrown = catchThrowable(() -> CertificateUtils.parseDerCertificate(invalidStream));

		// assert

		assertThat(thrown)
			.as("Should throw for invalid data")
			.isNotNull();
	}
}
