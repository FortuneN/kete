package io.github.fortunen.kete.certificateloaders;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;

import io.github.fortunen.kete.CertificateLoader;
import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.utils.Base64Utils;
import io.github.fortunen.kete.utils.CertificateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Component(name = "pkcs7-file-base64")
public class Pkcs7FileBase64CertificateLoader extends CertificateLoader {

	private String base64;

	@Override
	@SneakyThrows
	public void initialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		base64 = ValidationUtils.requireNonBlank(configuration.getString("base64", ""), "base64 is required");
	}

	@Override
	@SneakyThrows
	public void loadKeyStore(KeyStore keyStore, char[] passwordChars) {

		ValidationUtils.requireNonNull(keyStore, "keyStore is required");

		var pkcs7Bytes = Base64Utils.decode(base64);

		keyStore.load(null, passwordChars);

		try (var stream = new ByteArrayInputStream(pkcs7Bytes)) {

			var certificates = CertificateUtils.parsePkcs7Certificates(stream);
			var index = 0;

			for (var cert : certificates) {
				var alias = CertificateUtils.generateCertificateAlias(cert, index++);
				keyStore.setCertificateEntry(alias, cert);
			}
		}
	}
}
