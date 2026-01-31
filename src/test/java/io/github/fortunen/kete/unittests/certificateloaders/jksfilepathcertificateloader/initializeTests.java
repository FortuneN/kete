package io.github.fortunen.kete.unittests.certificateloaders.jksfilepathcertificateloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.certificateloaders.JksFilePathCertificateLoader;
import java.util.Map;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

class initializeTests {

	@Test
	void shouldThrowWhenConfigurationIsNull() {

		// arrange

		var loader = new JksFilePathCertificateLoader();
		// configuration is not set (null)

		// act

		var thrown = catchThrowable(() -> loader.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	void shouldThrowWhenPathIsMissing() {

		// arrange

		var loader = new JksFilePathCertificateLoader();
		var config = new MapConfiguration(Map.of());
		loader.setConfiguration(config);

		// act

		var thrown = catchThrowable(() -> loader.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("path is required");
	}

	@Test
	void shouldThrowWhenPathIsBlank() {

		// arrange

		var loader = new JksFilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", "   "));
		loader.setConfiguration(config);

		// act

		var thrown = catchThrowable(() -> loader.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("path is required");
	}

	@Test
	void shouldInitializeWithValidPath() {

		// arrange

		var loader = new JksFilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", "/path/to/keystore.jks"));
		loader.setConfiguration(config);

		// act

		loader.initialize();

		// assert

		assertThat(loader.getPath()).isEqualTo("/path/to/keystore.jks");
	}
}
