package io.github.fortunen.kete.certificateloaders;

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
@Component(name = "der-file-base64")
public class DerFileBase64CertificateLoader extends CertificateLoader {

	private static final String ALIAS = "cert";

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

		var derBytes = Base64Utils.decode(base64);
		var certificate = CertificateUtils.parseDerCertificate(derBytes);

		keyStore.load(null, passwordChars);
		keyStore.setCertificateEntry(ALIAS, certificate);
	}
}
