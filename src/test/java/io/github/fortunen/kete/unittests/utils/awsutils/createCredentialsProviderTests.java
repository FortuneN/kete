package io.github.fortunen.kete.unittests.utils.awsutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.AwsUtils;
import io.github.fortunen.kete.utils.Base64Utils;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

public class createCredentialsProviderTests {

	private static final String CREDENTIALS_CONTENT = "[default]\naws_access_key_id=AKIAIOSFODNN7EXAMPLE\naws_secret_access_key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY\n";

	// =========================================================================
	// Blank / null authentication-type → Anonymous
	// =========================================================================

	@Test
	public void shouldReturnAnonymousProviderWhenAuthenticationTypeIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var result = AwsUtils.createCredentialsProvider("", config);

		// assert

		assertThat(result).isInstanceOf(AnonymousCredentialsProvider.class);
	}

	@Test
	public void shouldReturnAnonymousProviderWhenAuthenticationTypeIsNull() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var result = AwsUtils.createCredentialsProvider(null, config);

		// assert

		assertThat(result).isInstanceOf(AnonymousCredentialsProvider.class);
	}

	// =========================================================================
	// access-key
	// =========================================================================

	@Test
	public void shouldReturnStaticProviderForAccessKey() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("access-key-id", "AKIAIOSFODNN7EXAMPLE");
		map.put("secret-access-key", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
		var config = new MapConfiguration(map);

		// act

		var result = AwsUtils.createCredentialsProvider("access-key", config);

		// assert

		assertThat(result).isInstanceOf(StaticCredentialsProvider.class);

		var credentials = (AwsBasicCredentials) result.resolveCredentials();
		assertThat(credentials.accessKeyId()).isEqualTo("AKIAIOSFODNN7EXAMPLE");
		assertThat(credentials.secretAccessKey()).isEqualTo("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
	}

	@Test
	public void shouldThrowWhenAccessKeyIdMissing() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("secret-access-key", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.createCredentialsProvider("access-key", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("access-key-id");
	}

	@Test
	public void shouldThrowWhenSecretAccessKeyMissing() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("access-key-id", "AKIAIOSFODNN7EXAMPLE");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.createCredentialsProvider("access-key", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("secret-access-key");
	}

	// =========================================================================
	// environment-variables
	// =========================================================================

	@Test
	public void shouldReturnEnvironmentVariableProvider() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var result = AwsUtils.createCredentialsProvider("environment-variables", config);

		// assert

		assertThat(result).isInstanceOf(EnvironmentVariableCredentialsProvider.class);
	}

	// =========================================================================
	// instance-metadata
	// =========================================================================

	@Test
	public void shouldReturnInstanceProfileProvider() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var result = AwsUtils.createCredentialsProvider("instance-metadata", config);

		// assert

		assertThat(result).isInstanceOf(InstanceProfileCredentialsProvider.class);
	}

	// =========================================================================
	// credentials-file-path
	// =========================================================================

	@Test
	public void shouldReturnProfileProviderFromPath() throws IOException {

		// arrange

		var tempFile = Files.createTempFile("kete-test-creds-", ".ini");
		tempFile.toFile().deleteOnExit();
		Files.writeString(tempFile, CREDENTIALS_CONTENT);

		var map = new HashMap<String, Object>();
		map.put("credentials-file-path", tempFile.toString());
		var config = new MapConfiguration(map);

		// act

		var result = AwsUtils.createCredentialsProvider("credentials-file-path", config);

		// assert

		assertThat(result).isInstanceOf(ProfileCredentialsProvider.class);

		var credentials = (AwsBasicCredentials) result.resolveCredentials();
		assertThat(credentials.accessKeyId()).isEqualTo("AKIAIOSFODNN7EXAMPLE");
		assertThat(credentials.secretAccessKey()).isEqualTo("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
	}

	@Test
	public void shouldReturnProfileProviderFromPathWithCustomProfile() throws IOException {

		// arrange

		var content = "[custom]\naws_access_key_id=CUSTOMKEY\naws_secret_access_key=CUSTOMSECRET\n";
		var tempFile = Files.createTempFile("kete-test-creds-", ".ini");
		tempFile.toFile().deleteOnExit();
		Files.writeString(tempFile, content);

		var map = new HashMap<String, Object>();
		map.put("credentials-file-path", tempFile.toString());
		map.put("credentials-profile", "custom");
		var config = new MapConfiguration(map);

		// act

		var result = AwsUtils.createCredentialsProvider("credentials-file-path", config);

		// assert

		assertThat(result).isInstanceOf(ProfileCredentialsProvider.class);

		var credentials = (AwsBasicCredentials) result.resolveCredentials();
		assertThat(credentials.accessKeyId()).isEqualTo("CUSTOMKEY");
		assertThat(credentials.secretAccessKey()).isEqualTo("CUSTOMSECRET");
	}

	@Test
	public void shouldThrowWhenCredentialsFilePathMissing() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.createCredentialsProvider("credentials-file-path", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("credentials-file-path");
	}

	// =========================================================================
	// credentials-file-text
	// =========================================================================

	@Test
	public void shouldReturnProfileProviderFromText() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-text", CREDENTIALS_CONTENT);
		var config = new MapConfiguration(map);

		// act

		var result = AwsUtils.createCredentialsProvider("credentials-file-text", config);

		// assert

		assertThat(result).isInstanceOf(ProfileCredentialsProvider.class);

		var credentials = (AwsBasicCredentials) result.resolveCredentials();
		assertThat(credentials.accessKeyId()).isEqualTo("AKIAIOSFODNN7EXAMPLE");
		assertThat(credentials.secretAccessKey()).isEqualTo("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
	}

	@Test
	public void shouldThrowWhenCredentialsFileTextMissing() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.createCredentialsProvider("credentials-file-text", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("credentials-file-text");
	}

	// =========================================================================
	// credentials-file-base64
	// =========================================================================

	@Test
	public void shouldReturnProfileProviderFromBase64() {

		// arrange

		var base64 = Base64Utils.encode(CREDENTIALS_CONTENT.getBytes(StandardCharsets.UTF_8));

		var map = new HashMap<String, Object>();
		map.put("credentials-file-base64", base64);
		var config = new MapConfiguration(map);

		// act

		var result = AwsUtils.createCredentialsProvider("credentials-file-base64", config);

		// assert

		assertThat(result).isInstanceOf(ProfileCredentialsProvider.class);

		var credentials = (AwsBasicCredentials) result.resolveCredentials();
		assertThat(credentials.accessKeyId()).isEqualTo("AKIAIOSFODNN7EXAMPLE");
		assertThat(credentials.secretAccessKey()).isEqualTo("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
	}

	@Test
	public void shouldThrowWhenCredentialsFileBase64Missing() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.createCredentialsProvider("credentials-file-base64", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("credentials-file-base64");
	}

	// =========================================================================
	// container-credentials
	// =========================================================================

	@Test
	public void shouldThrowWhenContainerCredentialsEnvVarsMissing() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.createCredentialsProvider("container-credentials", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("AWS_CONTAINER_CREDENTIALS_RELATIVE_URI");
	}

	// =========================================================================
	// default-credentials-chain
	// =========================================================================

	@Test
	public void shouldReturnDefaultCredentialsProvider() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var result = AwsUtils.createCredentialsProvider("default-credentials-chain", config);

		// assert

		assertThat(result).isInstanceOf(DefaultCredentialsProvider.class);
	}

	// =========================================================================
	// web-identity-token
	// =========================================================================

	@Test
	public void shouldThrowWhenWebIdentityTokenMissingStsClient() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act — StsWebIdentityTokenFileCredentialsProvider requires an STS client
		//        which is auto-configured from AWS_WEB_IDENTITY_TOKEN_FILE + AWS_ROLE_ARN env vars

		var thrown = catchThrowable(() -> AwsUtils.createCredentialsProvider("web-identity-token", config));

		// assert

		assertThat(thrown).isInstanceOf(NullPointerException.class).hasMessageContaining("STS client must not be null");
	}

	// =========================================================================
	// Unknown authentication-type
	// =========================================================================

	@Test
	public void shouldThrowForUnknownAuthenticationType() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.createCredentialsProvider("oauth2", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown authentication-type").hasMessageContaining("oauth2");
	}
}
