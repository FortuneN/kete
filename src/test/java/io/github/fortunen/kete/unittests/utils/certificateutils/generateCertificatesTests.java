package io.github.fortunen.kete.unittests.utils.certificateutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.CertificateUtils;

public class generateCertificatesTests {

	@Test
	public void shouldGenerateCertificateAuthority() throws Exception {

		// act

		var ca = CertificateUtils.generateCertificateAuthority("Test CA");

		// assert

		assertThat(ca.certificate().getSubjectX500Principal().getName()).isEqualTo("CN=Test CA");
		assertThat(ca.certificate().getBasicConstraints()).isGreaterThanOrEqualTo(0);
		assertThatCode(() -> ca.certificate().verify(ca.keyPair().getPublic())).doesNotThrowAnyException();
		assertThat(ca.certificatePem()).startsWith("-----BEGIN CERTIFICATE-----");
		assertThat(ca.privateKeyPkcs8Pem()).startsWith("-----BEGIN PRIVATE KEY-----");
	}

	@Test
	public void shouldGenerateCertificateSignedByTheAuthorityWithSubjectAlternativeNames() throws Exception {

		// arrange

		var ca = CertificateUtils.generateCertificateAuthority("Test CA");

		// act

		var server = CertificateUtils.generateSignedCertificate("Test Server", ca, Set.of("localhost", "127.0.0.1", "broker.example"));

		// assert

		assertThat(server.certificate().getIssuerX500Principal()).isEqualTo(ca.certificate().getSubjectX500Principal());
		assertThatCode(() -> server.certificate().verify(ca.keyPair().getPublic())).doesNotThrowAnyException();
		assertThat(server.certificate().getBasicConstraints()).isEqualTo(-1);

		var names = server.certificate().getSubjectAlternativeNames().stream().map(entry -> entry.get(1).toString()).toList();
		assertThat(names).containsExactlyInAnyOrder("localhost", "127.0.0.1", "broker.example");

		var types = server.certificate().getSubjectAlternativeNames().stream().map(entry -> (Integer) entry.get(0)).toList();
		assertThat(types).contains(2, 7);
	}

	@Test
	public void shouldParseGeneratedPemWithTheExistingLoaders() throws Exception {

		// arrange

		var ca = CertificateUtils.generateCertificateAuthority("Test CA");
		var client = CertificateUtils.generateSignedCertificate("Test Client", ca, Set.of());

		// act

		var certificates = CertificateUtils.parsePemCertificates(client.certificatePem());
		var privateKey = CertificateUtils.parsePemPrivateKey(client.privateKeyPkcs8Pem());

		// assert

		assertThat(certificates).hasSize(1);
		assertThat(certificates.get(0)).isEqualTo(client.certificate());
		assertThat(privateKey).isEqualTo(client.keyPair().getPrivate());
	}

	@Test
	public void shouldRequireAnIssuerForSignedCertificates() {

		// act

		var thrown = catchThrowable(() -> CertificateUtils.generateSignedCertificate("Test Server", null, Set.of()));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("issuer is required");
	}
}
