package io.github.fortunen.kete.unittests.utils.awsutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.AwsUtils;

public class validateAuthenticationTypeCrossContaminationTests {

	// =========================================================================
	// access-key — valid
	// =========================================================================

	@Test
	public void shouldPassForAccessKeyWithValidKeys() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("access-key-id", "AKIAIOSFODNN7EXAMPLE");
		map.put("secret-access-key", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("access-key", config));

		// assert

		assertThat(thrown).isNull();
	}

	@Test
	public void shouldPassForAccessKeyWithNoKeys() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("access-key", config));

		// assert

		assertThat(thrown).isNull();
	}

	// =========================================================================
	// access-key — cross-contamination
	// =========================================================================

	@Test
	public void shouldThrowForAccessKeyWithCredentialsFilePath() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("access-key-id", "AKIAIOSFODNN7EXAMPLE");
		map.put("secret-access-key", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
		map.put("credentials-file-path", "/some/path");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("access-key", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("credentials-file-path");
	}

	@Test
	public void shouldThrowForAccessKeyWithCredentialsFileText() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("access-key-id", "AKIAIOSFODNN7EXAMPLE");
		map.put("secret-access-key", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
		map.put("credentials-file-text", "[default]\naws_access_key_id=X");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("access-key", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("credentials-file-text");
	}

	@Test
	public void shouldThrowForAccessKeyWithCredentialsFileBase64() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("access-key-id", "AKIAIOSFODNN7EXAMPLE");
		map.put("secret-access-key", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
		map.put("credentials-file-base64", "W2RlZmF1bHRd");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("access-key", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("credentials-file-base64");
	}

	// =========================================================================
	// access-key — incomplete pair
	// =========================================================================

	@Test
	public void shouldThrowWhenAccessKeyIdSetButSecretMissing() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("access-key-id", "AKIAIOSFODNN7EXAMPLE");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("access-key", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("must both be set or both be omitted");
	}

	@Test
	public void shouldThrowWhenSecretSetButAccessKeyIdMissing() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("secret-access-key", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("access-key", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("must both be set or both be omitted");
	}

	// =========================================================================
	// environment-variables — valid
	// =========================================================================

	@Test
	public void shouldPassForEnvironmentVariablesWithNoOtherFields() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("environment-variables", config));

		// assert

		assertThat(thrown).isNull();
	}

	// =========================================================================
	// environment-variables — cross-contamination
	// =========================================================================

	@Test
	public void shouldThrowForEnvironmentVariablesWithAccessKeyId() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("access-key-id", "AKIAIOSFODNN7EXAMPLE");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("environment-variables", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("access-key-id");
	}

	@Test
	public void shouldThrowForEnvironmentVariablesWithSecretAccessKey() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("secret-access-key", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("environment-variables", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("secret-access-key");
	}

	@Test
	public void shouldThrowForEnvironmentVariablesWithCredentialsFilePath() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-path", "/some/path");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("environment-variables", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("credentials-file-path");
	}

	// =========================================================================
	// instance-metadata — cross-contamination
	// =========================================================================

	@Test
	public void shouldPassForInstanceMetadataWithNoOtherFields() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("instance-metadata", config));

		// assert

		assertThat(thrown).isNull();
	}

	@Test
	public void shouldThrowForInstanceMetadataWithAccessKeyId() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("access-key-id", "AKIAIOSFODNN7EXAMPLE");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("instance-metadata", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("access-key-id");
	}

	// =========================================================================
	// container-credentials — cross-contamination
	// =========================================================================

	@Test
	public void shouldPassForContainerCredentialsWithNoOtherFields() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("container-credentials", config));

		// assert

		assertThat(thrown).isNull();
	}

	@Test
	public void shouldPassForContainerCredentialsWithCredentialsFileText() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-text", "[default]\naws_access_key_id=X");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("container-credentials", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("credentials-file-text");
	}

	// =========================================================================
	// default-credentials-chain — valid & cross-contamination
	// =========================================================================

	@Test
	public void shouldPassForDefaultCredentialsChainWithNoOtherFields() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("default-credentials-chain", config));

		// assert

		assertThat(thrown).isNull();
	}

	@Test
	public void shouldThrowForDefaultCredentialsChainWithAccessKeyId() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("access-key-id", "AKIAIOSFODNN7EXAMPLE");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("default-credentials-chain", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("access-key-id");
	}

	// =========================================================================
	// web-identity-token — valid & cross-contamination
	// =========================================================================

	@Test
	public void shouldPassForWebIdentityTokenWithNoOtherFields() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("web-identity-token", config));

		// assert

		assertThat(thrown).isNull();
	}

	@Test
	public void shouldThrowForWebIdentityTokenWithCredentialsFilePath() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-path", "/some/path");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("web-identity-token", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("credentials-file-path");
	}

	// =========================================================================
	// credentials-file-path — valid & cross-contamination
	// =========================================================================

	@Test
	public void shouldPassForCredentialsFilePathWithNoOtherFields() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-path", "/home/user/.aws/credentials");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("credentials-file-path", config));

		// assert

		assertThat(thrown).isNull();
	}

	@Test
	public void shouldThrowForCredentialsFilePathWithAccessKeyId() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-path", "/home/user/.aws/credentials");
		map.put("access-key-id", "AKIAIOSFODNN7EXAMPLE");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("credentials-file-path", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("access-key-id");
	}

	@Test
	public void shouldThrowForCredentialsFilePathWithCredentialsFileText() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-path", "/home/user/.aws/credentials");
		map.put("credentials-file-text", "[default]\naws_access_key_id=X");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("credentials-file-path", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("credentials-file-text");
	}

	@Test
	public void shouldThrowForCredentialsFilePathWithCredentialsFileBase64() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-path", "/home/user/.aws/credentials");
		map.put("credentials-file-base64", "W2RlZmF1bHRd");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("credentials-file-path", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("credentials-file-base64");
	}

	// =========================================================================
	// credentials-file-text — valid & cross-contamination
	// =========================================================================

	@Test
	public void shouldPassForCredentialsFileTextWithNoOtherFields() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-text", "[default]\naws_access_key_id=AKIAIOSFODNN7EXAMPLE\naws_secret_access_key=wJalrXUtnFEMI");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("credentials-file-text", config));

		// assert

		assertThat(thrown).isNull();
	}

	@Test
	public void shouldThrowForCredentialsFileTextWithAccessKeyId() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-text", "[default]\naws_access_key_id=X");
		map.put("access-key-id", "AKIAIOSFODNN7EXAMPLE");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("credentials-file-text", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("access-key-id");
	}

	@Test
	public void shouldThrowForCredentialsFileTextWithCredentialsFilePath() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-text", "[default]\naws_access_key_id=X");
		map.put("credentials-file-path", "/some/path");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("credentials-file-text", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("credentials-file-path");
	}

	// =========================================================================
	// credentials-file-base64 — valid & cross-contamination
	// =========================================================================

	@Test
	public void shouldPassForCredentialsFileBase64WithNoOtherFields() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-base64", "W2RlZmF1bHRdCmF3c19hY2Nlc3Nfa2V5X2lkPUFLSUFJT1NGT0ROTjdFWEFNUExFCg==");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("credentials-file-base64", config));

		// assert

		assertThat(thrown).isNull();
	}

	@Test
	public void shouldThrowForCredentialsFileBase64WithSecretAccessKey() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-base64", "W2RlZmF1bHRd");
		map.put("secret-access-key", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("credentials-file-base64", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("secret-access-key");
	}

	@Test
	public void shouldThrowForCredentialsFileBase64WithCredentialsFileText() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-base64", "W2RlZmF1bHRd");
		map.put("credentials-file-text", "[default]\naws_access_key_id=X");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("credentials-file-base64", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("credentials-file-text");
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

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("unknown-type", config));

		// assert

		assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown authentication-type");
	}

	// =========================================================================
	// Blank values should NOT trigger cross-contamination
	// =========================================================================

	@Test
	public void shouldNotThrowWhenCrossContaminatingFieldIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-path", "   ");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("access-key", config));

		// assert

		assertThat(thrown).isNull();
	}

	@Test
	public void shouldNotThrowWhenCrossContaminatingFieldIsEmpty() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("credentials-file-text", "");
		var config = new MapConfiguration(map);

		// act

		var thrown = catchThrowable(() -> AwsUtils.validateAuthenticationTypeCrossContamination("access-key", config));

		// assert

		assertThat(thrown).isNull();
	}
}
