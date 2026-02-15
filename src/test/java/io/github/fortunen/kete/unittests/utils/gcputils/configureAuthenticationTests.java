package io.github.fortunen.kete.unittests.utils.gcputils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.GcpUtils;

class configureAuthenticationTests {

	@Test
	void shouldReturnNullWhenAuthenticationTypeIsEmpty() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act

		var credentials = GcpUtils.configureAuthentication("", config, "https://www.googleapis.com/auth/cloud-platform");

		// assert

		assertThat(credentials).isNull();
	}

	@Test
	void shouldThrowWhenAuthenticationTypeIsUnsupported() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act

		var thrown = catchThrowable(() -> GcpUtils.configureAuthentication("unknown-type", config, "https://www.googleapis.com/auth/cloud-platform"));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("unsupported authentication-type: 'unknown-type' — valid options: application-default, service-account-file-path, service-account-file-text, service-account-file-base64");
	}

	@Test
	void shouldThrowWhenMultipleCredentialSourcesProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-path", "/some/path");
		map.put("credentials-file-text", "{\"some\":\"json\"}");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> GcpUtils.configureAuthentication("", config, "https://www.googleapis.com/auth/cloud-platform"));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("credentials-file-path, credentials-file-text, and credentials-file-base64 are mutually exclusive");
	}

	@Test
	void shouldThrowWhenAllThreeCredentialSourcesProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-path", "/some/path");
		map.put("credentials-file-text", "{\"some\":\"json\"}");
		map.put("credentials-file-base64", "eyJzb21lIjoianNvbiJ9");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> GcpUtils.configureAuthentication("", config, "https://www.googleapis.com/auth/cloud-platform"));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("credentials-file-path, credentials-file-text, and credentials-file-base64 are mutually exclusive");
	}

	@Test
	void shouldThrowWhenFilePathAndBase64CredentialSourcesProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-path", "/some/path");
		map.put("credentials-file-base64", "eyJzb21lIjoianNvbiJ9");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> GcpUtils.configureAuthentication("", config, "https://www.googleapis.com/auth/cloud-platform"));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("credentials-file-path, credentials-file-text, and credentials-file-base64 are mutually exclusive");
	}

	@Test
	void shouldThrowWhenTextAndBase64CredentialSourcesProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-text", "{\"some\":\"json\"}");
		map.put("credentials-file-base64", "eyJzb21lIjoianNvbiJ9");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> GcpUtils.configureAuthentication("", config, "https://www.googleapis.com/auth/cloud-platform"));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("credentials-file-path, credentials-file-text, and credentials-file-base64 are mutually exclusive");
	}

	@Test
	void shouldReturnNullWhenAuthenticationTypeIsEmptyAndSingleCredentialSourceProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-path", "/some/path");
		var config = new MapConfiguration(map);

		// act

		var credentials = GcpUtils.configureAuthentication("", config, "https://www.googleapis.com/auth/cloud-platform");

		// assert

		assertThat(credentials).isNull();
	}

	@Test
	void shouldThrowWhenApplicationDefaultHasCredentialsFilePath() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-path", "/some/path");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> GcpUtils.configureAuthentication("application-default", config, "https://www.googleapis.com/auth/cloud-platform"));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("'credentials-file-path', 'credentials-file-text', and 'credentials-file-base64' cannot be set when authentication-type is 'application-default'");
	}

	@Test
	void shouldThrowWhenApplicationDefaultHasCredentialsFileText() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-text", "{\"some\":\"json\"}");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> GcpUtils.configureAuthentication("application-default", config, "https://www.googleapis.com/auth/cloud-platform"));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("'credentials-file-path', 'credentials-file-text', and 'credentials-file-base64' cannot be set when authentication-type is 'application-default'");
	}

	@Test
	void shouldThrowWhenApplicationDefaultHasCredentialsFileBase64() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-base64", "eyJzb21lIjoianNvbiJ9");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> GcpUtils.configureAuthentication("application-default", config, "https://www.googleapis.com/auth/cloud-platform"));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("'credentials-file-path', 'credentials-file-text', and 'credentials-file-base64' cannot be set when authentication-type is 'application-default'");
	}

	@Test
	void shouldThrowWhenServiceAccountFilePathIsBlank() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act

		var thrown = catchThrowable(() -> GcpUtils.configureAuthentication("service-account-file-path", config, "https://www.googleapis.com/auth/cloud-platform"));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("credentials-file-path is required when authentication-type is 'service-account-file-path'");
	}

	@Test
	void shouldThrowWhenServiceAccountFileTextIsBlank() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act

		var thrown = catchThrowable(() -> GcpUtils.configureAuthentication("service-account-file-text", config, "https://www.googleapis.com/auth/cloud-platform"));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("credentials-file-text is required when authentication-type is 'service-account-file-text'");
	}

	@Test
	void shouldThrowWhenServiceAccountFileBase64IsBlank() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act

		var thrown = catchThrowable(() -> GcpUtils.configureAuthentication("service-account-file-base64", config, "https://www.googleapis.com/auth/cloud-platform"));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("credentials-file-base64 is required when authentication-type is 'service-account-file-base64'");
	}

	@Test
	void shouldAllowEmptyAuthenticationTypeWithNoCredentialSources() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act

		var credentials = GcpUtils.configureAuthentication("", config, "https://www.googleapis.com/auth/cloud-platform");

		// assert

		assertThat(credentials).isNull();
	}

	@Test
	void shouldAllowEmptyAuthenticationTypeWithBlankCredentialSources() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-path", "   ");
		map.put("credentials-file-text", "");
		map.put("credentials-file-base64", "  ");
		var config = new MapConfiguration(map);

		// act

		var credentials = GcpUtils.configureAuthentication("", config, "https://www.googleapis.com/auth/cloud-platform");

		// assert

		assertThat(credentials).isNull();
	}
}
