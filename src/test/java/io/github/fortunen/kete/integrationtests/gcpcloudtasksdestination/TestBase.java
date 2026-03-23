package io.github.fortunen.kete.integrationtests.gcpcloudtasksdestination;

import static org.awaitility.Awaitility.await;

import java.net.Socket;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.google.cloud.tasks.v2.CloudTasksGrpc;
import com.google.cloud.tasks.v2.ListQueuesRequest;
import io.grpc.ManagedChannelBuilder;

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
import io.github.fortunen.kete.destinations.gcpcloudtasks.GcpCloudTasksDestination;
import io.github.fortunen.kete.destinations.gcpcloudtasks.GcpCloudTasksDestinationConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class TestBase {

	private static final String EMULATOR_IMAGE = "ghcr.io/aertje/cloud-tasks-emulator:latest";
	private static final String NGINX_IMAGE = "nginx:1.27-alpine";
	private static final int EMULATOR_PORT = 8123;
	private static final int NGINX_TLS_PORT = 8443;

	protected static final String PROJECT = "test-project";
	protected static final String LOCATION = "us-central1";
	protected static final String QUEUE = "test-queue";
	protected static final String QUEUE_PATH = "projects/" + PROJECT + "/locations/" + LOCATION + "/queues/" + QUEUE;

	protected GenericContainer<?> emulator;
	protected GenericContainer<?> nginxProxy;
	protected Network network;
	protected MockWebServer taskReceiver;
	protected GcpCloudTasksDestination destination;
	protected GcpCloudTasksDestinationConfig config;

	@BeforeEach
	void setUp() {
		destination = new GcpCloudTasksDestination();
		config = new GcpCloudTasksDestinationConfig();
	}

	@AfterEach
	void tearDown() {
		if (destination != null) {
			try { destination.close(); } catch (Exception e) { /* ignore */ }
		}
		if (taskReceiver != null) {
			try { taskReceiver.shutdown(); } catch (Exception e) { /* ignore */ }
			taskReceiver = null;
		}
		if (nginxProxy != null) {
			try { nginxProxy.stop(); } catch (Exception e) { /* ignore */ }
			nginxProxy = null;
		}
		if (emulator != null) {
			try { emulator.stop(); } catch (Exception e) { /* ignore */ }
			emulator = null;
		}
		if (network != null) {
			try { network.close(); } catch (Exception e) { /* ignore */ }
			network = null;
		}
	}

	protected void startTaskReceiver() throws Exception {
		taskReceiver = new MockWebServer();
		taskReceiver.start();
		taskReceiver.enqueue(new MockResponse().setResponseCode(200));
		Testcontainers.exposeHostPorts(taskReceiver.getPort());
	}

	@SuppressWarnings("resource")
	protected void startEmulator() {
		emulator = new GenericContainer<>(DockerImageName.parse(EMULATOR_IMAGE))
			.withExposedPorts(EMULATOR_PORT)
			.withCommand(
				"-host", "0.0.0.0",
				"-port", String.valueOf(EMULATOR_PORT),
				"-queue", QUEUE_PATH);
		emulator.start();

		waitForEmulatorReady();
	}

	@SuppressWarnings("resource")
	protected void startEmulatorOnNetwork() {
		network = Network.newNetwork();
		emulator = new GenericContainer<>(DockerImageName.parse(EMULATOR_IMAGE))
			.withNetwork(network)
			.withNetworkAliases("cloud-tasks-emulator")
			.withExposedPorts(EMULATOR_PORT)
			.withCommand(
				"-host", "0.0.0.0",
				"-port", String.valueOf(EMULATOR_PORT),
				"-queue", QUEUE_PATH);
		emulator.start();

		waitForEmulatorReady();
	}

	@SuppressWarnings("resource")
	protected void startNginxTlsProxy(TlsMaterial tls, boolean requireClientAuth) {
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

	protected String getEmulatorEndpoint() {
		return "127.0.0.1:" + emulator.getMappedPort(EMULATOR_PORT);
	}

	protected String getNginxEndpoint() {
		return "127.0.0.1:" + nginxProxy.getMappedPort(NGINX_TLS_PORT);
	}

	protected String getTargetUrl() {
		return "http://host.testcontainers.internal:" + taskReceiver.getPort() + "/handler";
	}

	protected void configureDestination() {
		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-cloud-tasks");
		map.put("project", PROJECT);
		map.put("location", LOCATION);
		map.put("queue", QUEUE);
		map.put("target-url", getTargetUrl());
		map.put("endpoint", getEmulatorEndpoint());
		map.put("use-plaintext", true);
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithTls(TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-cloud-tasks");
		map.put("project", PROJECT);
		map.put("location", LOCATION);
		map.put("queue", QUEUE);
		map.put("target-url", getTargetUrl());
		map.put("endpoint", getNginxEndpoint());
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
		map.put("kind", "gcp-cloud-tasks");
		map.put("project", PROJECT);
		map.put("location", LOCATION);
		map.put("queue", QUEUE);
		map.put("target-url", getTargetUrl());
		map.put("endpoint", getNginxEndpoint());
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

	protected RecordedRequest waitForDispatchedTask() {
		return await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				return taskReceiver.takeRequest(1, TimeUnit.SECONDS);
			} catch (Exception e) {
				return null;
			}
		}, req -> req != null);
	}

	protected EventMessage createMessage(String eventId, String eventType, String contentType, byte[] eventBody) {
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "EVENT", "", "");
	}

	private void waitForEmulatorReady() {
		var endpoint = getEmulatorEndpoint();
		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var channel = ManagedChannelBuilder.forTarget(endpoint).usePlaintext().build();
				try {
					var stub = CloudTasksGrpc.newBlockingStub(channel);
					var request = ListQueuesRequest.newBuilder()
						.setParent("projects/" + PROJECT + "/locations/" + LOCATION)
						.build();
					stub.listQueues(request);
					return true;
				} finally {
					channel.shutdownNow();
				}
			} catch (Exception e) {
				return false;
			}
		});
	}

	private void waitForNginxReady() {
		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try (var socket = new Socket("127.0.0.1", nginxProxy.getMappedPort(NGINX_TLS_PORT))) {
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
			            grpc_pass grpc://cloud-tasks-emulator:%d;
			        }
			    }
			}
			""".formatted(NGINX_TLS_PORT, sslClientCertificate, EMULATOR_PORT);
	}
}
