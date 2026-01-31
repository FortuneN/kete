package io.github.fortunen.kete.integrationtests.stompdestination;

import static org.awaitility.Awaitility.await;

import java.io.StringWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLSocketFactory;

import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.activemq.transport.stomp.StompFrame;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.stomp.StompDestination;
import io.github.fortunen.kete.destinations.stomp.StompDestinationConfig;

@SuppressWarnings("resource")
public class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];
	protected static final int STOMP_PORT = 61613;
	protected static final int STOMPS_PORT = 61614;

	protected GenericContainer<?> container;
	protected StompDestination destination;
	protected StompDestinationConfig config;

	@BeforeEach
	void setUp() {
		destination = new StompDestination();
		config = new StompDestinationConfig();
	}

	@AfterEach
	void tearDown() throws Exception {
		cleanUpContainer();
		if (destination != null) {
			try {
				destination.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	protected void configureDestination(MapConfiguration mapConfig) {
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected GenericContainer<?> startActiveMq() throws Exception {

		cleanUpContainer();

		container = new GenericContainer<>(DockerImageName.parse("apache/activemq-classic:6.1.6"))
			.withExposedPorts(STOMP_PORT, 61616)
			.waitingFor(Wait.forLogMessage(".*Apache ActiveMQ.*started.*", 1))
			.withStartupTimeout(Duration.ofMinutes(2));

		container.start();

		waitForStompReady();

		return container;
	}

	protected void startWithServerOnlyTLS(TlsMaterial tls) throws Exception {
		startActiveMqWithTls(tls, false);
	}

	protected void startWithClientAndServerTLS(TlsMaterial tls) throws Exception {
		startActiveMqWithTls(tls, true);
	}

	private void startActiveMqWithTls(TlsMaterial tls, boolean requireClientAuth) throws Exception {

		cleanUpContainer();

		if (tls == null) {
			throw new IllegalArgumentException("TLS material cannot be null");
		}

		if (!tls.isEnabled()) {
			throw new IllegalArgumentException("TLS must be enabled");
		}

		// Read keystore and truststore file bytes
		var keyStoreBytes = Files.readAllBytes(Path.of(tls.getServerKeyStoreFilePath()));
		var trustStoreBytes = Files.readAllBytes(Path.of(tls.getTrustStoreFilePath()));

		// Create ActiveMQ XML config with STOMP+SSL connector
		var activeMqXml = createActiveMqXml(tls.getKeyStorePassword(), tls.getTrustStorePassword(), requireClientAuth);

		container = new GenericContainer<>(DockerImageName.parse("apache/activemq-classic:6.1.6"))
			.withExposedPorts(STOMP_PORT, STOMPS_PORT, 61616)
			.withCopyToContainer(Transferable.of(activeMqXml), "/opt/apache-activemq/conf/activemq.xml")
			.withCopyToContainer(Transferable.of(keyStoreBytes), "/opt/apache-activemq/conf/keystore.jks")
			.withCopyToContainer(Transferable.of(trustStoreBytes), "/opt/apache-activemq/conf/truststore.jks")
			.waitingFor(Wait.forLogMessage(".*Apache ActiveMQ.*started.*", 1))
			.withStartupTimeout(Duration.ofMinutes(2));

		container.start();

		waitForStompReady();
		waitForStompsReady();
	}

	private String createActiveMqXml(String keyStorePassword, String trustStorePassword, boolean needClientAuth) {
		return """
			<?xml version="1.0" encoding="UTF-8"?>
			<beans xmlns="http://www.springframework.org/schema/beans"
			       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			       xsi:schemaLocation="http://www.springframework.org/schema/beans
			                           http://www.springframework.org/schema/beans/spring-beans.xsd
			                           http://activemq.apache.org/schema/core
			                           http://activemq.apache.org/schema/core/activemq-core.xsd">

			    <bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
			        <property name="locations">
			            <value>file:${activemq.conf}/credentials.properties</value>
			        </property>
			    </bean>

			    <broker xmlns="http://activemq.apache.org/schema/core" brokerName="localhost" dataDirectory="${activemq.data}">

			        <destinationPolicy>
			            <policyMap>
			                <policyEntries>
			                    <policyEntry topic=">" />
			                    <policyEntry queue=">" />
			                </policyEntries>
			            </policyMap>
			        </destinationPolicy>

			        <managementContext>
			            <managementContext createConnector="false"/>
			        </managementContext>

			        <persistenceAdapter>
			            <kahaDB directory="${activemq.data}/kahadb"/>
			        </persistenceAdapter>

			        <sslContext>
			            <sslContext keyStore="file:${activemq.conf}/keystore.jks"
			                        keyStorePassword="%s"
			                        trustStore="file:${activemq.conf}/truststore.jks"
			                        trustStorePassword="%s"/>
			        </sslContext>

			        <transportConnectors>
			            <transportConnector name="openwire" uri="tcp://0.0.0.0:61616"/>
			            <transportConnector name="stomp" uri="stomp://0.0.0.0:%d"/>
			            <transportConnector name="stomp+ssl" uri="stomp+ssl://0.0.0.0:%d?transport.needClientAuth=%s"/>
			        </transportConnectors>

			    </broker>

			</beans>
			""".formatted(keyStorePassword, trustStorePassword, STOMP_PORT, STOMPS_PORT, needClientAuth);
	}

	protected int getStompsPort() {
		return container.getMappedPort(STOMPS_PORT);
	}

	protected void cleanUpContainer() {
		if (container != null) {
			try {
				container.stop();
			} catch (Exception e) {
				// ignore
			}
			container = null;
		}
	}

	protected int getStompPort() {
		return container.getMappedPort(STOMP_PORT);
	}

	protected void waitForStompReady() {
		await().atMost(Duration.ofSeconds(30))
			.pollInterval(Duration.ofSeconds(1))
			.until(() -> {
				try (var socket = new Socket(container.getHost(), getStompPort())) {
					return socket.isConnected();
				} catch (Exception e) {
					return false;
				}
			});
	}

	protected void waitForStompsReady() {
		await().atMost(Duration.ofSeconds(30))
			.pollInterval(Duration.ofSeconds(1))
			.until(() -> {
				try (var socket = new Socket(container.getHost(), getStompsPort())) {
					return socket.isConnected();
				} catch (Exception e) {
					return false;
				}
			});
	}

	protected EventMessage createMessage(String eventId, String eventType, String contentType, byte[] eventBody) {
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "false", "", "");
	}

	protected StompSubscriber createSubscriber(String destination, int qos) throws Exception {
		return new StompSubscriber(container.getHost(), getStompPort(), destination, null);
	}

	protected StompSubscriber createTlsSubscriber(String destination, TlsMaterial tls) throws Exception {
		var sslFactory = tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory();
		return new StompSubscriber(container.getHost(), getStompsPort(), destination, sslFactory);
	}

	protected static class StompSubscriber implements AutoCloseable {

		private final StompConnection connection;
		private final BlockingQueue<String> receivedMessages = new LinkedBlockingQueue<>();
		private final AtomicBoolean running = new AtomicBoolean(true);
		private final Thread readerThread;

		public StompSubscriber(String host, int port, String destination, SSLSocketFactory sslFactory) throws Exception {

			connection = new StompConnection();

			if (sslFactory != null) {
				connection.open(sslFactory.createSocket(host, port));
			} else {
				connection.open(host, port);
			}

			connection.connect("admin", "admin");

			var headers = new HashMap<String, String>();
			headers.put("id", "sub-0");
			connection.subscribe(destination, "auto", headers);

			readerThread = new Thread(() -> {
				while (running.get()) {
					try {
						StompFrame frame = connection.receive(1000);
						if (frame != null && "MESSAGE".equals(frame.getAction())) {
							receivedMessages.offer(new String(frame.getContent()));
						}
					} catch (Exception e) {
						if (running.get()) {
							// ignore timeout
						}
					}
				}
			});
			readerThread.setDaemon(true);
			readerThread.start();
		}

		public String takeMessage(long timeout, TimeUnit unit) throws InterruptedException {
			return receivedMessages.poll(timeout, unit);
		}

		@Override
		public void close() throws Exception {
			running.set(false);
			try {
				connection.disconnect();
			} catch (Exception e) {
				// ignore
			}
			try {
				connection.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}
}
