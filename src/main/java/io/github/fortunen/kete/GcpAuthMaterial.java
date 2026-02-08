package io.github.fortunen.kete;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;

import io.github.fortunen.kete.utils.Base64Utils;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.JWTBearerGrant;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.token.AccessToken;

import io.github.fortunen.kete.utils.CertificateUtils;
import io.github.fortunen.kete.utils.JsonUtils;
import io.github.fortunen.kete.utils.JwtUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@EqualsAndHashCode(exclude = {"tokenHolder", "signer"})
@ToString(exclude = {"privateKey", "signer", "tokenHolder"})
public class GcpAuthMaterial {

	public static final long DEFAULT_TOKEN_LIFETIME_SECONDS = 3600;
	public static final String GCP_PUBSUB_SCOPE = "https://www.googleapis.com/auth/pubsub";

	private URI tokenUri;
	private String clientEmail;
	private RSASSASigner signer;
	private RSAPrivateKey privateKey;
	private transient final JwtUtils.CachedTokenHolder tokenHolder = new JwtUtils.CachedTokenHolder();

	@SneakyThrows
	public static GcpAuthMaterial fromCredentialsFilePath(String credentialsFilePath) {

		ValidationUtils.requireNonBlank(credentialsFilePath, "credentials file path is required");

		var path = Path.of(credentialsFilePath.trim());

		ValidationUtils.requireTrue(Files.exists(path), "credentials file does not exist: " + credentialsFilePath);
		ValidationUtils.requireTrue(Files.isRegularFile(path), "credentials file is not a file: " + credentialsFilePath);

		var json = Files.readString(path, StandardCharsets.UTF_8);

		return fromCredentialsJson(json);
	}

	@SneakyThrows
	public static GcpAuthMaterial fromCredentialsText(String credentialsText) {

		ValidationUtils.requireNonBlank(credentialsText, "credentials text is required");

		return fromCredentialsJson(credentialsText.trim());
	}

	@SneakyThrows
	public static GcpAuthMaterial fromCredentialsBase64(String credentialsBase64) {

		ValidationUtils.requireNonBlank(credentialsBase64, "credentials base64 is required");

		var json = new String(Base64Utils.decode(credentialsBase64.trim()), StandardCharsets.UTF_8);

		return fromCredentialsJson(json);
	}

	@SneakyThrows
	private static GcpAuthMaterial fromCredentialsJson(String json) {

		var root = JsonUtils.parseJson(json);

		var material = new GcpAuthMaterial();

		// client_email

		material.clientEmail = ValidationUtils.requireNonBlank(JsonUtils.getString(root, "client_email"), "client_email is required in credentials file");

		// token_uri

		var tokenUriStr = JsonUtils.getString(root, "token_uri");

		if (ValidationUtils.isBlank(tokenUriStr)) {
			tokenUriStr = "https://oauth2.googleapis.com/token";
		}

		material.tokenUri = URI.create(tokenUriStr);

		// private_key

		var privateKeyPem = ValidationUtils.requireNonBlank(JsonUtils.getString(root, "private_key"), "private_key is required in credentials file");

		var parsedKey = CertificateUtils.parsePemPrivateKey(privateKeyPem);

		ValidationUtils.requireNonNull(parsedKey, "failed to parse private_key from credentials file");
		ValidationUtils.requireTrue(parsedKey instanceof RSAPrivateKey, "private_key must be an RSA key");

		material.privateKey = (RSAPrivateKey) parsedKey;

		// signer

		material.signer = new RSASSASigner(material.privateKey);

		return material;
	}

	@SneakyThrows
	public AccessToken getAccessToken() {

		return tokenHolder.getToken(() -> {

			var now = new Date();
			var expiry = new Date(now.getTime() + DEFAULT_TOKEN_LIFETIME_SECONDS * 1000);

			var claimsSet = new JWTClaimsSet.Builder()
				.issuer(clientEmail)
				.subject(clientEmail)
				.audience(tokenUri.toString())
				.issueTime(now)
				.expirationTime(expiry)
				.claim("scope", GCP_PUBSUB_SCOPE)
				.build();

			var signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

			try {
				signedJwt.sign(signer);
			} catch (Exception e) {
				throw new RuntimeException("Failed to sign JWT for GCP auth", e);
			}

			var grant = new JWTBearerGrant(signedJwt);

			return new TokenRequest(tokenUri, grant, null);
		});
	}

}
