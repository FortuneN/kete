package io.github.fortunen.kete.certificateloaders;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;

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
@Component(name = "pem-file-base64")
public class PemFileBase64CertificateLoader extends CertificateLoader {

	private static final String ALIAS = "key";

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

		var pemContent = new String(Base64Utils.decode(base64), StandardCharsets.UTF_8);
		var certificates = CertificateUtils.parsePemCertificates(pemContent);
		var privateKey = CertificateUtils.parsePemPrivateKey(pemContent);

		keyStore.load(null, passwordChars);

		if (ValidationUtils.isNotNull(privateKey) && !certificates.isEmpty()) {

			keyStore.setKeyEntry(ALIAS, privateKey, ValidationUtils.requireNonNullElse(passwordChars, new char[0]), certificates.toArray(new Certificate[0]));

		} else if (!certificates.isEmpty()) {

			for (var i = 0; i < certificates.size(); i++) {
				keyStore.setCertificateEntry(ALIAS + "-" + i, certificates.get(i));
			}

		}
	}
}
