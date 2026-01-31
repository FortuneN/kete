package io.github.fortunen.kete.certificateloaders;

import java.security.KeyStore;
import java.security.cert.Certificate;

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
@Component(name = "pem-file-text")
@EqualsAndHashCode(callSuper = true)
public class PemFileTextCertificateLoader extends CertificateLoader {

	private static final String ALIAS = "key";

	private String text;

	@Override
	@SneakyThrows
	public void initialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		text = ValidationUtils.requireNonBlank(configuration.getString("text", ""), "text is required");
	}

	@Override
	@SneakyThrows
	public void loadKeyStore(KeyStore keyStore, char[] passwordChars) {

		ValidationUtils.requireNonNull(keyStore, "keyStore is required");

		var certificates = CertificateUtils.parsePemCertificates(text);
		var privateKey = CertificateUtils.parsePemPrivateKey(text);

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
