package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.Testcontainers;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

class ZeroMQDestinationE2ETests extends EndToEndTestBase {

	private ZContext zmqContext;
	private ZMQ.Socket pullSocket;
	private int zmqPort;

	@BeforeEach
	void setUp() throws Exception {

		zmqContext = new ZContext(1);
		zmqPort = findAvailablePort();

		// Create a PULL socket that binds on the host — Keycloak will PUSH/CONNECT to it

		pullSocket = zmqContext.createSocket(SocketType.PULL);
		pullSocket.setReceiveTimeOut(10_000);
		pullSocket.bind("tcp://*:" + zmqPort);

		// Expose the port to Docker containers

		Testcontainers.exposeHostPorts(zmqPort);
	}

	@AfterEach
	void tearDown() {
		if (pullSocket != null) {
			try {
				pullSocket.close();
			} catch (Exception e) {
				// ignore
			}
		}
		if (zmqContext != null) {
			try {
				zmqContext.close();
			} catch (Exception e) {
				// ignore
			}
		}
		cleanupNetwork();
	}

	@Test
	void shouldForwardLoginEventToZeroMQ_PushConnect() throws Exception {

		// arrange

		var envVars = new HashMap<String, String>();
		envVars.put("kete.enabled", "true");
		envVars.put("kete.routes.zeromq-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.zeromq-test.destination.kind", "zeromq");
		envVars.put("kete.routes.zeromq-test.destination.endpoint", "tcp://host.testcontainers.internal:" + zmqPort);
		envVars.put("kete.routes.zeromq-test.destination.socket-type", "PUSH");
		envVars.put("kete.routes.zeromq-test.destination.connection-mode", "CONNECT");
		envVars.put("kete.routes.zeromq-test.serializer.kind", "properties");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert — wait for message from Keycloak via ZeroMQ

				var receivedMessage = new AtomicReference<byte[]>();

				await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
					var data = pullSocket.recv(ZMQ.NOBLOCK);
					if (data != null) {
						receivedMessage.set(data);
						return true;
					}
					return false;
				});

				assertThat(receivedMessage.get()).isNotNull();

				var body = new String(receivedMessage.get());

				// Properties serializer assertions — check for key=value format

				assertThat(body).satisfiesAnyOf(
					b -> assertThat(b).contains("type="),
					b -> assertThat(b).contains("operationType=")
				);
				assertThat(body).satisfiesAnyOf(
					b -> assertThat(b).contains("realmName="),
					b -> assertThat(b).contains("realmId=")
				);
				assertThat(body).contains(TEST_REALM);

				// cleanup

				cleanupTestRealm(adminClient);
			}
		}
	}

	private int findAvailablePort() throws IOException {
		try (var serverSocket = new ServerSocket(0)) {
			return serverSocket.getLocalPort();
		}
	}
}
