package io.github.fortunen.kete.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration2.MapConfiguration;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;

import io.github.fortunen.kete.CertificateLoader;
import io.github.fortunen.kete.Constants;
import lombok.SneakyThrows;

@SuppressWarnings("unchecked")
public final class CertificateUtils {

	public static final String X509_TYPE = "X.509";

	public static final Base64.Decoder BASE64_DECODER = Base64.getMimeDecoder();

	private static final JcaPEMKeyConverter PEM_KEY_CONVERTER = new JcaPEMKeyConverter();

	private CertificateUtils() {}

	@SneakyThrows
	public static CertificateLoader createCertificateLoader(MapConfiguration configuration) {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		var kind = ValidationUtils.requireNonBlank(configuration.getString(Constants.KIND, "").trim(), Constants.KIND + " is required");
		var certificateLoader = ValidationUtils.requireNonNull(IocUtils.get(kind, CertificateLoader.class), "certificate loader " + Constants.KIND + " '" + kind + "' not found");

		certificateLoader.setConfiguration(configuration);
		certificateLoader.initialize();

		return certificateLoader;
	}

	@SneakyThrows
	public static CertificateFactory getCertificateFactory() {
		return CertificateFactory.getInstance(X509_TYPE);
	}

	@SneakyThrows
	public static List<X509Certificate> parsePemCertificates(String pemContent) {

		ValidationUtils.requireNonNull(pemContent, "pemContent is required");

		var certificates = new ArrayList<X509Certificate>();

		try (var parser = new PEMParser(new StringReader(pemContent))) {

			Object object;

			while (ValidationUtils.isNotNull(object = parser.readObject())) {

				if (object instanceof X509CertificateHolder holder) {
					certificates.add(parseDerCertificate(holder.getEncoded()));
				}
			}
		}

		return certificates;
	}

	@SneakyThrows
	public static PrivateKey parsePemPrivateKey(String pemContent) {

		ValidationUtils.requireNonNull(pemContent, "pemContent is required");

		try (var parser = new PEMParser(new StringReader(pemContent))) {

			Object object;

			while (ValidationUtils.isNotNull(object = parser.readObject())) {

				if (object instanceof PEMKeyPair keyPair) {
					return PEM_KEY_CONVERTER.getKeyPair(keyPair).getPrivate();
				}

				if (object instanceof PrivateKeyInfo keyInfo) {
					return PEM_KEY_CONVERTER.getPrivateKey(keyInfo);
				}

				if (object instanceof PKCS8EncryptedPrivateKeyInfo) {
					throw new IllegalStateException("Encrypted private keys are not supported");
				}
			}
		}

		return null;
	}

	@SneakyThrows
	public static X509Certificate parseDerCertificate(byte[] derBytes) {

		ValidationUtils.requireNonNull(derBytes, "derBytes is required");

		return (X509Certificate) getCertificateFactory().generateCertificate(new ByteArrayInputStream(derBytes));
	}

	@SneakyThrows
	public static X509Certificate parseDerCertificate(InputStream stream) {

		ValidationUtils.requireNonNull(stream, "stream is required");

		return (X509Certificate) getCertificateFactory().generateCertificate(stream);
	}

	@SneakyThrows
	public static Collection<X509Certificate> parsePkcs7Certificates(InputStream stream) {

		ValidationUtils.requireNonNull(stream, "stream is required");

		return (Collection<X509Certificate>) getCertificateFactory().generateCertificates(stream);
	}

	public static String extractCommonName(X509Certificate certificate) {

		ValidationUtils.requireNonNull(certificate, "certificate is required");

		var x500Name = new X500Name(certificate.getSubjectX500Principal().getName());
		var cnRdns = x500Name.getRDNs(BCStyle.CN);

		if (cnRdns.length > 0) {
			return cnRdns[0].getFirst().getValue().toString();
		}

		return null;
	}

	public static String generateCertificateAlias(X509Certificate certificate, int index) {

		ValidationUtils.requireNonNull(certificate, "certificate is required");

		var commonName = extractCommonName(certificate);

		if (ValidationUtils.isNotBlank(commonName)) {
			return commonName.toLowerCase().replaceAll("[^a-z0-9]", "-") + "-" + index;
		}

		return "cert-" + certificate.getSerialNumber().toString(16) + "-" + index;
	}

	@SneakyThrows
	public static String convertPkcs8ToPkcs1Pem(PrivateKey privateKey) {

		ValidationUtils.requireNonNull(privateKey, "privateKey is required");

		if (!(privateKey instanceof RSAPrivateKey)) {
			throw new IllegalArgumentException("Only RSA private keys can be converted to PKCS#1 format");
		}

		var keyInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
		var pkcs1Bytes = keyInfo.parsePrivateKey().toASN1Primitive().getEncoded();
		var base64Encoded = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(pkcs1Bytes);

		return "-----BEGIN RSA PRIVATE KEY-----\n" + base64Encoded + "\n-----END RSA PRIVATE KEY-----\n";
	}
}
