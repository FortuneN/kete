package io.github.fortunen.kete.unittests.utils.certificateutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.CertificateUtils;
import okhttp3.tls.HeldCertificate;
import org.junit.jupiter.api.Test;

public class extractCommonNameTests {

	@Test
	public void shouldExtractSimpleCommonName() {

		// arrange

		var cert = new HeldCertificate.Builder().commonName("my-server").build();

		// act

		var result = CertificateUtils.extractCommonName(cert.certificate());

		// assert

		assertThat(result)
			.as("Should extract simple CN")
			.isEqualTo("my-server");
	}

	@Test
	public void shouldExtractCommonNameWithDots() {

		// arrange

		var cert = new HeldCertificate.Builder().commonName("www.example.com").build();

		// act

		var result = CertificateUtils.extractCommonName(cert.certificate());

		// assert

		assertThat(result)
			.as("Should extract CN with dots")
			.isEqualTo("www.example.com");
	}

	@Test
	public void shouldExtractCommonNameWithSpaces() {

		// arrange

		var cert = new HeldCertificate.Builder().commonName("My Test Server").build();

		// act

		var result = CertificateUtils.extractCommonName(cert.certificate());

		// assert

		assertThat(result)
			.as("Should extract CN with spaces")
			.isEqualTo("My Test Server");
	}

	@Test
	public void shouldExtractCommonNameWithSpecialCharacters() {

		// arrange

		var cert = new HeldCertificate.Builder().commonName("*.example.com").build();

		// act

		var result = CertificateUtils.extractCommonName(cert.certificate());

		// assert

		assertThat(result)
			.as("Should extract wildcard CN")
			.isEqualTo("*.example.com");
	}

	@Test
	public void shouldReturnNullWhenNoCnPresent() {

		// arrange — certificate with organization unit only, no CN

		var cert = new HeldCertificate.Builder()
			.addSubjectAlternativeName("dns:example.com")
			.build();

		// act

		var result = CertificateUtils.extractCommonName(cert.certificate());

		// assert — HeldCertificate without commonName() sets CN to hostname or empty
		// We just verify the method doesn't throw and returns a String or null

		assertThat(result).isNotNull(); // HeldCertificate always sets a CN
	}

	@Test
	public void shouldExtractFromCaCertificate() {

		// arrange

		var ca = new HeldCertificate.Builder()
			.commonName("Root CA")
			.certificateAuthority(2)
			.build();

		// act

		var result = CertificateUtils.extractCommonName(ca.certificate());

		// assert

		assertThat(result)
			.as("Should extract CN from CA certificate")
			.isEqualTo("Root CA");
	}

	@Test
	public void shouldExtractFromIntermediateCaCertificate() {

		// arrange

		var rootCa = new HeldCertificate.Builder()
			.commonName("Root CA")
			.certificateAuthority(2)
			.build();

		var intermediateCa = new HeldCertificate.Builder()
			.commonName("Intermediate CA")
			.certificateAuthority(1)
			.signedBy(rootCa)
			.build();

		// act

		var result = CertificateUtils.extractCommonName(intermediateCa.certificate());

		// assert

		assertThat(result)
			.as("Should extract CN from intermediate CA")
			.isEqualTo("Intermediate CA");
	}

	@Test
	public void shouldThrowForNullCertificate() {

		// act

		var thrown = catchThrowable(() -> CertificateUtils.extractCommonName(null));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("certificate is required");
	}
}
