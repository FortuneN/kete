package io.github.fortunen.kete.unittests.tlsmaterial;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;

class systemTrustedCertificatesTests {

	@Test
	void shouldLoadSystemTrustedCertificates() {

		// act & assert

		assertThat(TlsMaterial.SYSTEM_TRUSTED_CERTIFICATES)
			.as("System trusted certificates should be loaded")
			.isNotNull()
			.isNotEmpty();
	}

	@Test
	void shouldContainReasonableNumberOfCertificates() {

		// act & assert

		assertThat(TlsMaterial.SYSTEM_TRUSTED_CERTIFICATES)
			.as("Should contain at least 50 system trusted certificates")
			.hasSizeGreaterThanOrEqualTo(50);
	}

	@Test
	void shouldContainValidX509Certificates() {

		// act & assert

		assertThat(TlsMaterial.SYSTEM_TRUSTED_CERTIFICATES)
			.as("All entries should be valid X509Certificates")
			.allMatch(cert -> cert instanceof X509Certificate);
	}

	@Test
	void shouldContainRecognizableCANames() {

		// arrange

		var commonCANames = Arrays.asList(
			"DigiCert", "GeoTrust", "VeriSign", "GlobalSign", "Entrust",
			"Baltimore", "GoDaddy", "Starfield", "Comodo", "IdenTrust"
		);

		// act

		var certSubjects = Arrays.stream(TlsMaterial.SYSTEM_TRUSTED_CERTIFICATES)
			.map(cert -> cert.getSubjectX500Principal().getName())
			.collect(Collectors.toList());

		var allSubjects = String.join(" ", certSubjects);

		// assert

		var foundAtLeastOneCA = commonCANames.stream()
			.anyMatch(caName -> allSubjects.contains(caName));

		assertThat(foundAtLeastOneCA)
			.as("Should contain at least one recognizable CA from: " + commonCANames)
			.isTrue();
	}

	@Test
	void shouldHaveNonExpiredCertificates() {

		// arrange

		var currentTime = System.currentTimeMillis();

		// act

		var nonExpiredCount = Arrays.stream(TlsMaterial.SYSTEM_TRUSTED_CERTIFICATES)
			.filter(cert -> cert.getNotAfter().getTime() > currentTime)
			.count();

		// assert

		assertThat(nonExpiredCount)
			.as("Majority of system certificates should not be expired")
			.isGreaterThan(TlsMaterial.SYSTEM_TRUSTED_CERTIFICATES.length / 2);
	}
}
