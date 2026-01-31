package io.github.fortunen.kete.integrationtests.amqp091destination;

import static org.assertj.core.api.Assertions.assertThat;

import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import io.github.fortunen.kete.TlsMaterial;

public class sendTests extends TestBase {

	@Test
	public void shouldSendMessage_NonTls() throws Exception {

		// arrange

		startRabbitMq();
		var exchangeName = "test-exchange";
		var routingKey = "test-key";
		var queueName = "test-queue";
		var map = new HashMap<String, Object>();
		map.put("host", container.getHost());
		map.put("port", String.valueOf(container.getAmqpPort()));
		map.put("exchange", exchangeName);
		map.put("routing-key", routingKey);
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var receivedMessage = new AtomicReference<byte[]>();

		// Set up consumer using RabbitMQ client
		var factory = new ConnectionFactory();
		factory.setHost(container.getHost());
		factory.setPort(container.getAmqpPort());

		try (var connection = factory.newConnection();
			 var channel = connection.createChannel()) {

			// Declare exchange, queue, and bind them
			channel.exchangeDeclare(exchangeName, "direct", false, true, null);
			channel.queueDeclare(queueName, false, false, true, null);
			channel.queueBind(queueName, exchangeName, routingKey);

			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				receivedMessage.set(delivery.getBody());
			};
			channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});

			var message = createMessage(
				"test-event-id",
				"test-realm",
				false,
				"LOGIN",
				"application/json",
				"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8),
				null,
				null
			);

			// act

			destination.send(message);

			// assert

			await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).until(() -> receivedMessage.get() != null);
			assertThat(new String(receivedMessage.get(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
		}
	}

	@Test
	@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "TLS container startup too slow on GitHub runners")
	public void shouldSendMessage_Tls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withWriteFiles(true)
			.withTrustStorePassword("changeit")
			.withKeyStorePassword("changeit")
			.withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startWithServerOnlyTLS(tls);
		var exchangeName = "test-exchange";
		var routingKey = "test-key";
		var queueName = "test-queue";
		var map = new HashMap<String, Object>();
		map.put("host", container.getHost());
		map.put("port", String.valueOf(getAmqpTlsPort()));
		map.put("exchange", exchangeName);
		map.put("routing-key", routingKey);
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var receivedMessage = new AtomicReference<byte[]>();

		// Set up consumer using RabbitMQ client with TLS
		var factory = new ConnectionFactory();
		factory.setHost(container.getHost());
		factory.setPort(getAmqpTlsPort());
		factory.useSslProtocol(tls.getKeyStoreAndTrustStoreSSLContext());

		try (var connection = factory.newConnection();
			 var channel = connection.createChannel()) {

			// Declare exchange, queue, and bind them
			channel.exchangeDeclare(exchangeName, "direct", false, true, null);
			channel.queueDeclare(queueName, false, false, true, null);
			channel.queueBind(queueName, exchangeName, routingKey);

			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				receivedMessage.set(delivery.getBody());
			};
			channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});

			var message = createMessage(
				"test-event-id",
				"test-realm",
				false,
				"LOGIN",
				"application/json",
				"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8),
				null,
				null
			);

			// act

			destination.send(message);

			// assert

			await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).until(() -> receivedMessage.get() != null);
			assertThat(new String(receivedMessage.get(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
		}
	}

	@Test
	@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "TLS container startup too slow on GitHub runners")
	public void shouldSendMessage_mTls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withWriteFiles(true)
			.withTrustStorePassword("changeit")
			.withKeyStorePassword("changeit")
			.withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startWithClientAndServerTLS(tls);
		var exchangeName = "test-exchange";
		var routingKey = "test-key";
		var queueName = "test-queue";
		var map = new HashMap<String, Object>();
		map.put("host", container.getHost());
		map.put("port", String.valueOf(getAmqpTlsPort()));
		map.put("exchange", exchangeName);
		map.put("routing-key", routingKey);
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		map.put("tls.key-store.loader.kind", "jks-file-path");
		map.put("tls.key-store.loader.path", tls.getKeyStoreFilePath());
		map.put("tls.key-store.password", tls.getKeyStorePassword());
		map.put("tls.key-store.key-password", tls.getKeyPassword());
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var receivedMessage = new AtomicReference<byte[]>();

		// Set up consumer using RabbitMQ client with TLS
		var factory = new ConnectionFactory();
		factory.setHost(container.getHost());
		factory.setPort(getAmqpTlsPort());
		factory.useSslProtocol(tls.getKeyStoreAndTrustStoreSSLContext());

		try (var connection = factory.newConnection();
			 var channel = connection.createChannel()) {

			// Declare exchange, queue, and bind them
			channel.exchangeDeclare(exchangeName, "direct", false, true, null);
			channel.queueDeclare(queueName, false, false, true, null);
			channel.queueBind(queueName, exchangeName, routingKey);

			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				receivedMessage.set(delivery.getBody());
			};
			channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});

			var message = createMessage(
				"test-event-id",
				"test-realm",
				false,
				"LOGIN",
				"application/json",
				"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8),
				null,
				null
			);

			// act

			destination.send(message);

			// assert

			await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).until(() -> receivedMessage.get() != null);
			assertThat(new String(receivedMessage.get(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
		}
	}
}
