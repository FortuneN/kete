package io.github.fortunen.kete.unittests.destinations.kafkadestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.kafka.KafkaDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

@SuppressWarnings("unchecked")
public class sendTests {

	private static KafkaDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new KafkaDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldSendProducerRecord() throws Exception {

		// arrange

		var producer = mock(KafkaProducer.class);
		var future = mock(Future.class);
		when(future.get()).thenReturn(mock(RecordMetadata.class));
		when(producer.send(any(ProducerRecord.class))).thenReturn(future);

		destination.setProducer(producer);
		destination.setTopic("test-topic");
		destination.setTopicTemplated(false);
		destination.setCustomHeadersBytesEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(producer).send(any(ProducerRecord.class));
	}

	@Test
	public void shouldSendWithTemplatedTopic() throws Exception {

		// arrange

		var producer = mock(KafkaProducer.class);
		var future = mock(Future.class);
		when(future.get()).thenReturn(mock(RecordMetadata.class));
		when(producer.send(any(ProducerRecord.class))).thenReturn(future);

		destination.setProducer(producer);
		destination.setTopic("events-${realmLowerCase}");
		destination.setTopicTemplated(true);
		destination.setCustomHeadersBytesEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("my-realm", "evt-002", body, "LOGOUT", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(producer).send(any(ProducerRecord.class));
	}

	@Test
	public void shouldSendWithCustomHeaders() throws Exception {

		// arrange

		var producer = mock(KafkaProducer.class);
		var future = mock(Future.class);
		when(future.get()).thenReturn(mock(RecordMetadata.class));
		when(producer.send(any(ProducerRecord.class))).thenReturn(future);

		destination.setProducer(producer);
		destination.setTopic("test-topic");
		destination.setTopicTemplated(false);
		destination.setCustomHeadersBytesEntrySet(Set.of(Map.entry("x-custom", "value".getBytes(StandardCharsets.UTF_8))));

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-003", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(producer).send(any(ProducerRecord.class));
	}

	@Test
	public void shouldThrowWhenMessageIsNull() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("message is required");
	}
}
