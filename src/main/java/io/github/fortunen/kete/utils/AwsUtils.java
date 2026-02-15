package io.github.fortunen.kete.utils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.configuration2.MapConfiguration;

import lombok.SneakyThrows;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.profiles.ProfileFile;
import software.amazon.awssdk.services.sts.auth.StsWebIdentityTokenFileCredentialsProvider;

public final class AwsUtils {

	private AwsUtils() {}

	public static final String REGION = "region";
	public static final String ACCESS_KEY_ID = "access-key-id";
	public static final String SECRET_ACCESS_KEY = "secret-access-key";
	public static final String CREDENTIALS_PROFILE = "credentials-profile";
	public static final String CREDENTIALS_FILE_PATH = "credentials-file-path";
	public static final String CREDENTIALS_FILE_TEXT = "credentials-file-text";
	public static final String CREDENTIALS_FILE_BASE64 = "credentials-file-base64";

	// --- region resolution ---

	public static String resolveRegion(MapConfiguration configuration) {

		var configRegion = configuration.getString(REGION, "").trim();

		if (ValidationUtils.isNotBlank(configRegion)) {
			return configRegion;
		}

		var envRegion = System.getenv("AWS_REGION");

		if (ValidationUtils.isNotBlank(envRegion)) {
			return envRegion;
		}

		return System.getenv("AWS_DEFAULT_REGION");
	}

	// --- authentication-type cross-contamination validation ---

	public static void validateAuthenticationTypeCrossContamination(String authenticationType, MapConfiguration configuration) {

		switch (authenticationType) {
			case "access-key" -> {
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(CREDENTIALS_FILE_PATH, "").trim()), "'" + CREDENTIALS_FILE_PATH + "' cannot be set when authentication-type is 'access-key'");
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(CREDENTIALS_FILE_TEXT, "").trim()), "'" + CREDENTIALS_FILE_TEXT + "' cannot be set when authentication-type is 'access-key'");
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(CREDENTIALS_FILE_BASE64, "").trim()), "'" + CREDENTIALS_FILE_BASE64 + "' cannot be set when authentication-type is 'access-key'");
			}
			case "environment-variables", "instance-metadata", "container-credentials", "default-credentials-chain", "web-identity-token" -> {
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(ACCESS_KEY_ID, "").trim()), "'" + ACCESS_KEY_ID + "' cannot be set when authentication-type is '" + authenticationType + "'");
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(SECRET_ACCESS_KEY, "").trim()), "'" + SECRET_ACCESS_KEY + "' cannot be set when authentication-type is '" + authenticationType + "'");
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(CREDENTIALS_FILE_PATH, "").trim()), "'" + CREDENTIALS_FILE_PATH + "' cannot be set when authentication-type is '" + authenticationType + "'");
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(CREDENTIALS_FILE_TEXT, "").trim()), "'" + CREDENTIALS_FILE_TEXT + "' cannot be set when authentication-type is '" + authenticationType + "'");
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(CREDENTIALS_FILE_BASE64, "").trim()), "'" + CREDENTIALS_FILE_BASE64 + "' cannot be set when authentication-type is '" + authenticationType + "'");
			}
			case "credentials-file-path" -> {
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(ACCESS_KEY_ID, "").trim()), "'" + ACCESS_KEY_ID + "' cannot be set when authentication-type is 'credentials-file-path'");
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(SECRET_ACCESS_KEY, "").trim()), "'" + SECRET_ACCESS_KEY + "' cannot be set when authentication-type is 'credentials-file-path'");
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(CREDENTIALS_FILE_TEXT, "").trim()), "'" + CREDENTIALS_FILE_TEXT + "' cannot be set when authentication-type is 'credentials-file-path'");
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(CREDENTIALS_FILE_BASE64, "").trim()), "'" + CREDENTIALS_FILE_BASE64 + "' cannot be set when authentication-type is 'credentials-file-path'");
			}
			case "credentials-file-text" -> {
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(ACCESS_KEY_ID, "").trim()), "'" + ACCESS_KEY_ID + "' cannot be set when authentication-type is 'credentials-file-text'");
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(SECRET_ACCESS_KEY, "").trim()), "'" + SECRET_ACCESS_KEY + "' cannot be set when authentication-type is 'credentials-file-text'");
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(CREDENTIALS_FILE_PATH, "").trim()), "'" + CREDENTIALS_FILE_PATH + "' cannot be set when authentication-type is 'credentials-file-text'");
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(CREDENTIALS_FILE_BASE64, "").trim()), "'" + CREDENTIALS_FILE_BASE64 + "' cannot be set when authentication-type is 'credentials-file-text'");
			}
			case "credentials-file-base64" -> {
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(ACCESS_KEY_ID, "").trim()), "'" + ACCESS_KEY_ID + "' cannot be set when authentication-type is 'credentials-file-base64'");
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(SECRET_ACCESS_KEY, "").trim()), "'" + SECRET_ACCESS_KEY + "' cannot be set when authentication-type is 'credentials-file-base64'");
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(CREDENTIALS_FILE_PATH, "").trim()), "'" + CREDENTIALS_FILE_PATH + "' cannot be set when authentication-type is 'credentials-file-base64'");
				ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(CREDENTIALS_FILE_TEXT, "").trim()), "'" + CREDENTIALS_FILE_TEXT + "' cannot be set when authentication-type is 'credentials-file-base64'");
			}
			default -> throw new IllegalArgumentException("Unknown authentication-type: '" + authenticationType + "'");
		}

		// access-key pair must be complete

		var hasAccessKeyId = ValidationUtils.isNotBlank(configuration.getString(ACCESS_KEY_ID, "").trim());
		var hasSecretAccessKey = ValidationUtils.isNotBlank(configuration.getString(SECRET_ACCESS_KEY, "").trim());

		ValidationUtils.requireTrue(hasAccessKeyId == hasSecretAccessKey, "'" + ACCESS_KEY_ID + "' and '" + SECRET_ACCESS_KEY + "' must both be set or both be omitted");
	}

	// --- URL / ARN builders ---

	public static String buildSqsQueueUrl(String endpointUrl, String region, String accountId, String queue) {

		if (ValidationUtils.isNotBlank(endpointUrl)) {
			var base = endpointUrl.endsWith("/") ? endpointUrl : endpointUrl + "/";
			return base + accountId + "/" + queue;
		}

		var tld = region.startsWith("cn-") ? ".amazonaws.com.cn" : ".amazonaws.com";

		return "https://sqs." + region + tld + "/" + accountId + "/" + queue;
	}

	public static String buildSnsTopicArn(String region, String accountId, String topic) {
		return "arn:aws:sns:" + region + ":" + accountId + ":" + topic;
	}

	// --- credentials provider creation ---

	public static AwsCredentialsProvider createCredentialsProvider(String authenticationType, MapConfiguration config) {

		if (ValidationUtils.isBlank(authenticationType)) {
			return AnonymousCredentialsProvider.create();
		}

		return switch (authenticationType) {
			case "access-key" -> createStaticProvider(config);
			case "environment-variables" -> EnvironmentVariableCredentialsProvider.create();
			case "instance-metadata" -> InstanceProfileCredentialsProvider.create();
			case "container-credentials" -> createContainerProvider();
			case "credentials-file-path" -> createProfileProviderFromPath(config);
			case "credentials-file-text" -> createProfileProviderFromText(config);
			case "credentials-file-base64" -> createProfileProviderFromBase64(config);
			case "default-credentials-chain" -> DefaultCredentialsProvider.create();
			case "web-identity-token" -> StsWebIdentityTokenFileCredentialsProvider.builder().build();
			default -> throw new IllegalArgumentException("Unknown authentication-type: '" + authenticationType + "'. Valid values: access-key, environment-variables, instance-metadata, container-credentials, credentials-file-path, credentials-file-text, credentials-file-base64, default-credentials-chain, web-identity-token");
		};
	}

	private static AwsCredentialsProvider createStaticProvider(MapConfiguration config) {

		var accessKeyId = ValidationUtils.requireNonBlank(config.getString(ACCESS_KEY_ID, "").trim(), "'" + ACCESS_KEY_ID + "' is required when authentication-type is 'access-key'");
		var secretAccessKey = ValidationUtils.requireNonBlank(config.getString(SECRET_ACCESS_KEY, "").trim(), "'" + SECRET_ACCESS_KEY + "' is required when authentication-type is 'access-key'");

		return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
	}

	private static AwsCredentialsProvider createContainerProvider() {

		var relativeUri = System.getenv("AWS_CONTAINER_CREDENTIALS_RELATIVE_URI");
		var fullUri = System.getenv("AWS_CONTAINER_CREDENTIALS_FULL_URI");

		ValidationUtils.requireTrue(ValidationUtils.isNotBlank(relativeUri) || ValidationUtils.isNotBlank(fullUri), "authentication-type 'container-credentials' requires the AWS_CONTAINER_CREDENTIALS_RELATIVE_URI or AWS_CONTAINER_CREDENTIALS_FULL_URI environment variable (set automatically by the ECS/EKS agent)");

		return ContainerCredentialsProvider.create();
	}

	private static AwsCredentialsProvider createProfileProviderFromPath(MapConfiguration config) {

		var filePath = ValidationUtils.requireNonBlank(config.getString(CREDENTIALS_FILE_PATH, "").trim(), "'" + CREDENTIALS_FILE_PATH + "' is required when authentication-type is 'credentials-file-path'");
		var profile = resolveProfile(config);
		var profileFile = ProfileFile.builder().content(Path.of(filePath)).type(ProfileFile.Type.CREDENTIALS).build();

		var builder = ProfileCredentialsProvider.builder().profileFile(profileFile);

		if (ValidationUtils.isNotBlank(profile)) {
			builder.profileName(profile);
		}

		return builder.build();
	}

	private static AwsCredentialsProvider createProfileProviderFromText(MapConfiguration config) {

		var text = ValidationUtils.requireNonBlank(config.getString(CREDENTIALS_FILE_TEXT, "").trim(), "'" + CREDENTIALS_FILE_TEXT + "' is required when authentication-type is 'credentials-file-text'");

		return createProfileProviderFromContent(text.getBytes(StandardCharsets.UTF_8), config);
	}

	private static AwsCredentialsProvider createProfileProviderFromBase64(MapConfiguration config) {

		var base64 = ValidationUtils.requireNonBlank(config.getString(CREDENTIALS_FILE_BASE64, "").trim(), "'" + CREDENTIALS_FILE_BASE64 + "' is required when authentication-type is 'credentials-file-base64'");
		var decoded = Base64Utils.decode(base64);

		return createProfileProviderFromContent(decoded, config);
	}

	@SneakyThrows
	private static AwsCredentialsProvider createProfileProviderFromContent(byte[] content, MapConfiguration config) {

		var tempFile = Files.createTempFile("kete-aws-credentials-", ".ini");

		tempFile.toFile().deleteOnExit();

		Files.write(tempFile, content);

		var profile = resolveProfile(config);
		var profileFile = ProfileFile.builder().content(tempFile).type(ProfileFile.Type.CREDENTIALS).build();

		var builder = ProfileCredentialsProvider.builder().profileFile(profileFile);

		if (ValidationUtils.isNotBlank(profile)) {
			builder.profileName(profile);
		}

		return builder.build();
	}

	private static String resolveProfile(MapConfiguration config) {

		var profile = config.getString(CREDENTIALS_PROFILE, "").trim();

		if (ValidationUtils.isNotBlank(profile)) {
			return profile;
		}

		var envProfile = System.getenv("AWS_PROFILE");

		return ValidationUtils.isNotBlank(envProfile) ? envProfile : null;
	}
}
