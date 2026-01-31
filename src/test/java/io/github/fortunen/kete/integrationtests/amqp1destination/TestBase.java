package io.github.fortunen.kete.integrationtests.amqp1destination;

import static org.awaitility.Awaitility.await;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.amqp1.Amqp1Destination;
import io.github.fortunen.kete.destinations.amqp1.Amqp1DestinationConfig;

@SuppressWarnings("resource")
public abstract class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];
	protected static final int AMQP_PORT = 5672;
	protected static final int AMQPS_PORT = 5671;
	protected static final String DEFAULT_USERNAME = "admin";
	protected static final String DEFAULT_PASSWORD = "admin";

	protected Amqp1Destination destination;
	protected Amqp1DestinationConfig config;
	protected GenericContainer<?> container;

	@BeforeEach
	void setUpTest() {
		destination = new Amqp1Destination();
		config = new Amqp1DestinationConfig();
	}

	protected void configureDestination(MapConfiguration mapConfig) {
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	@AfterEach
	void tearDownTest() {

		if (destination != null) {
			try {
				destination.close();
			} catch (Exception ignored) {
				// ignore
			}
		}

		if (container != null) {

			try {
				container.stop();
			} catch (Exception ignored) {
				// ignore
			}

			container = null;
		}
	}

	protected void startActiveMqArtemis() throws Exception {
		container = new GenericContainer<>(DockerImageName.parse("apache/activemq-artemis:2.40.0-alpine"))
			.withEnv("ARTEMIS_USER", DEFAULT_USERNAME)
			.withEnv("ARTEMIS_PASSWORD", DEFAULT_PASSWORD)
			.withEnv("ANONYMOUS_LOGIN", "true")
			.withExposedPorts(AMQP_PORT, 8161);
		container.start();

		// Wait for AMQP broker to be fully ready to accept connections
		waitForAmqpReady();
	}

	protected void startWithServerOnlyTLS(TlsMaterial tls) throws Exception {
		startActiveMqArtemisWithTls(tls, false);
	}

	protected void startWithClientAndServerTLS(TlsMaterial tls) throws Exception {
		startActiveMqArtemisWithTls(tls, true);
	}

	private void startActiveMqArtemisWithTls(TlsMaterial tls, boolean requireClientAuth) throws Exception {

		if (tls == null) {
			throw new IllegalArgumentException("TLS material cannot be null");
		}

		if (!tls.isEnabled()) {
			throw new IllegalArgumentException("TLS must be enabled (call withEnabled(true) when building TlsMaterial)");
		}

		if (tls.getKeyStoreAndTrustStoreSSLContext() == null) {
			throw new IllegalStateException("SSL context is null - ensure TLS is properly configured");
		}

		if (tls.getKeyStoreFilePath() == null) {
			throw new IllegalStateException("Key store file path is null - ensure TLS material is built with withWriteFiles(true)");
		}

		if (tls.getTrustStoreFilePath() == null) {
			throw new IllegalStateException("Trust store file path is null - ensure TLS material is built with withWriteFiles(true)");
		}

		// Create broker.xml with SSL acceptor configuration
		// needClientAuth controls SERVER-SIDE enforcement:
		//   - false: Server-only TLS - client trusts server but server doesn't require client cert
		//   - true: mTLS - server REQUIRES valid client certificate (enforced by Artemis via needClientAuth=true)
		var brokerXml = createArtemisBrokerXml(
			tls.getKeyStorePassword() != null ? tls.getKeyStorePassword() : "",
			tls.getTrustStorePassword() != null ? tls.getTrustStorePassword() : "",
			requireClientAuth
		);

		// Write broker.xml to temp file for etc-override
		var brokerXmlPath = Files.createTempFile("broker", ".xml");
		Files.writeString(brokerXmlPath, brokerXml);
		brokerXmlPath.toFile().deleteOnExit();

		// Read keystore and truststore file bytes
		// Use serverKeyStoreFilePath for the container (server-side TLS) - it contains only the server key
		var keyStoreBytes = Files.readAllBytes(Path.of(tls.getServerKeyStoreFilePath()));
		var trustStoreBytes = Files.readAllBytes(Path.of(tls.getTrustStoreFilePath()));

		container = new GenericContainer<>(DockerImageName.parse("apache/activemq-artemis:2.40.0-alpine"))
			.withEnv("ARTEMIS_USER", DEFAULT_USERNAME)
			.withEnv("ARTEMIS_PASSWORD", DEFAULT_PASSWORD)
			.withEnv("ANONYMOUS_LOGIN", "true")
			// Copy broker.xml to etc-override directory for SSL configuration
			.withCopyToContainer(Transferable.of(brokerXml), "/var/lib/artemis-instance/etc-override/broker.xml")
			// Copy keystore and truststore files to etc-override - they will be copied to etc after instance creation
			.withCopyToContainer(Transferable.of(keyStoreBytes), "/var/lib/artemis-instance/etc-override/keystore.jks")
			.withCopyToContainer(Transferable.of(trustStoreBytes), "/var/lib/artemis-instance/etc-override/truststore.jks")
			.withExposedPorts(AMQP_PORT, AMQPS_PORT, 8161)
			.withLogConsumer(outputFrame -> System.out.println("[ARTEMIS] " + outputFrame.getUtf8String()));

		container.start();

		// Wait for AMQP broker to be fully ready to accept connections
		waitForAmqpReady();
	}

	private String createArtemisBrokerXml(String keyStorePassword, String trustStorePassword, boolean needClientAuth) {
		return """
			<?xml version='1.0'?>
			<configuration xmlns="urn:activemq" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			               xsi:schemaLocation="urn:activemq /schema/artemis-configuration.xsd">
			   <core xmlns="urn:activemq:core" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			         xsi:schemaLocation="urn:activemq:core ">
			      <name>0.0.0.0</name>
			      <persistence-enabled>false</persistence-enabled>
			      <journal-type>NIO</journal-type>
			      <paging-directory>data/paging</paging-directory>
			      <bindings-directory>data/bindings</bindings-directory>
			      <journal-directory>data/journal</journal-directory>
			      <large-messages-directory>data/large-messages</large-messages-directory>
			      <journal-datasync>true</journal-datasync>
			      <journal-min-files>2</journal-min-files>
			      <journal-pool-files>10</journal-pool-files>
			      <journal-device-block-size>4096</journal-device-block-size>
			      <journal-file-size>10M</journal-file-size>
			      <disk-scan-period>5000</disk-scan-period>
			      <max-disk-usage>90</max-disk-usage>
			      <critical-analyzer>true</critical-analyzer>
			      <critical-analyzer-timeout>120000</critical-analyzer-timeout>
			      <critical-analyzer-check-period>60000</critical-analyzer-check-period>
			      <critical-analyzer-policy>HALT</critical-analyzer-policy>
			      <acceptors>
			         <!-- Plain AMQP acceptor -->
			         <acceptor name="amqp">tcp://0.0.0.0:%d?protocols=AMQP</acceptor>
			         <!-- SSL AMQP acceptor -->
			         <acceptor name="amqps">tcp://0.0.0.0:%d?protocols=AMQP;sslEnabled=true;keyStorePath=/var/lib/artemis-instance/etc/keystore.jks;keyStorePassword=%s;trustStorePath=/var/lib/artemis-instance/etc/truststore.jks;trustStorePassword=%s;needClientAuth=%s</acceptor>
			      </acceptors>
			      <security-settings>
			         <security-setting match="#">
			            <permission type="createNonDurableQueue" roles="amq"/>
			            <permission type="deleteNonDurableQueue" roles="amq"/>
			            <permission type="createDurableQueue" roles="amq"/>
			            <permission type="deleteDurableQueue" roles="amq"/>
			            <permission type="createAddress" roles="amq"/>
			            <permission type="deleteAddress" roles="amq"/>
			            <permission type="consume" roles="amq"/>
			            <permission type="browse" roles="amq"/>
			            <permission type="send" roles="amq"/>
			            <permission type="manage" roles="amq"/>
			         </security-setting>
			      </security-settings>
			      <address-settings>
			         <address-setting match="activemq.management#">
			            <dead-letter-address>DLQ</dead-letter-address>
			            <expiry-address>ExpiryQueue</expiry-address>
			            <redelivery-delay>0</redelivery-delay>
			            <max-size-bytes>-1</max-size-bytes>
			            <message-counter-history-day-limit>10</message-counter-history-day-limit>
			            <address-full-policy>PAGE</address-full-policy>
			            <auto-create-queues>true</auto-create-queues>
			            <auto-create-addresses>true</auto-create-addresses>
			         </address-setting>
			         <address-setting match="#">
			            <dead-letter-address>DLQ</dead-letter-address>
			            <expiry-address>ExpiryQueue</expiry-address>
			            <redelivery-delay>0</redelivery-delay>
			            <max-size-bytes>-1</max-size-bytes>
			            <message-counter-history-day-limit>10</message-counter-history-day-limit>
			            <address-full-policy>PAGE</address-full-policy>
			            <auto-create-queues>true</auto-create-queues>
			            <auto-create-addresses>true</auto-create-addresses>
			         </address-setting>
			      </address-settings>
			      <addresses>
			         <address name="DLQ">
			            <anycast>
			               <queue name="DLQ" />
			            </anycast>
			         </address>
			         <address name="ExpiryQueue">
			            <anycast>
			               <queue name="ExpiryQueue" />
			            </anycast>
			         </address>
			      </addresses>
			   </core>
			</configuration>
			""".formatted(AMQP_PORT, AMQPS_PORT, keyStorePassword, trustStorePassword, needClientAuth);
	}

	protected String getHost() {
		return container.getHost();
	}

	protected int getMappedPort() {
		return container.getMappedPort(AMQP_PORT);
	}

	protected int getAmqpsTlsPort() {
		return container.getMappedPort(AMQPS_PORT);
	}

	private void waitForAmqpReady() throws Exception {
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {

				var factory = new JmsConnectionFactory("amqp://" + getHost() + ":" + getMappedPort());

				try (var connection = factory.createConnection()) {
					connection.start();
					return true;
				}

			} catch (Exception e) {
				return false;
			}
		});
	}

	protected static EventMessage createMessage(
		String eventId,
		String realm,
		boolean isAdminEvent,
		String eventType,
		String contentType,
		byte[] eventBody,
		String resourceType,
		String operationType
	) {
		return new EventMessage(
			realm != null ? realm : "",
			eventId != null ? eventId : "",
			eventBody != null ? eventBody : EMPTY_BYTES,
			eventType != null ? eventType : "",
			contentType != null ? contentType : "",
			resourceType != null ? resourceType : "",
			isAdminEvent ? Constants.ADMIN_EVENT : Constants.EVENT,
			operationType != null ? operationType : "",
			"SUCCESS"
		);
	}
}
