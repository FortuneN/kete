package io.github.fortunen.kete.unittests.utils.iocutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.CertificateLoader;
import io.github.fortunen.kete.certificateloaders.JksFileBase64CertificateLoader;
import io.github.fortunen.kete.certificateloaders.DerFileBase64CertificateLoader;
import io.github.fortunen.kete.certificateloaders.DerFilePathCertificateLoader;
import io.github.fortunen.kete.certificateloaders.JksFilePathCertificateLoader;
import io.github.fortunen.kete.certificateloaders.PemFileBase64CertificateLoader;
import io.github.fortunen.kete.certificateloaders.PemFilePathCertificateLoader;
import io.github.fortunen.kete.certificateloaders.PemFileTextCertificateLoader;
import io.github.fortunen.kete.certificateloaders.Pkcs12FileBase64CertificateLoader;
import io.github.fortunen.kete.certificateloaders.Pkcs12FilePathCertificateLoader;
import io.github.fortunen.kete.utils.IocUtils;
import org.junit.jupiter.api.Test;

public class getCertificateLoaderTests {

	@Test
	public void shouldGetJksFileBase64CertificateLoader() {

		// act

		var result = IocUtils.get("jks-file-base64", CertificateLoader.class);

		// assert

		assertThat(result)
			.as("Should return a JKS file base64 certificate loader")
			.isNotNull()
			.isInstanceOf(JksFileBase64CertificateLoader.class);
	}

	@Test
	public void shouldGetJksFilePathCertificateLoader() {

		// act

		var result = IocUtils.get("jks-file-path", CertificateLoader.class);

		// assert

		assertThat(result)
			.as("Should return a JKS file path certificate loader")
			.isNotNull()
			.isInstanceOf(JksFilePathCertificateLoader.class);
	}

	@Test
	public void shouldGetDerFileBase64CertificateLoader() {

		// act

		var result = IocUtils.get("der-file-base64", CertificateLoader.class);

		// assert

		assertThat(result)
			.as("Should return a DER file base64 certificate loader")
			.isNotNull()
			.isInstanceOf(DerFileBase64CertificateLoader.class);
	}

	@Test
	public void shouldGetDerFilePathCertificateLoader() {

		// act

		var result = IocUtils.get("der-file-path", CertificateLoader.class);

		// assert

		assertThat(result)
			.as("Should return a DER file path certificate loader")
			.isNotNull()
			.isInstanceOf(DerFilePathCertificateLoader.class);
	}

	@Test
	public void shouldGetPemFileBase64CertificateLoader() {

		// act

		var result = IocUtils.get("pem-file-base64", CertificateLoader.class);

		// assert

		assertThat(result)
			.as("Should return a PEM file base64 certificate loader")
			.isNotNull()
			.isInstanceOf(PemFileBase64CertificateLoader.class);
	}

	@Test
	public void shouldGetPemFilePathCertificateLoader() {

		// act

		var result = IocUtils.get("pem-file-path", CertificateLoader.class);

		// assert

		assertThat(result)
			.as("Should return a PEM file path certificate loader")
			.isNotNull()
			.isInstanceOf(PemFilePathCertificateLoader.class);
	}

	@Test
	public void shouldGetPemFileTextCertificateLoader() {

		// act

		var result = IocUtils.get("pem-file-text", CertificateLoader.class);

		// assert

		assertThat(result)
			.as("Should return a PEM file text certificate loader")
			.isNotNull()
			.isInstanceOf(PemFileTextCertificateLoader.class);
	}

	@Test
	public void shouldGetPkcs12FileBase64CertificateLoader() {

		// act

		var result = IocUtils.get("pkcs12-file-base64", CertificateLoader.class);

		// assert

		assertThat(result)
			.as("Should return a PKCS12 file base64 certificate loader")
			.isNotNull()
			.isInstanceOf(Pkcs12FileBase64CertificateLoader.class);
	}

	@Test
	public void shouldGetPkcs12FilePathCertificateLoader() {

		// act

		var result = IocUtils.get("pkcs12-file-path", CertificateLoader.class);

		// assert

		assertThat(result)
			.as("Should return a PKCS12 file path certificate loader")
			.isNotNull()
			.isInstanceOf(Pkcs12FilePathCertificateLoader.class);
	}

	@Test
	public void shouldReturnNewInstanceEachTimeForTransientLoaders() {

		// act

		var first = IocUtils.get("jks-file-base64", CertificateLoader.class);
		var second = IocUtils.get("jks-file-base64", CertificateLoader.class);

		// assert

		assertThat(first)
			.as("Should return different instances for transient certificate loaders")
			.isNotSameAs(second);
	}

	@Test
	public void shouldReturnNullForUnknownLoaderKey() {

		// act

		var result = IocUtils.get("unknown-loader", CertificateLoader.class);

		// assert

		assertThat(result)
			.as("Should return null for unknown certificate loader key")
			.isNull();
	}
}
