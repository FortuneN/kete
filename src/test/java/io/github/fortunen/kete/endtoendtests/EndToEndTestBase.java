package io.github.fortunen.kete.endtoendtests;

import java.io.File;
import java.net.Socket;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.kafka.KafkaContainer;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.ws.rs.core.Response;

import static org.awaitility.Awaitility.await;

@Slf4j
@SuppressWarnings("resource")
public abstract class EndToEndTestBase {

	private static final File SHADED_JAR_FILE = new File("target/kete.jar");

	private static boolean SHADED_JAR_BUILT = false;

	static {
		ensureShadedJarBuilt();
	}

	protected static final String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:26.0.0";
	protected static final String TEST_REALM = "test-realm";
	protected static final String TEST_USER = "test-user";
	protected static final String TEST_PASSWORD = "test-password";
	protected static final Duration CONTAINER_STARTUP_TIMEOUT = Duration.ofMinutes(10);

	protected Network network;

	private static void ensureShadedJarBuilt() {

		if (SHADED_JAR_BUILT) {
			return;
		}

		log.info("Building shaded JAR with 'mvn package -DskipTests'...");

		if (buildWithMavenInvoker() && SHADED_JAR_FILE.exists()) {
			SHADED_JAR_BUILT = true;
			log.info("Shaded JAR built successfully at {}", SHADED_JAR_FILE.getAbsolutePath());
			return;
		}

		log.info("Maven Invoker failed, falling back to CLI...");

		if (buildWithCli() && SHADED_JAR_FILE.exists()) {
			SHADED_JAR_BUILT = true;
			log.info("Shaded JAR built successfully at {}", SHADED_JAR_FILE.getAbsolutePath());
			return;
		}

		throw new IllegalStateException("Failed to build shaded JAR at " + SHADED_JAR_FILE.getAbsolutePath() + ". E2E tests cannot run without it.");
	}

	private static boolean buildWithMavenInvoker() {
		try {

			var request = new org.apache.maven.shared.invoker.DefaultInvocationRequest();

			var pomFile = new File("pom.xml").getAbsoluteFile();

			if (!pomFile.isFile()) {
				log.warn("pom.xml not found at {}", pomFile.getAbsolutePath());
				return false;
			}

			request.setPomFile(pomFile);
			request.setGoals(java.util.List.of("package"));
			request.addArg("-DskipTests");
			request.setBatchMode(true);

			request.setBaseDirectory(pomFile.getParentFile());

			request.setTimeoutInSeconds(60 * 10);

			var invoker = new org.apache.maven.shared.invoker.DefaultInvoker();

			invoker.setOutputHandler(line -> log.info("[mvn] {}", line));
			invoker.setErrorHandler(line -> log.warn("[mvn] {}", line));

			var mavenHome = System.getenv("MAVEN_HOME");

			if (mavenHome == null) {
				mavenHome = System.getenv("MVN_HOME");
			}

			if (mavenHome == null) {
				mavenHome = System.getenv("M2_HOME");
			}

			if (mavenHome != null) {
				invoker.setMavenHome(new File(mavenHome));
			} else {
				invoker.setMavenExecutable(new File("mvn"));
			}

			var result = invoker.execute(request);

			if (result.getExitCode() == 0) {

				log.info("Shaded JAR built successfully via Maven Invoker at {}", SHADED_JAR_FILE.getAbsolutePath());
				return true;

			} else {

				log.warn("Maven Invoker build failed with exit code {}", result.getExitCode());

				if (result.getExecutionException() != null) {
					log.warn("Execution exception:", result.getExecutionException());
				}

				return false;
			}

		} catch (NoSuchMethodError e) {

			log.warn("Maven Invoker API incompatibility detected: {}", e.getMessage());
			return false;

		} catch (Exception e) {

			log.warn("Maven Invoker failed:", e);
			return false;

		}
	}


	private static boolean buildWithCli() {
		try {

			var mvnCommand = System.getProperty("os.name").toLowerCase().contains("win") ? "mvn.cmd" : "mvn";
			var processBuilder = new ProcessBuilder(mvnCommand, "package", "-DskipTests").directory(new File(".").getAbsoluteFile()).inheritIO();
			var process = processBuilder.start();
			var exitCode = process.waitFor();

			if (exitCode != 0) {

				log.error("Maven CLI build failed with exit code {}", exitCode);
				return false;

			}

			if (SHADED_JAR_FILE.exists()) {

				log.info("Shaded JAR built successfully via CLI at {}", SHADED_JAR_FILE.getAbsolutePath());
				return true;

			} else {

				log.error("Maven CLI build completed but shaded JAR still not found at {}", SHADED_JAR_FILE.getAbsolutePath());
				return false;

			}

		} catch (Exception e) {

			log.error("Failed to build shaded JAR via CLI: {}", e.getMessage(), e);
			return false;

		}
	}

	protected Network createNetwork() {

		if (network == null) {
			network = Network.newNetwork();
		}

		return network;
	}

	protected KeycloakContainer createKeycloakContainer(Map<String, String> envVars) {

		var keycloak = new KeycloakContainer(KEYCLOAK_IMAGE)
			.withNetwork(createNetwork())
			.withNetworkAliases("keycloak")
			.withAdminUsername("admin")
			.withAdminPassword("admin")
			.withProviderLibsFrom(List.of(SHADED_JAR_FILE))
			.withStartupTimeout(CONTAINER_STARTUP_TIMEOUT)
			.withLogConsumer(new Slf4jLogConsumer(log));

		envVars.forEach(keycloak::withEnv);

		return keycloak;
	}

	protected KeycloakContainer createKeycloakContainerWithMetrics(Map<String, String> envVars) {

		var keycloak = new KeycloakContainer(KEYCLOAK_IMAGE)
			.withNetwork(createNetwork())
			.withNetworkAliases("keycloak")
			.withAdminUsername("admin")
			.withAdminPassword("admin")
			.withProviderLibsFrom(List.of(SHADED_JAR_FILE))
			.withStartupTimeout(CONTAINER_STARTUP_TIMEOUT)
			.withLogConsumer(new Slf4jLogConsumer(log))
			.withEnabledMetrics();

		envVars.forEach(keycloak::withEnv);

		return keycloak;
	}

	protected void createTestRealm(Keycloak adminClient) {

		var realm = new RealmRepresentation();

		realm.setRealm(TEST_REALM);
		realm.setEnabled(true);
		realm.setEventsEnabled(true);
		realm.setAdminEventsEnabled(true);
		realm.setEventsListeners(java.util.List.of("kete"));

		adminClient.realms().create(realm);

		var user = new UserRepresentation();

		user.setUsername(TEST_USER);
		user.setEnabled(true);
		user.setEmail(TEST_USER + "@test.com");
		user.setFirstName("Test");
		user.setLastName("User");

		var credential = new CredentialRepresentation();

		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue(TEST_PASSWORD);
		credential.setTemporary(false);

		user.setCredentials(java.util.List.of(credential));

		RealmResource realmResource = adminClient.realm(TEST_REALM);

		try (Response response = realmResource.users().create(user)) {
			if (response.getStatus() != 201) {
				throw new RuntimeException("Failed to create user: " + response.getStatus());
			}
		}
	}

	protected void triggerLoginEvent(KeycloakContainer keycloak) {

		try (var userClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), TEST_REALM, TEST_USER, TEST_PASSWORD, "admin-cli")) {
			userClient.tokenManager().getAccessToken();
		}
	}

	protected void triggerAdminEvent(Keycloak adminClient) {

		var realm = adminClient.realm(TEST_REALM);
		var tempUser = new UserRepresentation();

		tempUser.setUsername("temp-user-" + UUID.randomUUID());
		tempUser.setEnabled(true);

		String userId;

		try (Response response = realm.users().create(tempUser)) {

			if (response.getStatus() != 201) {
				throw new RuntimeException("Failed to create temp user: " + response.getStatus());
			}

			var location = response.getHeaderString("Location");

			userId = location.substring(location.lastIndexOf('/') + 1);
		}

		realm.users().delete(userId);
	}

	protected void cleanupTestRealm(Keycloak adminClient) {

		try {
			adminClient.realm(TEST_REALM).remove();
		} catch (Exception e) {
			// ignore
		}
	}

	protected void cleanupNetwork() {

		if (network != null) {

			try {
				network.close();
			} catch (Exception e) {
				// ignore
			}

			network = null;
		}
	}

	protected void waitForPortReady(String host, int port) throws Exception {
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try (Socket socket = new Socket(host, port)) {
				log.debug("Port {}:{} is ready", host, port);
				return true;
			} catch (Exception e) {
				return false;
			}
		});
	}

	protected void waitForMqttReady(GenericContainer<?> container, int port) throws Exception {
		waitForPortReady(container.getHost(), container.getMappedPort(port));
	}

	protected void waitForKafkaReady(KafkaContainer kafka) throws Exception {

		var props = new java.util.Properties();

		props.put("bootstrap.servers", kafka.getBootstrapServers());
		props.put("request.timeout.ms", "5000");
		props.put("default.api.timeout.ms", "5000");

		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try (var adminClient = org.apache.kafka.clients.admin.AdminClient.create(props)) {
				adminClient.listTopics().names().get(5, TimeUnit.SECONDS);
				log.debug("Kafka broker is ready at {}", kafka.getBootstrapServers());
				return true;
			} catch (Exception e) {
				return false;
			}
		});
	}

	protected void waitForAmqpReady(GenericContainer<?> container, int port) throws Exception {
		waitForPortReady(container.getHost(), container.getMappedPort(port));
	}

	protected void waitForRabbitMqReady(com.rabbitmq.client.ConnectionFactory factory) throws Exception {
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try (var connection = factory.newConnection()) {
				log.debug("RabbitMQ is ready at {}:{}", factory.getHost(), factory.getPort());
				return true;
			} catch (Exception e) {
				return false;
			}
		});
	}
}
