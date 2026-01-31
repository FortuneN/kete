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
@Component(name = "der-file-path")
@EqualsAndHashCode(callSuper = true)
public class DerFilePathCertificateLoader extends CertificateLoader {

	private static final String ALIAS = "cert";

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

		try (var stream = new FileInputStream(path)) {

			var certificate = CertificateUtils.parseDerCertificate(stream);

			keyStore.load(null, passwordChars);
			keyStore.setCertificateEntry(ALIAS, certificate);
		}
	}
}
