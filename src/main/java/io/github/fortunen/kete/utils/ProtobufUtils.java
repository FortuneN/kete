package io.github.fortunen.kete.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;

public final class ProtobufUtils {

	private ProtobufUtils() {}

	private static final String DESCRIPTOR_RESOURCE = "schemas/protobuf/protobuf.desc";

	private static final Map<String, FileDescriptor> FILE_DESCRIPTORS;

	static {
		try (var stream = ProtobufUtils.class.getClassLoader().getResourceAsStream(DESCRIPTOR_RESOURCE)) {
			FILE_DESCRIPTORS = loadDescriptors(stream);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to load protobuf descriptors from " + DESCRIPTOR_RESOURCE, e);
		}
	}

	private static Map<String, FileDescriptor> loadDescriptors(InputStream descStream) throws IOException {

		if (descStream == null) {
			throw new IOException("Descriptor stream is null (resource not found on classpath)");
		}

		var descBytes = descStream.readAllBytes();
		var fileDescSet = FileDescriptorSet.parseFrom(descBytes);

		return buildFileDescriptors(fileDescSet);
	}

	public static Descriptor findMessage(String messageName) {
		return FILE_DESCRIPTORS.values().stream()
			.flatMap(fd -> fd.getMessageTypes().stream())
			.filter(d -> d.getName().equals(messageName))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("Message not found in descriptors: " + messageName));
	}

	public static void setIfPresent(DynamicMessage.Builder builder, Descriptor desc, String fieldName, Object value) {

		var field = desc.findFieldByName(fieldName);

		if (field != null && value != null) {
			builder.setField(field, value);
		}
	}

	public static void setMapIfPresent(DynamicMessage.Builder builder, Descriptor desc, String fieldName, Map<String, String> map) {

		if (map == null) {
			return;
		}

		var field = desc.findFieldByName(fieldName);

		if (field == null) {
			return;
		}

		var entryDesc = field.getMessageType();

		map.forEach((k, v) -> builder
			.addRepeatedField(field, DynamicMessage.newBuilder(entryDesc)
			.setField(entryDesc.findFieldByName("key"), k)
			.setField(entryDesc.findFieldByName("value"), v)
			.build()));
	}

	private static Map<String, FileDescriptor> buildFileDescriptors(FileDescriptorSet fileDescSet) throws IOException {

		var built = new HashMap<String, FileDescriptor>();

		for (var fdProto : fileDescSet.getFileList()) {
			buildFileDescriptor(fdProto, fileDescSet, built);
		}

		return built;
	}

	private static FileDescriptor buildFileDescriptor(FileDescriptorProto fdProto, FileDescriptorSet fileDescSet, Map<String, FileDescriptor> built) throws IOException {

		if (built.containsKey(fdProto.getName())) {
			return built.get(fdProto.getName());
		}

		var deps = new FileDescriptor[fdProto.getDependencyCount()];

		for (var i = 0; i < fdProto.getDependencyCount(); i++) {

			var depName = fdProto.getDependency(i);

			var depProto = fileDescSet.getFileList().stream()
				.filter(f -> f.getName().equals(depName))
				.findFirst()
				.orElseThrow(() -> new IOException("Missing dependency: " + depName));

			deps[i] = buildFileDescriptor(depProto, fileDescSet, built);
		}

		try {
			var fd = FileDescriptor.buildFrom(fdProto, deps);
			built.put(fdProto.getName(), fd);
			return fd;
		} catch (Exception e) {
			throw new IOException("Failed to build descriptor: " + fdProto.getName(), e);
		}
	}
}
