package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import io.github.fortunen.kete.TlsMaterial;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SuppressWarnings("null")
class AzureEventGridDestinationE2ETests extends EndToEndTestBase {

	private static final String NGINX_IMAGE = "nginx:1.27-alpine";
	private static final int NGINX_TLS_PORT = 8443;
	private static final String ACCESS_KEY = "test-access-key-for-e2e";
	private static final String TRUST_STORE_CONTAINER_PATH = "/tmp/truststore.jks";

	private MockWebServer mockServer;
	private GenericContainer<?> nginxProxy;
	private TlsMaterial tls;

	@BeforeEach
	void setUp() throws Exception {
		tls = TlsMaterial.builder()
			.withEnabled(true)
			.withTrustStorePassword("changeit")
			.withKeyStorePassword("changeit")
			.withKeyPassword("changeit")
			.withServerHostNames(new String[] { "eventgrid-proxy", "localhost", "127.0.0.1" })
			.build();

		mockServer = new MockWebServer();
		mockServer.start();
		Testcontainers.exposeHostPorts(mockServer.getPort());
	}

	@AfterEach
	void tearDown() throws Exception {
		if (nginxProxy != null) {
			try { nginxProxy.stop(); } catch (Exception e) { /* ignore */ }
		}
		if (mockServer != null) mockServer.shutdown();
		cleanupNetwork();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldForwardLoginEventToAzureEventGrid() throws Exception {

		// arrange — Event Grid SDK requires HTTPS, so use nginx TLS proxy on Docker network

		for (var i = 0; i < 10; i++) {
			mockServer.enqueue(new MockResponse().setResponseCode(200));
		}

		var nginxConf = createNginxConfig(mockServer.getPort());
		nginxProxy = new GenericContainer<>(DockerImageName.parse(NGINX_IMAGE))
			.withNetwork(createNetwork())
			.withNetworkAliases("eventgrid-proxy")
			.withExposedPorts(NGINX_TLS_PORT)
			.withCopyToContainer(Transferable.of(nginxConf, 0777), "/etc/nginx/nginx.conf")
			.withCopyToContainer(Transferable.of(tls.getServerCertificatePemBytes(), 0777), "/etc/nginx/server.crt")
			.withCopyToContainer(Transferable.of(tls.getServerPrivateKeyPemBytes(), 0777), "/etc/nginx/server.key");
		nginxProxy.start();

		waitForPortReady("127.0.0.1", nginxProxy.getMappedPort(NGINX_TLS_PORT));

		var trustStoreBytes = tls.getTrustStoreBytes();

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.eg-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.eg-test.destination.kind", "azure-eventgrid");
		envVars.put("kete.routes.eg-test.destination.endpoint", "https://eventgrid-proxy:" + NGINX_TLS_PORT);
		envVars.put("kete.routes.eg-test.destination.access-key", ACCESS_KEY);
		envVars.put("kete.routes.eg-test.destination.tls.enabled", "true");
		envVars.put("kete.routes.eg-test.destination.tls.trust-store.loader.kind", "jks-file-path");
		envVars.put("kete.routes.eg-test.destination.tls.trust-store.loader.path", TRUST_STORE_CONTAINER_PATH);
		envVars.put("kete.routes.eg-test.destination.tls.trust-store.password", "changeit");
		envVars.put("kete.routes.eg-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.withCopyToContainer(Transferable.of(trustStoreBytes, 0777), TRUST_STORE_CONTAINER_PATH);
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert — find the POST request among connection test GETs

				okhttp3.mockwebserver.RecordedRequest request = null;
				for (var i = 0; i < 10; i++) {
					request = mockServer.takeRequest(30, TimeUnit.SECONDS);
					if (request != null && "POST".equals(request.getMethod())) break;
				}
				assertThat(request).isNotNull();
				assertThat(request.getMethod()).isEqualTo("POST");
				assertThat(request.getHeader("aeg-sas-key")).isEqualTo(ACCESS_KEY);
				var body = request.getBody().readUtf8();
				assertThat(body).satisfiesAnyOf(
					b -> assertThat(b).contains("\"type\""),
					b -> assertThat(b).contains("\"operationType\"")
				);
				assertThat(body).contains(TEST_REALM);

				// cleanup

				cleanupTestRealm(adminClient);
			}
		}
	}

	private String createNginxConfig(int backendPort) {
		return """
			events {
			    worker_connections 1024;
			}

			http {
			    server {
			        listen %d ssl;

			        ssl_certificate /etc/nginx/server.crt;
			        ssl_certificate_key /etc/nginx/server.key;

			        location / {
			            proxy_pass http://host.testcontainers.internal:%d;
			        }
			    }
			}
			""".formatted(NGINX_TLS_PORT, backendPort);
	}
}
