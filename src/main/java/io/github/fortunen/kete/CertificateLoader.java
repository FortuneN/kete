package io.github.fortunen.kete;

import java.security.KeyStore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.configuration2.MapConfiguration;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = false)
public abstract class CertificateLoader {

	protected MapConfiguration configuration;

	public abstract void initialize();

	public abstract void loadKeyStore(KeyStore keyStore, char[] passwordChars);
}
