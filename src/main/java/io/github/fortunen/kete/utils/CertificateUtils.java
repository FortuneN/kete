package io.github.fortunen.kete.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.MapConfiguration;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.util.io.pem.PemObject;

import io.github.fortunen.kete.CertificateLoader;
import io.github.fortunen.kete.Constants;
import lombok.SneakyThrows;

@SuppressWarnings("unchecked")
public final class CertificateUtils {

	public static final String X509_TYPE = "X.509";

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

	// test-certificate generation (TlsMaterial.builder().withServerHostNames(...) builds a CA-signed server/client pair)

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final Pattern IP_ADDRESS = Pattern.compile("^(\\d{1,3}(\\.\\d{1,3}){3}|[0-9a-fA-F:]+:[0-9a-fA-F:]*)$");

	public record GeneratedCertificate(X509Certificate certificate, KeyPair keyPair) {

		public String certificatePem() {
			return toPem(certificate);
		}

		public String privateKeyPkcs8Pem() {
			return toPem(new PemObject("PRIVATE KEY", keyPair.getPrivate().getEncoded()));
		}
	}

	public static GeneratedCertificate generateCertificateAuthority(String commonName) {
		return generate(commonName, null, true, Set.of());
	}

	public static GeneratedCertificate generateSignedCertificate(String commonName, GeneratedCertificate issuer, Set<String> subjectAlternativeNames) {

		ValidationUtils.requireNonNull(issuer, "issuer is required");

		return generate(commonName, issuer, false, subjectAlternativeNames);
	}

	@SneakyThrows
	private static GeneratedCertificate generate(String commonName, GeneratedCertificate issuer, boolean certificateAuthority, Set<String> subjectAlternativeNames) {

		ValidationUtils.requireNonBlank(commonName, "commonName is required");
		ValidationUtils.requireNonNull(subjectAlternativeNames, "subjectAlternativeNames is required");

		var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		var keyPair = keyPairGenerator.generateKeyPair();

		var subject = new X500Name("CN=" + commonName);
		var issuerName = ValidationUtils.isNull(issuer) ? subject : new JcaX509CertificateHolder(issuer.certificate()).getSubject();
		var signingKey = ValidationUtils.isNull(issuer) ? keyPair.getPrivate() : issuer.keyPair().getPrivate();
		var now = Instant.now();

		var builder = new JcaX509v3CertificateBuilder(issuerName, new BigInteger(63, SECURE_RANDOM), Date.from(now.minus(1, ChronoUnit.DAYS)), Date.from(now.plus(365, ChronoUnit.DAYS)), subject, keyPair.getPublic());

		builder.addExtension(Extension.basicConstraints, true, certificateAuthority ? new BasicConstraints(0) : new BasicConstraints(false));
		builder.addExtension(Extension.keyUsage, true, new KeyUsage(certificateAuthority ? KeyUsage.keyCertSign | KeyUsage.cRLSign : KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

		if (!certificateAuthority) {
			builder.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(new KeyPurposeId[] { KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth }));
		}

		if (!subjectAlternativeNames.isEmpty()) {
			var names = subjectAlternativeNames.stream().map(name -> new GeneralName(IP_ADDRESS.matcher(name).matches() ? GeneralName.iPAddress : GeneralName.dNSName, name)).toArray(GeneralName[]::new);
			builder.addExtension(Extension.subjectAlternativeName, false, new GeneralNames(names));
		}

		var certificate = new JcaX509CertificateConverter().getCertificate(builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(signingKey)));

		return new GeneratedCertificate(certificate, keyPair);
	}

	@SneakyThrows
	private static String toPem(Object object) {

		var writer = new StringWriter();

		try (var pemWriter = new JcaPEMWriter(writer)) {
			pemWriter.writeObject(object);
		}

		return writer.toString();
	}
}
