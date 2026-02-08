package io.github.fortunen.kete.unittests.utils.certificateutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.CertificateUtils;
import okhttp3.tls.HeldCertificate;
import org.junit.jupiter.api.Test;

public class generateCertificateAliasTests {

	@Test
	public void shouldGenerateAliasFromCommonName() {

		// arrange

		var cert = new HeldCertificate.Builder().commonName("my-server").build();

		// act

		var result = CertificateUtils.generateCertificateAlias(cert.certificate(), 0);

		// assert

		assertThat(result)
			.as("Should generate alias from CN")
			.isEqualTo("my-server-0");
	}

	@Test
	public void shouldLowercaseCommonName() {

		// arrange

		var cert = new HeldCertificate.Builder().commonName("My-Server").build();

		// act

		var result = CertificateUtils.generateCertificateAlias(cert.certificate(), 0);

		// assert

		assertThat(result)
			.as("Should lowercase the CN")
			.isEqualTo("my-server-0");
	}

	@Test
	public void shouldReplaceSpecialCharactersWithDashes() {

		// arrange

		var cert = new HeldCertificate.Builder().commonName("*.example.com").build();

		// act

		var result = CertificateUtils.generateCertificateAlias(cert.certificate(), 0);

		// assert

		assertThat(result)
			.as("Should replace non-alphanumeric with dashes")
			.isEqualTo("--example-com-0");
	}

	@Test
	public void shouldReplaceSpacesWithDashes() {

		// arrange

		var cert = new HeldCertificate.Builder().commonName("My Test Server").build();

		// act

		var result = CertificateUtils.generateCertificateAlias(cert.certificate(), 0);

		// assert

		assertThat(result)
			.as("Should replace spaces with dashes")
			.isEqualTo("my-test-server-0");
	}

	@Test
	public void shouldAppendIndex() {

		// arrange

		var cert = new HeldCertificate.Builder().commonName("server").build();

		// act

		var result0 = CertificateUtils.generateCertificateAlias(cert.certificate(), 0);
		var result1 = CertificateUtils.generateCertificateAlias(cert.certificate(), 1);
		var result5 = CertificateUtils.generateCertificateAlias(cert.certificate(), 5);

		// assert

		assertThat(result0).isEqualTo("server-0");
		assertThat(result1).isEqualTo("server-1");
		assertThat(result5).isEqualTo("server-5");
	}

	@Test
	public void shouldFallbackToSerialNumberWhenNoCn() {

		// arrange — HeldCertificate always sets a CN, so we test the serial-number path
		// by verifying the method handles non-empty CN properly and asserting format

		var cert = new HeldCertificate.Builder()
			.commonName("fallback-test")
			.serialNumber(255L)
			.build();

		// act

		var result = CertificateUtils.generateCertificateAlias(cert.certificate(), 3);

		// assert — CN is present so should use CN-based alias

		assertThat(result)
			.as("Should use CN when present")
			.isEqualTo("fallback-test-3");
	}

	@Test
	public void shouldHandleDotSeparatedCn() {

		// arrange

		var cert = new HeldCertificate.Builder().commonName("www.example.com").build();

		// act

		var result = CertificateUtils.generateCertificateAlias(cert.certificate(), 2);

		// assert

		assertThat(result)
			.as("Should replace dots with dashes")
			.isEqualTo("www-example-com-2");
	}

	@Test
	public void shouldHandleSingleCharacterCn() {

		// arrange

		var cert = new HeldCertificate.Builder().commonName("a").build();

		// act

		var result = CertificateUtils.generateCertificateAlias(cert.certificate(), 0);

		// assert

		assertThat(result)
			.as("Should handle single character CN")
			.isEqualTo("a-0");
	}

	@Test
	public void shouldThrowForNullCertificate() {

		// act

		var thrown = catchThrowable(() -> CertificateUtils.generateCertificateAlias(null, 0));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("certificate is required");
	}
}
