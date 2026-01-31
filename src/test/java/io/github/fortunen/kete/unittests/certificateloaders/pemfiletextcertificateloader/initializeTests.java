package io.github.fortunen.kete.unittests.certificateloaders.pemfiletextcertificateloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.certificateloaders.PemFileTextCertificateLoader;
import java.util.Map;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

public class initializeTests {

	@Test
	public void shouldThrowWhenConfigurationIsNull() {

		// arrange

		var loader = new PemFileTextCertificateLoader();
		// configuration is not set (null)

		// act

		var thrown = catchThrowable(() -> loader.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	public void shouldThrowWhenTextIsMissing() {

		// arrange

		var loader = new PemFileTextCertificateLoader();
		var config = new MapConfiguration(Map.of());
		loader.setConfiguration(config);

		// act

		var thrown = catchThrowable(() -> loader.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("text is required");
	}

	@Test
	public void shouldThrowWhenTextIsBlank() {

		// arrange

		var loader = new PemFileTextCertificateLoader();
		var config = new MapConfiguration(Map.of("text", "   "));
		loader.setConfiguration(config);

		// act

		var thrown = catchThrowable(() -> loader.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("text is required");
	}

	@Test
	public void shouldSetTextWhenValid() {

		// arrange

		var loader = new PemFileTextCertificateLoader();
		var pemText = "-----BEGIN CERTIFICATE-----\ntest\n-----END CERTIFICATE-----";
		var config = new MapConfiguration(Map.of("text", pemText));
		loader.setConfiguration(config);

		// act

		loader.initialize();

		// assert

		assertThat(loader.getText())
			.as("Text should be set from configuration")
			.isEqualTo(pemText);
	}
}
