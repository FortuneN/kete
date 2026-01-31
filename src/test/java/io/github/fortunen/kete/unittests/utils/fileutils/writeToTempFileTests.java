package io.github.fortunen.kete.unittests.utils.fileutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class writeToTempFileTests {

	@Test
	public void shouldWriteContentToTempFileAndReturnPath() throws IOException {

		// arrange

		var content = "test content";

		// act

		var path = FileUtils.writeToTempFile(content, "-test.txt");

		// assert

		assertThat(path).isNotNull();
		assertThat(path).isNotEmpty();
		assertThat(Files.exists(Path.of(path))).isTrue();
		assertThat(Files.readString(Path.of(path))).isEqualTo("test content");
	}

	@Test
	public void shouldWriteEmptyContentToTempFile() throws IOException {

		// arrange

		var content = "";

		// act

		var path = FileUtils.writeToTempFile(content, "-test.txt");

		// assert

		assertThat(path).isNotNull();
		assertThat(Files.readString(Path.of(path))).isEmpty();
	}

	@Test
	public void shouldWriteMultilineContentToTempFile() throws IOException {

		// arrange

		var content = "line1\nline2\nline3";

		// act

		var path = FileUtils.writeToTempFile(content, "-test.txt");

		// assert

		assertThat(Files.readString(Path.of(path))).isEqualTo("line1\nline2\nline3");
	}

	@Test
	public void shouldWriteUnicodeContentToTempFile() throws IOException {

		// arrange

		var content = "こんにちは世界 🌍 مرحبا";

		// act

		var path = FileUtils.writeToTempFile(content, "-test.txt");

		// assert

		assertThat(Files.readString(Path.of(path))).isEqualTo("こんにちは世界 🌍 مرحبا");
	}

	@Test
	public void shouldWritePemCertificateContent() throws IOException {

		// arrange

		var pemContent = "-----BEGIN CERTIFICATE-----\n" +
			"MIIBkTCB+wIJALYFf7MQOsHvMA0GCSqGSIb3DQEBCwUAMBExDzANBgNVBAMMBnNl\n" +
			"cnZlcjAeFw0yNDAxMDEwMDAwMDBaFw0yNTAxMDEwMDAwMDBaMBExDzANBgNVBAMM\n" +
			"BnNlcnZlcjBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQC8n3oHMP+H8n0zzOMOq3JM\n" +
			"-----END CERTIFICATE-----";

		// act

		var path = FileUtils.writeToTempFile(pemContent, "-server.pem");

		// assert

		assertThat(path).endsWith("-server.pem");
		assertThat(Files.readString(Path.of(path))).isEqualTo(pemContent);
	}

	@Test
	public void shouldWriteToTempFileWithNullSuffix() throws IOException {

		// arrange

		var content = "test content";

		// act

		var path = FileUtils.writeToTempFile(content, null);

		// assert

		assertThat(path).isNotNull();
		assertThat(Files.readString(Path.of(path))).isEqualTo("test content");
	}

	@Test
	public void shouldReturnAbsolutePath() throws IOException {

		// arrange

		var content = "test";

		// act

		var path = FileUtils.writeToTempFile(content, "-test.txt");

		// assert

		assertThat(Path.of(path).isAbsolute()).isTrue();
	}

	@Test
	public void shouldCreateUniqueFilesOnMultipleCalls() throws IOException {

		// arrange

		var content = "same content";

		// act

		var path1 = FileUtils.writeToTempFile(content, "-test.txt");
		var path2 = FileUtils.writeToTempFile(content, "-test.txt");

		// assert

		assertThat(path1).isNotEqualTo(path2);
	}

	@Test
	public void shouldWriteVeryLongContentToTempFile() throws IOException {

		// arrange

		var content = "x".repeat(100000);

		// act

		var path = FileUtils.writeToTempFile(content, "-large.txt");

		// assert

		assertThat(Files.readString(Path.of(path))).hasSize(100000);
		assertThat(Files.readString(Path.of(path))).isEqualTo(content);
	}

	@Test
	public void shouldWriteSpecialCharactersToTempFile() throws IOException {

		// arrange

		var content = "tab:\there\nnewline\rcarriage\r\nboth\0null";

		// act

		var path = FileUtils.writeToTempFile(content, "-special.txt");

		// assert

		assertThat(Files.readString(Path.of(path))).isEqualTo(content);
	}
}
