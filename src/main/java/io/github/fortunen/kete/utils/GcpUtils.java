package io.github.fortunen.kete.utils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import org.apache.commons.configuration2.MapConfiguration;

import lombok.SneakyThrows;

public final class GcpUtils {

	private GcpUtils() {}

	public static final String CREDENTIALS_FILE_PATH = "credentials-file-path";
	public static final String CREDENTIALS_FILE_TEXT = "credentials-file-text";
	public static final String CREDENTIALS_FILE_BASE64 = "credentials-file-base64";

	@SneakyThrows
	public static GoogleCredentials configureAuthentication(String authenticationType, MapConfiguration configuration, String scope) {

		var credentialsFilePath = configuration.getString(CREDENTIALS_FILE_PATH, "").trim();
		var credentialsFileText = configuration.getString(CREDENTIALS_FILE_TEXT, "").trim();
		var credentialsFileBase64 = configuration.getString(CREDENTIALS_FILE_BASE64, "").trim();
		var credentialsSources = (ValidationUtils.isNotBlank(credentialsFilePath) ? 1 : 0) + (ValidationUtils.isNotBlank(credentialsFileText) ? 1 : 0) + (ValidationUtils.isNotBlank(credentialsFileBase64) ? 1 : 0);

		ValidationUtils.requireFalse(credentialsSources > 1, CREDENTIALS_FILE_PATH + ", " + CREDENTIALS_FILE_TEXT + ", and " + CREDENTIALS_FILE_BASE64 + " are mutually exclusive");

		if (authenticationType.isEmpty()) {
			return null;
		}

		return switch (authenticationType) {
			case "application-default" -> {
				ValidationUtils.requireFalse(credentialsSources > 0, "'" + CREDENTIALS_FILE_PATH + "', '" + CREDENTIALS_FILE_TEXT + "', and '" + CREDENTIALS_FILE_BASE64 + "' cannot be set when authentication-type is 'application-default'");
				yield GoogleCredentials.getApplicationDefault().createScoped(scope);
			}
			case "service-account-file-path" -> {
				var json = Files.readAllBytes(Path.of(ValidationUtils.requireNonBlank(credentialsFilePath, CREDENTIALS_FILE_PATH + " is required when authentication-type is 'service-account-file-path'")));
				yield ServiceAccountCredentials.fromStream(new ByteArrayInputStream(json)).createScoped(scope);
			}
			case "service-account-file-text" -> {
				var json = ValidationUtils.requireNonBlank(credentialsFileText, CREDENTIALS_FILE_TEXT + " is required when authentication-type is 'service-account-file-text'").getBytes(StandardCharsets.UTF_8);
				yield ServiceAccountCredentials.fromStream(new ByteArrayInputStream(json)).createScoped(scope);
			}
			case "service-account-file-base64" -> {
				var json = Base64Utils.decode(ValidationUtils.requireNonBlank(credentialsFileBase64, CREDENTIALS_FILE_BASE64 + " is required when authentication-type is 'service-account-file-base64'"));
				yield ServiceAccountCredentials.fromStream(new ByteArrayInputStream(json)).createScoped(scope);
			}
			default -> throw new IllegalStateException("unsupported authentication-type: '" + authenticationType + "' — valid options: application-default, service-account-file-path, service-account-file-text, service-account-file-base64");
		};
	}
}
