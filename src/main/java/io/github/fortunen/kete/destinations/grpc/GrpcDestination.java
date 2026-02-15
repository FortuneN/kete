package io.github.fortunen.kete.destinations.grpc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ForwardingClientCall;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@Component(name = "grpc")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class GrpcDestination extends Destination<GrpcDestinationConfig> {

	private static final MethodDescriptor.Marshaller<byte[]> IDENTITY_MARSHALLER = new MethodDescriptor.Marshaller<>() {

		@Override
		public InputStream stream(byte[] value) {
			return new ByteArrayInputStream(value);
		}

		@Override
		public byte[] parse(InputStream stream) {
			try {
				return stream.readAllBytes();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	};

	private Duration timeout;
	private ManagedChannel channel;
	private MethodDescriptor<byte[], byte[]> methodDescriptor;
	private Set<Map.Entry<String, String>> customHeadersEntrySet;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		timeout = config.getTimeout();
		customHeadersEntrySet = config.getCustomHeadersEntrySet();

		// method descriptor

		methodDescriptor = MethodDescriptor.<byte[], byte[]>newBuilder()
			.setType(MethodDescriptor.MethodType.UNARY)
			.setFullMethodName(config.getFullMethodName())
			.setRequestMarshaller(IDENTITY_MARSHALLER)
			.setResponseMarshaller(IDENTITY_MARSHALLER)
			.build();

		// channel

		channel = config.getChannelBuilder().build();

		// verify connection

		var verifyOptions = CallOptions.DEFAULT.withDeadlineAfter(timeout.toSeconds(), TimeUnit.SECONDS);

		try {
			ClientCalls.blockingUnaryCall(channel, methodDescriptor, verifyOptions, new byte[0]);
		} catch (StatusRuntimeException e) {
			var code = e.getStatus().getCode();
			if (code == Status.Code.UNAVAILABLE || code == Status.Code.DEADLINE_EXCEEDED) {
				throw e;
			}
		}
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// payload

		var payload = encodePayload(message.eventBody());

		// metadata

		var metadata = new Metadata();

		for (var entry : customHeadersEntrySet) {
			metadata.put(Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER), entry.getValue());
		}

		metadata.put(Metadata.Key.of(Constants.MESSAGE_HEADER_EVENT_KIND, Metadata.ASCII_STRING_MARSHALLER), message.kind());
		metadata.put(Metadata.Key.of(Constants.MESSAGE_HEADER_EVENT_TYPE, Metadata.ASCII_STRING_MARSHALLER), message.eventType());
		metadata.put(Metadata.Key.of(Constants.MESSAGE_HEADER_CONTENT_TYPE, Metadata.ASCII_STRING_MARSHALLER), message.contentType());

		// intercepted channel with metadata

		var headerInterceptor = new ClientInterceptor() {
			@Override
			public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
				return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
					@Override
					public void start(Listener<RespT> responseListener, Metadata headers) {
						headers.merge(metadata);
						super.start(responseListener, headers);
					}
				};
			}
		};

		var interceptedChannel = ClientInterceptors.intercept(channel, headerInterceptor);

		// call options

		var callOptions = CallOptions.DEFAULT.withDeadlineAfter(timeout.toSeconds(), TimeUnit.SECONDS);

		// send

		ClientCalls.blockingUnaryCall(interceptedChannel, methodDescriptor, callOptions, payload);
	}

	@Override
	@SneakyThrows
	public void close() {
		if (channel != null) {
			channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
		}
	}
}
