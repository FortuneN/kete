package io.github.fortunen.kete.certificateloaders;

import java.io.ByteArrayInputStream;
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
@Component(name = "pkcs12-file-base64")
public class Pkcs12FileBase64CertificateLoader extends CertificateLoader {

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

		var pkcs12Bytes = CertificateUtils.BASE64_DECODER.decode(base64);

		try (var stream = new ByteArrayInputStream(pkcs12Bytes)) {
			keyStore.load(stream, passwordChars);
		}
	}
}
