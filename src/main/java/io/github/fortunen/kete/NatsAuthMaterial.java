package io.github.fortunen.kete;

import io.github.fortunen.kete.utils.Base64Utils;
import io.github.fortunen.kete.utils.JsonUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.nats.client.AuthHandler;
import io.nats.client.Nats;
import io.nats.client.NKey;
import io.nats.client.Options;
import io.nats.client.support.JwtUtils;
import lombok.Data;
import lombok.ToString;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.Configuration;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;

@Data
@Slf4j
@ToString(exclude = {"token", "nkeySeed", "password", "credentialsFileContent"})
public class NatsAuthMaterial {

	public static final String AUTH_NONE = "none";
	public static final String AUTH_NKEY = "nkey";
	public static final String AUTH_TOKEN = "token";
	public static final String AUTHENTICATION_METHOD = "authentication-method";
	public static final String AUTH_CREDENTIALS_FILE_PATH = "credentials-file-path";
	public static final String AUTH_CREDENTIALS_FILE_TEXT = "credentials-file-text";
	public static final String AUTH_USERNAME_AND_PASSWORD = "username-and-password";
	public static final String AUTH_CREDENTIALS_FILE_BASE64 = "credentials-file-base64";

	public static final String TOKEN = "token";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String NKEY_SEED = "nkey-seed";
	public static final String CREDENTIALS_FILE_PATH = "credentials-file-path";
	public static final String CREDENTIALS_FILE_TEXT = "credentials-file-text";
	public static final String CREDENTIALS_FILE_BASE64 = "credentials-file-base64";

	public static final int JWT_EXPIRY_WARNING_DAYS = 30;

	private String token;
	private String username;
	private String nkeySeed;
	private String password;
	private String authenticationMethod;
	private String credentialsFileContent;

	@SneakyThrows
	public void initialize(Configuration configuration) {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		authenticationMethod = ValidationUtils.requireNonBlank(configuration.getString(AUTHENTICATION_METHOD, "").trim(), AUTHENTICATION_METHOD + " is required (none, username-and-password, token, nkey, credentials-file-path, credentials-file-text, credentials-file-base64)");

		switch (authenticationMethod) {

			case AUTH_NONE:
				break;

			case AUTH_USERNAME_AND_PASSWORD:
				username = ValidationUtils.requireNonBlank(configuration.getString(USERNAME, "").trim(), USERNAME + " is required when authentication-method is " + AUTH_USERNAME_AND_PASSWORD);
				password = ValidationUtils.requireNonBlank(configuration.getString(PASSWORD, "").trim(), PASSWORD + " is required when authentication-method is " + AUTH_USERNAME_AND_PASSWORD);
				break;

			case AUTH_TOKEN:
				token = ValidationUtils.requireNonBlank(configuration.getString(TOKEN, "").trim(), TOKEN + " is required when authentication-method is " + AUTH_TOKEN);
				break;

			case AUTH_NKEY:
				nkeySeed = ValidationUtils.requireNonBlank(configuration.getString(NKEY_SEED, "").trim(), NKEY_SEED + " is required when authentication-method is " + AUTH_NKEY);
				break;

			case AUTH_CREDENTIALS_FILE_PATH:
				var filePath = ValidationUtils.requireNonBlank(configuration.getString(CREDENTIALS_FILE_PATH, "").trim(), CREDENTIALS_FILE_PATH + " is required when authentication-method is " + AUTH_CREDENTIALS_FILE_PATH);
				credentialsFileContent = Files.readString(Paths.get(filePath));
				checkJwtExpiry(credentialsFileContent);
				break;

			case AUTH_CREDENTIALS_FILE_TEXT:
				credentialsFileContent = ValidationUtils.requireNonBlank(configuration.getString(CREDENTIALS_FILE_TEXT, "").trim(), CREDENTIALS_FILE_TEXT + " is required when authentication-method is " + AUTH_CREDENTIALS_FILE_TEXT);
				checkJwtExpiry(credentialsFileContent);
				break;

			case AUTH_CREDENTIALS_FILE_BASE64:
				var fileBase64 = ValidationUtils.requireNonBlank(configuration.getString(CREDENTIALS_FILE_BASE64, "").trim(), CREDENTIALS_FILE_BASE64 + " is required when authentication-method is " + AUTH_CREDENTIALS_FILE_BASE64);
				credentialsFileContent = new String(Base64Utils.decode(fileBase64), StandardCharsets.UTF_8);
				checkJwtExpiry(credentialsFileContent);
				break;

			default:
				throw new IllegalStateException("invalid " + AUTHENTICATION_METHOD + ": " + authenticationMethod + " (valid values: none, username-and-password, token, nkey, credentials-file-path, credentials-file-text, credentials-file-base64)");
		}
	}

	public void applyTo(Options.Builder builder) {

		ValidationUtils.requireNonNull(builder, "builder is required");

		switch (authenticationMethod) {

			case AUTH_NONE:
				break;

			case AUTH_USERNAME_AND_PASSWORD:
				builder.userInfo(username, password);
				break;

			case AUTH_TOKEN:
				builder.token(token.toCharArray());
				break;

			case AUTH_NKEY:
				builder.authHandler(createNKeyAuthHandler(nkeySeed));
				break;

			case AUTH_CREDENTIALS_FILE_PATH:
			case AUTH_CREDENTIALS_FILE_TEXT:
			case AUTH_CREDENTIALS_FILE_BASE64:
				builder.authHandler(Nats.staticCredentials(credentialsFileContent.getBytes(StandardCharsets.UTF_8)));
				break;
		}
	}

	private void checkJwtExpiry(String credsContent) {

		try {

			var jwtStart = credsContent.indexOf("-----BEGIN NATS USER JWT-----");
			var jwtEnd = credsContent.indexOf("------END NATS USER JWT------");

			if (jwtStart == -1 || jwtEnd == -1) {
				return;
			}

			var jwt = credsContent.substring(jwtStart + "-----BEGIN NATS USER JWT-----".length(), jwtEnd).trim();
			var claimBody = JwtUtils.getClaimBody(jwt);
			var claims = JsonUtils.parseJson(claimBody);

			var expSeconds = JsonUtils.getLong(claims, "exp");

			if (ValidationUtils.isNull(expSeconds)) {
				return;
			}

			var expiry = Instant.ofEpochSecond(expSeconds);
			var daysUntilExpiry = Duration.between(Instant.now(), expiry).toDays();

			if (daysUntilExpiry <= JWT_EXPIRY_WARNING_DAYS) {
				if (daysUntilExpiry < 0) {
					log.warn("NATS JWT has EXPIRED {} days ago. Authentication will fail.", Math.abs(daysUntilExpiry));
				} else if (daysUntilExpiry == 0) {
					log.warn("NATS JWT expires TODAY. Consider using a longer-lived JWT for background services.");
				} else {
					log.warn("NATS JWT expires in {} days. Consider using a longer-lived JWT for background services.", daysUntilExpiry);
				}
			}

		} catch (Exception e) {
			log.warn("Failed to parse JWT expiry from credentials file: {}", e.getMessage());
		}
	}

	private AuthHandler createNKeyAuthHandler(String seed) {

		return new AuthHandler() {

			private NKey nKey;

			private NKey getNKey() throws GeneralSecurityException {
				if (ValidationUtils.isNull(nKey)) {
					nKey = NKey.fromSeed(seed.toCharArray());
				}
				return nKey;
			}

			@Override
			public char[] getID() {
				try {
					return getNKey().getPublicKey();
				} catch (Exception e) {
					throw new RuntimeException("Failed to get NKey public key", e);
				}
			}

			@Override
			public byte[] sign(byte[] nonce) {
				try {
					return getNKey().sign(nonce);
				} catch (Exception e) {
					throw new RuntimeException("Failed to sign nonce with NKey", e);
				}
			}

			@Override
			public char[] getJWT() {
				return null;
			}
		};
	}
}
