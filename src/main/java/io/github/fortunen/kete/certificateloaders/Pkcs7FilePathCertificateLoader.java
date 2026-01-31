package io.github.fortunen.kete.certificateloaders;

import java.io.FileInputStream;
import java.security.KeyStore;

import io.github.fortunen.kete.CertificateLoader;
import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.utils.CertificateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Component(name = "pkcs7-file-path")
public class Pkcs7FilePathCertificateLoader extends CertificateLoader {

	private String path;

	@Override
	@SneakyThrows
	public void initialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		path = ValidationUtils.requireNonBlank(configuration.getString("path", ""), "path is required");
	}

	@Override
	@SneakyThrows
	public void loadKeyStore(KeyStore keyStore, char[] passwordChars) {

		ValidationUtils.requireNonNull(keyStore, "keyStore is required");

		keyStore.load(null, passwordChars);

		try (var stream = new FileInputStream(path)) {

			var certificates = CertificateUtils.parsePkcs7Certificates(stream);
			var index = 0;

			for (var cert : certificates) {
				var alias = CertificateUtils.generateCertificateAlias(cert, index++);
				keyStore.setCertificateEntry(alias, cert);
			}
		}
	}
}
