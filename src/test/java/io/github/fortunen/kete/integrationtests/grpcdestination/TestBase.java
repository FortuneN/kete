package io.github.fortunen.kete.integrationtests.grpcdestination;

import static org.awaitility.Awaitility.await;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.grpc.GrpcDestination;
import io.github.fortunen.kete.destinations.grpc.GrpcDestinationConfig;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerServiceDefinition;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.ServerCalls;

public class TestBase {

	private static final String NGINX_IMAGE = "nginx:1.27-alpine";
	private static final int NGINX_TLS_PORT = 8443;

	protected static final String SERVICE_NAME = "EventService";
	protected static final String METHOD_NAME = "SendEvent";

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

	protected Server grpcServer;
	protected GenericContainer<?> nginxProxy;
	protected Network network;
	protected GrpcDestination destination;
	protected GrpcDestinationConfig config;
	protected CopyOnWriteArrayList<byte[]> receivedPayloads;
	protected CopyOnWriteArrayList<Metadata> receivedMetadata;

	@BeforeEach
	void setUp() {
		destination = new GrpcDestination();
		config = new GrpcDestinationConfig();
		receivedPayloads = new CopyOnWriteArrayList<>();
		receivedMetadata = new CopyOnWriteArrayList<>();
	}

	@AfterEach
	void tearDown() {
		if (destination != null) {
			try { destination.close(); } catch (Exception e) { /* ignore */ }
		}
		if (grpcServer != null) {
			try { grpcServer.shutdownNow(); } catch (Exception e) { /* ignore */ }
			grpcServer = null;
		}
		if (nginxProxy != null) {
			try { nginxProxy.stop(); } catch (Exception e) { /* ignore */ }
			nginxProxy = null;
		}
		if (network != null) {
			try { network.close(); } catch (Exception e) { /* ignore */ }
			network = null;
		}
	}

	protected void startGrpcServer() throws Exception {

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
			.intercept(new io.grpc.ServerInterceptor() {
				@Override
				public <ReqT, RespT> io.grpc.ServerCall.Listener<ReqT> interceptCall(
						io.grpc.ServerCall<ReqT, RespT> call,
						Metadata headers,
						io.grpc.ServerCallHandler<ReqT, RespT> next) {
					receivedMetadata.add(headers);
					return next.startCall(call, headers);
				}
			})
			.build()
			.start();
	}

	protected int getGrpcServerPort() {
		return grpcServer.getPort();
	}

	protected void configureDestination() {
		var map = new HashMap<String, Object>();
		map.put("kind", "grpc");
		map.put("host", "127.0.0.1");
		map.put("port", getGrpcServerPort());
		map.put("service", SERVICE_NAME);
		map.put("method", METHOD_NAME);
		map.put("use-plaintext", true);
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	@SuppressWarnings("resource")
	protected void startNginxTlsProxy(TlsMaterial tls, boolean requireClientAuth) {
		network = Network.newNetwork();
		Testcontainers.exposeHostPorts(getGrpcServerPort());

		var nginxConf = createNginxConfig(requireClientAuth);
		nginxProxy = new GenericContainer<>(DockerImageName.parse(NGINX_IMAGE))
			.withNetwork(network)
			.withExposedPorts(NGINX_TLS_PORT)
			.withCopyToContainer(Transferable.of(nginxConf, 0777), "/etc/nginx/nginx.conf")
			.withCopyToContainer(Transferable.of(tls.getServerCertificatePemBytes(), 0777), "/etc/nginx/server.crt")
			.withCopyToContainer(Transferable.of(tls.getServerPrivateKeyPemBytes(), 0777), "/etc/nginx/server.key")
			.withCopyToContainer(Transferable.of(tls.getCaCertificatePemBytes(), 0777), "/etc/nginx/ca.crt");
		nginxProxy.start();

		waitForNginxReady();
	}

	protected String getNginxHost() {
		return "127.0.0.1";
	}

	protected int getNginxPort() {
		return nginxProxy.getMappedPort(NGINX_TLS_PORT);
	}

	protected void configureDestinationWithTls(TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "grpc");
		map.put("host", getNginxHost());
		map.put("port", getNginxPort());
		map.put("service", SERVICE_NAME);
		map.put("method", METHOD_NAME);
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithMtls(TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "grpc");
		map.put("host", getNginxHost());
		map.put("port", getNginxPort());
		map.put("service", SERVICE_NAME);
		map.put("method", METHOD_NAME);
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		map.put("tls.key-store.loader.kind", "jks-file-path");
		map.put("tls.key-store.loader.path", tls.getKeyStoreFilePath());
		map.put("tls.key-store.password", tls.getKeyStorePassword());
		map.put("tls.key-store.key-password", tls.getKeyPassword());
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected EventMessage createMessage(String eventId, String eventType, String contentType, byte[] eventBody) {
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "EVENT", "", "");
	}

	private void waitForNginxReady() {
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try (var socket = new Socket(getNginxHost(), getNginxPort())) {
				return true;
			} catch (Exception e) {
				return false;
			}
		});
	}

	private String createNginxConfig(boolean requireClientAuth) {
		var sslClientCertificate = requireClientAuth
			? """
			        ssl_client_certificate /etc/nginx/ca.crt;
			        ssl_verify_client on;
			"""
			: "";

		return """
			events {
			    worker_connections 1024;
			}

			http {
			    server {
			        listen %d ssl http2;

			        ssl_certificate /etc/nginx/server.crt;
			        ssl_certificate_key /etc/nginx/server.key;
			%s
			        location / {
			            grpc_pass grpc://host.testcontainers.internal:%d;
			        }
			    }
			}
			""".formatted(NGINX_TLS_PORT, sslClientCertificate, getGrpcServerPort());
	}
}
