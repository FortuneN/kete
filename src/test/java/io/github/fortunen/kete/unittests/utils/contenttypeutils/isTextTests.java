package io.github.fortunen.kete.unittests.utils.contenttypeutils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.ContentTypeUtils;

public class isTextTests {

	@Test
	public void shouldTreatTextTypesAsText() {

		// act & assert

		assertThat(ContentTypeUtils.isText("text/plain")).isTrue();
		assertThat(ContentTypeUtils.isText("text/csv")).isTrue();
		assertThat(ContentTypeUtils.isText("application/json")).isTrue();
		assertThat(ContentTypeUtils.isText("application/xml")).isTrue();
		assertThat(ContentTypeUtils.isText("application/yaml")).isTrue();
		assertThat(ContentTypeUtils.isText("application/toml")).isTrue();
		assertThat(ContentTypeUtils.isText("text/x-java-properties")).isTrue();
		assertThat(ContentTypeUtils.isText("application/x-www-form-urlencoded")).isTrue();
		assertThat(ContentTypeUtils.isText(" Application/JSON; charset=utf-8 ")).isTrue();
	}

	@Test
	public void shouldTreatBinaryTypesAsBinary() {

		// act & assert

		assertThat(ContentTypeUtils.isText("application/cbor")).isFalse();
		assertThat(ContentTypeUtils.isText("application/x-jackson-smile")).isFalse();
		assertThat(ContentTypeUtils.isText("application/x-protobuf")).isFalse();
		assertThat(ContentTypeUtils.isText("avro/binary")).isFalse();
		assertThat(ContentTypeUtils.isText("application/octet-stream")).isFalse();
		assertThat(ContentTypeUtils.isText("multipart/form-data; boundary=abc")).isFalse();
	}

	@Test
	public void shouldTreatMissingTypeAsBinary() {

		// act & assert

		assertThat(ContentTypeUtils.isText(null)).isFalse();
		assertThat(ContentTypeUtils.isText("  ")).isFalse();
	}
}
