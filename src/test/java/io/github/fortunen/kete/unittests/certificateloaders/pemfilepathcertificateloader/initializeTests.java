package io.github.fortunen.kete.unittests.certificateloaders.pemfilepathcertificateloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.certificateloaders.PemFilePathCertificateLoader;
import java.util.Map;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

public class initializeTests {

	@Test
	public void shouldThrowWhenConfigurationIsNull() {

		// arrange

		var loader = new PemFilePathCertificateLoader();
		// configuration is not set (null)

		// act

		var thrown = catchThrowable(() -> loader.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	public void shouldThrowWhenPathIsMissing() {

		// arrange

		var loader = new PemFilePathCertificateLoader();
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
	public void shouldThrowWhenPathIsBlank() {

		// arrange

		var loader = new PemFilePathCertificateLoader();
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
	public void shouldSetPathWhenValid() {

		// arrange

		var loader = new PemFilePathCertificateLoader();
		var config = new MapConfiguration(Map.of("path", "/path/to/certificate.pem"));
		loader.setConfiguration(config);

		// act

		loader.initialize();

		// assert

		assertThat(loader.getPath())
			.as("Path should be set from configuration")
			.isEqualTo("/path/to/certificate.pem");
	}
}
