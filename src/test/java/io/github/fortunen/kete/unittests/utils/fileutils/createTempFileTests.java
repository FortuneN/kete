package io.github.fortunen.kete.unittests.utils.fileutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.FileUtils;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class createTempFileTests {

	@Test
	public void shouldCreateTempFileWithSuffix() throws IOException {

		// act

		var file = FileUtils.createTempFile("-test.txt");

		// assert

		assertThat(file).isNotNull();
		assertThat(file.exists()).isTrue();
		assertThat(file.getName()).endsWith("-test.txt");
		assertThat(file.getAbsolutePath()).isNotEmpty();
	}

	@Test
	public void shouldCreateTempFileWithNullSuffix() throws IOException {

		// act

		var file = FileUtils.createTempFile(null);

		// assert

		assertThat(file).isNotNull();
		assertThat(file.exists()).isTrue();
		assertThat(file.getAbsolutePath()).isNotEmpty();
	}

	@Test
	public void shouldCreateTempFileWithEmptySuffix() throws IOException {

		// act

		var file = FileUtils.createTempFile("");

		// assert

		assertThat(file).isNotNull();
		assertThat(file.exists()).isTrue();
		assertThat(file.getAbsolutePath()).isNotEmpty();
	}

	@Test
	public void shouldCreateTempFileWithPemSuffix() throws IOException {

		// act

		var file = FileUtils.createTempFile("-server.pem");

		// assert

		assertThat(file).isNotNull();
		assertThat(file.exists()).isTrue();
		assertThat(file.getName()).endsWith("-server.pem");
	}

	@Test
	public void shouldCreateTempFileWithJksSuffix() throws IOException {

		// act

		var file = FileUtils.createTempFile("-server.jks");

		// assert

		assertThat(file).isNotNull();
		assertThat(file.exists()).isTrue();
		assertThat(file.getName()).endsWith("-server.jks");
	}

	@Test
	public void shouldCreateUniqueTempFilesOnMultipleCalls() throws IOException {

		// act

		var file1 = FileUtils.createTempFile("-test.txt");
		var file2 = FileUtils.createTempFile("-test.txt");

		// assert

		assertThat(file1.getAbsolutePath()).isNotEqualTo(file2.getAbsolutePath());
		assertThat(file1.exists()).isTrue();
		assertThat(file2.exists()).isTrue();
	}

	@Test
	public void shouldCreateTempFileInSystemTempDirectory() throws IOException {

		// arrange

		var tempDir = System.getProperty("java.io.tmpdir");

		// act

		var file = FileUtils.createTempFile("-test.txt");

		// assert

		assertThat(file.getAbsolutePath()).startsWith(tempDir);
	}

	@Test
	public void shouldCreateWritableFile() throws IOException {

		// act

		var file = FileUtils.createTempFile("-test.txt");

		// assert

		assertThat(file.canWrite()).isTrue();
	}

	@Test
	public void shouldCreateReadableFile() throws IOException {

		// act

		var file = FileUtils.createTempFile("-test.txt");

		// assert

		assertThat(file.canRead()).isTrue();
	}
}
