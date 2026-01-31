package io.github.fortunen.kete.unittests.certificateloaders.jksfilebase64certificateloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.certificateloaders.JksFileBase64CertificateLoader;
import java.util.Map;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

class initializeTests {

	@Test
	void shouldThrowWhenConfigurationIsNull() {

		// arrange

		var loader = new JksFileBase64CertificateLoader();
		// configuration is not set (null)

		// act

		var thrown = catchThrowable(() -> loader.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	void shouldThrowWhenBase64IsMissing() {

		// arrange

		var loader = new JksFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of());
		loader.setConfiguration(config);

		// act

		var thrown = catchThrowable(() -> loader.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("base64 is required");
	}

	@Test
	void shouldThrowWhenBase64IsBlank() {

		// arrange

		var loader = new JksFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", "   "));
		loader.setConfiguration(config);

		// act

		var thrown = catchThrowable(() -> loader.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("base64 is required");
	}

	@Test
	void shouldInitializeWithValidBase64() {

		// arrange

		var loader = new JksFileBase64CertificateLoader();
		var config = new MapConfiguration(Map.of("base64", "dGVzdA=="));
		loader.setConfiguration(config);

		// act

		loader.initialize();

		// assert

		assertThat(loader.getBase64()).isEqualTo("dGVzdA==");
	}
}
