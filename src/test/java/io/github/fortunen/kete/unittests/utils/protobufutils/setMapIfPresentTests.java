package io.github.fortunen.kete.unittests.utils.protobufutils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.protobuf.DynamicMessage;

import io.github.fortunen.kete.utils.ProtobufUtils;

public class setMapIfPresentTests {

	@Test
	public void shouldSetMapFieldWhenMapIsPopulated() throws Exception {

		// arrange

		var desc = ProtobufUtils.findMessage("Event");
		var builder = DynamicMessage.newBuilder(desc);

		// act

		ProtobufUtils.setMapIfPresent(builder, desc, "details",
				Map.of("k1", "v1", "k2", "v2"));

		// assert

		var bytes = builder.build().toByteArray();
		var parsed = DynamicMessage.parseFrom(desc, bytes);
		var detailsField = desc.findFieldByName("details");
		var entryDesc = detailsField.getMessageType();
		@SuppressWarnings("unchecked")
		var entries = (java.util.List<DynamicMessage>) parsed.getField(detailsField);
		var result = new HashMap<String, String>();
		for (var entry : entries) {
			result.put(
					(String) entry.getField(entryDesc.findFieldByName("key")),
					(String) entry.getField(entryDesc.findFieldByName("value")));
		}
		assertThat(result).containsExactlyInAnyOrderEntriesOf(
				Map.of("k1", "v1", "k2", "v2"));
	}

	@Test
	public void shouldSkipWhenMapIsNull() throws Exception {

		// arrange

		var desc = ProtobufUtils.findMessage("Event");
		var builder = DynamicMessage.newBuilder(desc);

		// act

		ProtobufUtils.setMapIfPresent(builder, desc, "details", null);

		// assert

		var bytes = builder.build().toByteArray();
		var parsed = DynamicMessage.parseFrom(desc, bytes);
		@SuppressWarnings("unchecked")
		var entries = (java.util.List<DynamicMessage>) parsed.getField(
				desc.findFieldByName("details"));
		assertThat(entries).isEmpty();
	}

	@Test
	public void shouldSkipWhenFieldNameDoesNotExist() {

		// arrange

		var desc = ProtobufUtils.findMessage("Event");
		var builder = DynamicMessage.newBuilder(desc);

		// act

		ProtobufUtils.setMapIfPresent(builder, desc, "nonexistent_field",
				Map.of("k", "v"));

		// assert

		var msg = builder.build();
		assertThat(msg).isNotNull();
	}
}
