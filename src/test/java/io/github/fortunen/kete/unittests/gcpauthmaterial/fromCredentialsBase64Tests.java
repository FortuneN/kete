package io.github.fortunen.kete.unittests.gcpauthmaterial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;

import io.github.fortunen.kete.GcpAuthMaterial;
import okhttp3.tls.HeldCertificate;
import org.junit.jupiter.api.Test;

public class fromCredentialsBase64Tests {

	@Test
	public void shouldParseValidCredentialsBase64() {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("test").rsa2048().build();
		var privateKeyPem = heldCert.privateKeyPkcs8Pem().replace("\n", "\\n");

		var json = """
			{
				"type": "service_account",
				"client_email": "test@test-project.iam.gserviceaccount.com",
				"token_uri": "https://oauth2.googleapis.com/token",
				"private_key": "%s"
			}
			""".formatted(privateKeyPem);

		var base64 = Base64.getEncoder().encodeToString(json.getBytes());

		// act

		var result = GcpAuthMaterial.fromCredentialsBase64(base64);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.getClientEmail()).isEqualTo("test@test-project.iam.gserviceaccount.com");
		assertThat(result.getTokenUri().toString()).isEqualTo("https://oauth2.googleapis.com/token");
		assertThat(result.getPrivateKey()).isNotNull().isInstanceOf(RSAPrivateKey.class);
		assertThat(result.getSigner()).isNotNull();
	}

	@Test
	public void shouldDefaultTokenUriWhenMissing() {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("test").rsa2048().build();
		var privateKeyPem = heldCert.privateKeyPkcs8Pem().replace("\n", "\\n");

		var json = """
			{
				"type": "service_account",
				"client_email": "test@test-project.iam.gserviceaccount.com",
				"private_key": "%s"
			}
			""".formatted(privateKeyPem);

		var base64 = Base64.getEncoder().encodeToString(json.getBytes());

		// act

		var result = GcpAuthMaterial.fromCredentialsBase64(base64);

		// assert

		assertThat(result.getTokenUri().toString()).isEqualTo("https://oauth2.googleapis.com/token");
	}

	@Test
	public void shouldThrowWhenBase64IsNull() {

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsBase64(null);
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials base64 is required");
	}

	@Test
	public void shouldThrowWhenBase64IsEmpty() {

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsBase64("");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials base64 is required");
	}

	@Test
	public void shouldThrowWhenBase64IsBlank() {

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsBase64("   ");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials base64 is required");
	}

	@Test
	public void shouldThrowWhenClientEmailIsMissing() {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("test").rsa2048().build();
		var privateKeyPem = heldCert.privateKeyPkcs8Pem().replace("\n", "\\n");

		var json = """
			{
				"type": "service_account",
				"private_key": "%s"
			}
			""".formatted(privateKeyPem);

		var base64 = Base64.getEncoder().encodeToString(json.getBytes());

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsBase64(base64);
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("client_email is required in credentials file");
	}

	@Test
	public void shouldThrowWhenPrivateKeyIsMissing() {

		// arrange

		var json = """
			{
				"type": "service_account",
				"client_email": "test@test-project.iam.gserviceaccount.com"
			}
			""";

		var base64 = Base64.getEncoder().encodeToString(json.getBytes());

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsBase64(base64);
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("private_key is required in credentials file");
	}

	@Test
	public void shouldThrowWhenBase64IsInvalid() {

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsBase64("!!!not-valid-base64!!!");
		});

		// assert

		assertThat(thrown).isNotNull();
	}
}
