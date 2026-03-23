package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.Testcontainers;

import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerServiceDefinition;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.ServerCalls;

class GrpcDestinationE2ETests extends EndToEndTestBase {

	private static final String SERVICE_NAME = "EventService";
	private static final String METHOD_NAME = "SendEvent";

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

	private Server grpcServer;
	private final CopyOnWriteArrayList<byte[]> receivedPayloads = new CopyOnWriteArrayList<>();

	@AfterEach
	void tearDown() {
		if (grpcServer != null) {
			grpcServer.shutdownNow();
		}
		cleanupNetwork();
	}

	@Test
	void shouldForwardLoginEventToGrpcServer() throws Exception {

		// arrange — start embedded gRPC server

		var methodDescriptor = MethodDescriptor.<byte[], byte[]>newBuilder()
			.setType(MethodDescriptor.MethodType.UNARY)
			.setFullMethodName(MethodDescriptor.generateFullMethodName(SERVICE_NAME, METHOD_NAME))
			.setRequestMarshaller(IDENTITY_MARSHALLER)
			.setResponseMarshaller(IDENTITY_MARSHALLER)
			.build();

		var handler = ServerCalls.asyncUnaryCall(
			(byte[] request, io.grpc.stub.StreamObserver<byte[]> responseObserver) -> {
				receivedPayloads.add(request);
				responseObserver.onNext(new byte[0]);
				responseObserver.onCompleted();
			}
		);

		var serviceDefinition = ServerServiceDefinition.builder(SERVICE_NAME)
			.addMethod(methodDescriptor, handler)
			.build();

		grpcServer = NettyServerBuilder.forPort(0)
			.addService(serviceDefinition)
			.build()
			.start();

		var grpcPort = grpcServer.getPort();
		Testcontainers.exposeHostPorts(grpcPort);

		// configure keycloak

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.grpc-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.grpc-test.destination.kind", "grpc");
		envVars.put("kete.routes.grpc-test.destination.host", "host.testcontainers.internal");
		envVars.put("kete.routes.grpc-test.destination.port", String.valueOf(grpcPort));
		envVars.put("kete.routes.grpc-test.destination.service", SERVICE_NAME);
		envVars.put("kete.routes.grpc-test.destination.method", METHOD_NAME);
		envVars.put("kete.routes.grpc-test.destination.use-plaintext", "true");
		envVars.put("kete.routes.grpc-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {

			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {

				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert — wait for the gRPC server to receive the event

				await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
					// filter out verify calls (empty bytes)
					return receivedPayloads.stream().anyMatch(p -> p.length > 0);
				});

				var payload = receivedPayloads.stream()
					.filter(p -> p.length > 0)
					.findFirst()
					.orElseThrow();

				var body = new String(payload, StandardCharsets.UTF_8);
				assertThat(body).satisfiesAnyOf(
					b -> assertThat(b).contains("\"type\""),
					b -> assertThat(b).contains("\"operationType\"")
				);
				assertThat(body).satisfiesAnyOf(
					b -> assertThat(b).contains("\"realmName\""),
					b -> assertThat(b).contains("\"realmId\"")
				);
				assertThat(body).contains(TEST_REALM);

				// cleanup

				cleanupTestRealm(adminClient);
			}
		}
	}
}
