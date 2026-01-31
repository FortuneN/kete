package io.github.fortunen.kete.unittests.certificateloaders.pkcs7filepathcertificateloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.certificateloaders.Pkcs7FilePathCertificateLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import okhttp3.tls.HeldCertificate;
import org.apache.commons.configuration2.MapConfiguration;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class loadKeyStoreTests {

	@TempDir
	File tempDir;

	private byte[] createPkcs7(List<X509Certificate> certificates) throws Exception {
		var certStore = new JcaCertStore(certificates);
		var generator = new CMSSignedDataGenerator();
		generator.addCertificates(certStore);

		// Create a degenerate PKCS#7 (no signers, just certificates)
		var signedData = generator.generate(new CMSProcessableByteArray(new byte[0]), false);
		return signedData.getEncoded();
	}

	private File writePkcs7ToFile(byte[] pkcs7Bytes, String filename) throws Exception {
		var file = new File(tempDir, filename);
		try (var fos = new FileOutputStream(file)) {
			fos.write(pkcs7Bytes);
		}
		return file;
	}

	@Test
	public void shouldLoadSingleCertificateFromFile() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var pkcs7Bytes = createPkcs7(List.of(heldCert.certificate()));
		var pkcs7File = writePkcs7ToFile(pkcs7Bytes, "single.p7b");

		var loader = new Pkcs7FilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", pkcs7File.getAbsolutePath()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, "password".toCharArray());

		// assert - alias is auto-generated from CN with index suffix

		assertThat(keyStore.containsAlias("test-cert-0"))
			.as("KeyStore should contain the certificate with alias 'test-cert-0'")
			.isTrue();

		assertThat(keyStore.getCertificate("test-cert-0"))
			.as("Certificate should be loaded into KeyStore")
			.isNotNull();

		assertThat(keyStore.isCertificateEntry("test-cert-0"))
			.as("Entry should be a certificate entry")
			.isTrue();
	}

	@Test
	public void shouldLoadMultipleCertificatesFromFile() throws Exception {

		// arrange

		var cert1 = new HeldCertificate.Builder()
			.commonName("server-cert")
			.build();
		var cert2 = new HeldCertificate.Builder()
			.commonName("intermediate-ca")
			.build();
		var cert3 = new HeldCertificate.Builder()
			.commonName("root-ca")
			.build();

		var pkcs7Bytes = createPkcs7(List.of(
			cert1.certificate(),
			cert2.certificate(),
			cert3.certificate()
		));
		var pkcs7File = writePkcs7ToFile(pkcs7Bytes, "chain.p7b");

		var loader = new Pkcs7FilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", pkcs7File.getAbsolutePath()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, "password".toCharArray());

		// assert - aliases auto-generated from CNs with index suffix

		assertThat(keyStore.size())
			.as("KeyStore should contain 3 certificates")
			.isEqualTo(3);

		assertThat(keyStore.containsAlias("server-cert-0")).isTrue();
		assertThat(keyStore.containsAlias("intermediate-ca-1")).isTrue();
		assertThat(keyStore.containsAlias("root-ca-2")).isTrue();
	}

	@Test
	public void shouldThrowWhenFileDoesNotExist() throws Exception {

		// arrange

		var loader = new Pkcs7FilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", "/nonexistent/path/certs.p7b"));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		var thrown = catchThrowable(() -> loader.loadKeyStore(keyStore, "password".toCharArray()));

		// assert

		assertThat(thrown)
			.as("Should throw when file does not exist")
			.isNotNull();
	}

	@Test
	public void shouldThrowWhenFileIsNotValidPkcs7() throws Exception {

		// arrange

		var invalidFile = new File(tempDir, "invalid.p7b");
		Files.writeString(invalidFile.toPath(), "This is not a valid PKCS#7 file");

		var loader = new Pkcs7FilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", invalidFile.getAbsolutePath()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		var thrown = catchThrowable(() -> loader.loadKeyStore(keyStore, "password".toCharArray()));

		// assert

		assertThat(thrown)
			.as("Should throw when file content is not valid PKCS#7")
			.isNotNull();
	}

	@Test
	public void shouldLoadWithNullPassword() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var pkcs7Bytes = createPkcs7(List.of(heldCert.certificate()));
		var pkcs7File = writePkcs7ToFile(pkcs7Bytes, "null-password.p7b");

		var loader = new Pkcs7FilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", pkcs7File.getAbsolutePath()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, null);

		// assert

		assertThat(keyStore.containsAlias("test-cert-0"))
			.as("KeyStore should contain the certificate with null password")
			.isTrue();
	}

	@Test
	public void shouldLoadWithEmptyPassword() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var pkcs7Bytes = createPkcs7(List.of(heldCert.certificate()));
		var pkcs7File = writePkcs7ToFile(pkcs7Bytes, "empty-password.p7b");

		var loader = new Pkcs7FilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", pkcs7File.getAbsolutePath()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, new char[0]);

		// assert

		assertThat(keyStore.containsAlias("test-cert-0"))
			.as("KeyStore should contain the certificate with empty password")
			.isTrue();
	}

	@Test
	public void shouldLoadWithWhitespacePassword() throws Exception {

		// arrange

		var heldCert = new HeldCertificate.Builder()
			.commonName("test-cert")
			.build();
		var pkcs7Bytes = createPkcs7(List.of(heldCert.certificate()));
		var pkcs7File = writePkcs7ToFile(pkcs7Bytes, "whitespace-password.p7b");

		var loader = new Pkcs7FilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", pkcs7File.getAbsolutePath()));
		loader.setConfiguration(config);
		loader.initialize();

		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		// act

		loader.loadKeyStore(keyStore, "   ".toCharArray());

		// assert

		assertThat(keyStore.containsAlias("test-cert-0"))
			.as("KeyStore should contain the certificate with whitespace password")
			.isTrue();
	}
}
