package io.github.fortunen.kete.unittests.gcpauthmaterial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.nio.file.Files;
import java.security.interfaces.RSAPrivateKey;

import io.github.fortunen.kete.GcpAuthMaterial;
import okhttp3.tls.HeldCertificate;
import org.junit.jupiter.api.Test;

public class fromCredentialsFilePathTests {

	@Test
	public void shouldParseValidCredentialsFile() throws IOException {

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

		var tempFile = Files.createTempFile("gcp-creds-", ".json");
		Files.writeString(tempFile, json);

		try {

			// act

			var result = GcpAuthMaterial.fromCredentialsFilePath(tempFile.toString());

			// assert

			assertThat(result).isNotNull();
			assertThat(result.getClientEmail()).isEqualTo("test@test-project.iam.gserviceaccount.com");
			assertThat(result.getTokenUri().toString()).isEqualTo("https://oauth2.googleapis.com/token");
			assertThat(result.getPrivateKey()).isNotNull().isInstanceOf(RSAPrivateKey.class);
			assertThat(result.getSigner()).isNotNull();

		} finally {
			Files.deleteIfExists(tempFile);
		}
	}

	@Test
	public void shouldDefaultTokenUriWhenMissing() throws IOException {

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

		var tempFile = Files.createTempFile("gcp-creds-", ".json");
		Files.writeString(tempFile, json);

		try {

			// act

			var result = GcpAuthMaterial.fromCredentialsFilePath(tempFile.toString());

			// assert

			assertThat(result.getTokenUri().toString()).isEqualTo("https://oauth2.googleapis.com/token");

		} finally {
			Files.deleteIfExists(tempFile);
		}
	}

	@Test
	public void shouldThrowWhenPathIsNull() {

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsFilePath(null);
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials file path is required");
	}

	@Test
	public void shouldThrowWhenPathIsEmpty() {

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsFilePath("");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials file path is required");
	}

	@Test
	public void shouldThrowWhenPathIsBlank() {

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsFilePath("   ");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials file path is required");
	}

	@Test
	public void shouldThrowWhenFileDoesNotExist() {

		// act

		var thrown = catchThrowable(() -> {
			GcpAuthMaterial.fromCredentialsFilePath("/nonexistent/path/creds.json");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("does not exist");
	}

	@Test
	public void shouldThrowWhenClientEmailIsMissing() throws IOException {

		// arrange

		var heldCert = new HeldCertificate.Builder().commonName("test").rsa2048().build();
		var privateKeyPem = heldCert.privateKeyPkcs8Pem().replace("\n", "\\n");

		var json = """
			{
				"type": "service_account",
				"private_key": "%s"
			}
			""".formatted(privateKeyPem);

		var tempFile = Files.createTempFile("gcp-creds-", ".json");
		Files.writeString(tempFile, json);

		try {

			// act

			var thrown = catchThrowable(() -> {
				GcpAuthMaterial.fromCredentialsFilePath(tempFile.toString());
			});

			// assert

			assertThat(thrown)
				.isNotNull()
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("client_email is required in credentials file");

		} finally {
			Files.deleteIfExists(tempFile);
		}
	}

	@Test
	public void shouldThrowWhenPrivateKeyIsMissing() throws IOException {

		// arrange

		var json = """
			{
				"type": "service_account",
				"client_email": "test@test-project.iam.gserviceaccount.com"
			}
			""";

		var tempFile = Files.createTempFile("gcp-creds-", ".json");
		Files.writeString(tempFile, json);

		try {

			// act

			var thrown = catchThrowable(() -> {
				GcpAuthMaterial.fromCredentialsFilePath(tempFile.toString());
			});

			// assert

			assertThat(thrown)
				.isNotNull()
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("private_key is required in credentials file");

		} finally {
			Files.deleteIfExists(tempFile);
		}
	}

	@Test
	public void shouldThrowWhenPathIsDirectory() throws IOException {

		// arrange

		var tempDir = Files.createTempDirectory("gcp-creds-dir-");

		try {

			// act

			var thrown = catchThrowable(() -> {
				GcpAuthMaterial.fromCredentialsFilePath(tempDir.toString());
			});

			// assert

			assertThat(thrown)
				.isNotNull()
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("is not a file");

		} finally {
			Files.deleteIfExists(tempDir);
		}
	}
}
