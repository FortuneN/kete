package io.github.fortunen.kete.integrationtests.awssnsdestination;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange — create topic, SQS queue, subscribe queue to topic (SNS is push-only)

		startLocalStack();
		var topicArn = createTopic("my-topic");
		var queueUrl = createQueue("my-verify-queue");
		var queueArn = getQueueArn(queueUrl);
		subscribeQueueToTopic(topicArn, queueArn);
		configureDestination("my-topic");
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — read from the SQS queue subscribed to the SNS topic

		var body = receiveMessageFromQueue("my-verify-queue");
		assertThat(body).isEqualTo("{\"type\":\"LOGIN\"}");
	}

	@Test
	public void shouldSend_Tls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withTrustStorePassword("changeit").withKeyStorePassword("changeit").withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startLocalStackWithTls(tls);
		var topicArn = createTopic("my-topic");
		var queueUrl = createQueue("my-verify-queue");
		var queueArn = getQueueArn(queueUrl);
		subscribeQueueToTopic(topicArn, queueArn);
		configureDestinationWithTls("my-topic", tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — read directly from LocalStack SQS (plain HTTP) to verify

		var body = receiveMessageFromQueue("my-verify-queue");
		assertThat(body).isEqualTo("{\"type\":\"LOGIN\"}");
	}

	@Test
	public void shouldSend_mTls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withTrustStorePassword("changeit").withKeyStorePassword("changeit").withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startLocalStackWithTls(tls);
		var topicArn = createTopic("my-topic");
		var queueUrl = createQueue("my-verify-queue");
		var queueArn = getQueueArn(queueUrl);
		subscribeQueueToTopic(topicArn, queueArn);
		configureDestinationWithMtls("my-topic", tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — read directly from LocalStack SQS (plain HTTP) to verify

		var body = receiveMessageFromQueue("my-verify-queue");
		assertThat(body).isEqualTo("{\"type\":\"LOGIN\"}");
	}
}
