package io.github.fortunen.kete.utils;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

import lombok.SneakyThrows;

public final class FileUtils {

	private FileUtils() {}

	@SneakyThrows
	public static File createTempFile(String suffix) {

		var file = Files.createTempFile(UUID.randomUUID().toString(), suffix).toFile();

		file.deleteOnExit();

		return file;
	}

	@SneakyThrows
	public static String writeToTempFile(String content, String suffix) {

		ValidationUtils.requireNonNull(content, "content is required");

		var file = createTempFile(suffix);

		Files.writeString(file.toPath(), content);

		return file.getAbsolutePath();
	}
}
