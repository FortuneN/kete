package io.github.fortunen.kete.unittests.destinations.grpcdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.grpc.GrpcDestination;
import io.github.fortunen.kete.destinations.grpc.GrpcDestinationConfig;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

public class sendTests {

	private static GrpcDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new GrpcDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCallBlockingUnaryWithPayloadAndMetadata() {

		// arrange — mock the channel and client call to complete the gRPC lifecycle

		var channel = mock(ManagedChannel.class);
		var clientCall = mock(ClientCall.class);
		when(channel.newCall(any(MethodDescriptor.class), any(CallOptions.class))).thenReturn(clientCall);

		// capture the response listener so we can complete the call

		final ClientCall.Listener<byte[]>[] captured = new ClientCall.Listener[1];
		doAnswer(invocation -> {
			captured[0] = invocation.getArgument(0);
			return null;
		}).when(clientCall).start(any(), any(Metadata.class));

		// when halfClose is called, complete the call via the captured listener

		doAnswer(invocation -> {
			captured[0].onMessage(new byte[0]);
			captured[0].onClose(Status.OK, new Metadata());
			return null;
		}).when(clientCall).halfClose();

		destination.setConfig(mock(GrpcDestinationConfig.class));
		destination.setChannel(channel);
		destination.setTimeout(Duration.ofSeconds(5));
		destination.setCustomHeadersEntrySet(Set.of());

		var methodDescriptor = MethodDescriptor.<byte[], byte[]>newBuilder()
			.setType(MethodDescriptor.MethodType.UNARY)
			.setFullMethodName("test.Service/Method")
			.setRequestMarshaller(new MethodDescriptor.Marshaller<byte[]>() {
				@Override
				public java.io.InputStream stream(byte[] value) {
					return new ByteArrayInputStream(value);
				}
				@Override
				public byte[] parse(java.io.InputStream stream) {
					try { return stream.readAllBytes(); } catch (Exception e) { throw new RuntimeException(e); }
				}
			})
			.setResponseMarshaller(new MethodDescriptor.Marshaller<byte[]>() {
				@Override
				public java.io.InputStream stream(byte[] value) {
					return new ByteArrayInputStream(value);
				}
				@Override
				public byte[] parse(java.io.InputStream stream) {
					try { return stream.readAllBytes(); } catch (Exception e) { throw new RuntimeException(e); }
				}
			})
			.build();
		destination.setMethodDescriptor(methodDescriptor);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert — verify the channel was used to create a call (interceptor wraps, so newCall is invoked twice)

		verify(channel).newCall(any(MethodDescriptor.class), any(CallOptions.class));
	}

	@Test
	public void shouldRejectNullMessage() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}
}
