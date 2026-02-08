package io.github.fortunen.kete.unittests.gcpauthmaterial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.security.interfaces.RSAPrivateKey;

import io.github.fortunen.kete.GcpAuthMaterial;
import okhttp3.tls.HeldCertificate;
import org.junit.jupiter.api.Test;

public class fromCredentialsTextTests {

	@Test
	public void shouldParseValidCredentialsText() {

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

		// act

		var result = GcpAuthMaterial.fromCredentialsText(json);

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

		// act

		var result = GcpAuthMaterial.fromCredentialsText(json);

		// assert

		assertThat(result.getTokenUri().toString()).isEqualTo("https://oauth2.googleapis.com/token");
	}

	@Test
	public void shouldThrowWhenTextIsNull() {

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsText(null);
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials text is required");
	}

	@Test
	public void shouldThrowWhenTextIsEmpty() {

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsText("");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials text is required");
	}

	@Test
	public void shouldThrowWhenTextIsBlank() {

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsText("   ");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials text is required");
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

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsText(json);
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

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsText(json);
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("private_key is required in credentials file");
	}

	@Test
	public void shouldThrowWhenTextIsInvalidJson() {

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsText("not valid json");
		});

		// assert

		assertThat(thrown).isNotNull();
	}
}
