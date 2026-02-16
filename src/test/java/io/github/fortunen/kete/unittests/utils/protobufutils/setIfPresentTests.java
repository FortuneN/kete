package io.github.fortunen.kete.unittests.utils.protobufutils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.google.protobuf.DynamicMessage;

import io.github.fortunen.kete.utils.ProtobufUtils;

public class setIfPresentTests {

	@Test
	public void shouldSetFieldWhenBothFieldAndValueExist() throws Exception {

		// arrange

		var desc = ProtobufUtils.findMessage("Event");
		var builder = DynamicMessage.newBuilder(desc);

		// act

		ProtobufUtils.setIfPresent(builder, desc, "id", "test-id");

		// assert

		var bytes = builder.build().toByteArray();
		var parsed = DynamicMessage.parseFrom(desc, bytes);
		assertThat(parsed.getField(desc.findFieldByName("id")))
				.isEqualTo("test-id");
	}

	@Test
	public void shouldSkipWhenValueIsNull() throws Exception {

		// arrange

		var desc = ProtobufUtils.findMessage("Event");
		var builder = DynamicMessage.newBuilder(desc);

		// act

		ProtobufUtils.setIfPresent(builder, desc, "id", null);

		// assert

		var bytes = builder.build().toByteArray();
		var parsed = DynamicMessage.parseFrom(desc, bytes);
		assertThat(parsed.getField(desc.findFieldByName("id")))
				.isEqualTo("");
	}

	@Test
	public void shouldSkipWhenFieldNameDoesNotExist() {

		// arrange

		var desc = ProtobufUtils.findMessage("Event");
		var builder = DynamicMessage.newBuilder(desc);

		// act

		ProtobufUtils.setIfPresent(builder, desc, "nonexistent_field", "value");

		// assert

		var msg = builder.build();
		assertThat(msg).isNotNull();
	}
}
