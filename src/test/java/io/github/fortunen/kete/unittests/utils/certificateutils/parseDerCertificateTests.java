package io.github.fortunen.kete.unittests.utils.certificateutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.CertificateUtils;
import okhttp3.tls.HeldCertificate;
import org.junit.jupiter.api.Test;

public class parseDerCertificateTests {

	@Test
	public void shouldParseDerEncodedCertificate() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("test-der").build();
		var derBytes = heldCert.certificate().getEncoded();

		// act

		var result = CertificateUtils.parseDerCertificate(derBytes);

		// assert

		assertThat(result)
			.as("Should parse DER certificate")
			.isNotNull();
		assertThat(result.getSubjectX500Principal().getName())
			.as("Should preserve subject")
			.contains("CN=test-der");
	}

	@Test
	public void shouldPreserveIssuer() throws Exception {

		// arrange

		var ca = new HeldCertificate.Builder().commonName("my-ca").certificateAuthority(1).build();
		var leaf = new HeldCertificate.Builder().commonName("leaf").signedBy(ca).build();
		var derBytes = leaf.certificate().getEncoded();

		// act

		var result = CertificateUtils.parseDerCertificate(derBytes);

		// assert

		assertThat(result.getIssuerX500Principal().getName())
			.as("Should preserve issuer from CA")
			.contains("CN=my-ca");
	}

	@Test
	public void shouldPreserveSerialNumber() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("serial-test").serialNumber(12345L).build();
		var derBytes = heldCert.certificate().getEncoded();

		// act

		var result = CertificateUtils.parseDerCertificate(derBytes);

		// assert

		assertThat(result.getSerialNumber().longValue())
			.as("Should preserve serial number")
			.isEqualTo(12345L);
	}

	@Test
	public void shouldReturnX509CertificateInstance() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("x509-type").build();
		var derBytes = heldCert.certificate().getEncoded();

		// act

		var result = CertificateUtils.parseDerCertificate(derBytes);

		// assert

		assertThat(result.getType())
			.as("Should return X.509 certificate type")
			.isEqualTo("X.509");
	}

	@Test
	public void shouldThrowForNullInput() {

		// act

		var thrown = catchThrowable(() -> CertificateUtils.parseDerCertificate((byte[]) null));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("derBytes is required");
	}

	@Test
	public void shouldThrowForInvalidDerBytes() {

		// arrange

		var invalidDer = new byte[] { 0x00, 0x01, 0x02, 0x03 };

		// act

		var thrown = catchThrowable(() -> CertificateUtils.parseDerCertificate(invalidDer));

		// assert

		assertThat(thrown)
			.as("Should throw for invalid DER data")
			.isNotNull();
	}

	@Test
	public void shouldThrowForEmptyBytes() {

		// act

		var thrown = catchThrowable(() -> CertificateUtils.parseDerCertificate(new byte[0]));

		// assert

		assertThat(thrown)
			.as("Should throw for empty byte array")
			.isNotNull();
	}
}
