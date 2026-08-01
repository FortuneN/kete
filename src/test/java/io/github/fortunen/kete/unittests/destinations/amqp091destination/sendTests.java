package io.github.fortunen.kete.unittests.destinations.amqp091destination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.amqp091.Amqp091Destination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class sendTests {

	private static Amqp091Destination destination;

	@BeforeAll
	static void setUp() {
		destination = new Amqp091Destination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldPublishToExchangeWithRoutingKey() throws Exception {

		// arrange

		var channel = mock(Channel.class);
		destination.setChannel(channel);
		destination.setExchange("test-exchange");
		destination.setRoutingKey("test-key");
		destination.setDeliveryMode(2);
		destination.setExchangeTemplated(false);
		destination.setRoutingKeyTemplated(false);
		destination.setHasPriority(false);
		destination.setTimeToLiveExpiration(null);
		destination.setStaticHeaders(new HashMap<>());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(channel).basicPublish(eq("test-exchange"), eq("test-key"), any(AMQP.BasicProperties.class), eq(body));
	}

	@Test
	public void shouldPublishWithTemplatedExchange() throws Exception {

		// arrange

		var channel = mock(Channel.class);
		destination.setChannel(channel);
		destination.setExchange("exchange-${realmLowerCase}");
		destination.setRoutingKey("test-key");
		destination.setDeliveryMode(2);
		destination.setExchangeTemplated(true);
		destination.setRoutingKeyTemplated(false);
		destination.setHasPriority(false);
		destination.setTimeToLiveExpiration(null);
		destination.setStaticHeaders(new HashMap<>());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("my-realm", "evt-002", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(channel).basicPublish(eq("exchange-my-realm"), eq("test-key"), any(AMQP.BasicProperties.class), eq(body));
	}

	@Test
	public void shouldPublishWithTemplatedRoutingKey() throws Exception {

		// arrange

		var channel = mock(Channel.class);
		destination.setChannel(channel);
		destination.setExchange("test-exchange");
		destination.setRoutingKey("events.${eventTypeLowerCase}");
		destination.setDeliveryMode(2);
		destination.setExchangeTemplated(false);
		destination.setRoutingKeyTemplated(true);
		destination.setHasPriority(false);
		destination.setTimeToLiveExpiration(null);
		destination.setStaticHeaders(new HashMap<>());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-003", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(channel).basicPublish(eq("test-exchange"), eq("events.login"), any(AMQP.BasicProperties.class), eq(body));
	}

	@Test
	public void shouldThrowWhenMessageIsNull() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("message is required");
	}

	@Test
	public void shouldWaitForConfirmsWhenPublisherConfirmsEnabled() throws Exception {

		// arrange

		var channel = mock(Channel.class);
		destination.setChannel(channel);
		destination.setExchange("test-exchange");
		destination.setRoutingKey("test-key");
		destination.setDeliveryMode(2);
		destination.setExchangeTemplated(false);
		destination.setRoutingKeyTemplated(false);
		destination.setHasPriority(false);
		destination.setTimeToLiveExpiration(null);
		destination.setStaticHeaders(new HashMap<>());
		destination.setPublisherConfirms(true);
		destination.setConfirmTimeoutMillis(30000L);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-004", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(channel).basicPublish(eq("test-exchange"), eq("test-key"), any(AMQP.BasicProperties.class), eq(body));
		verify(channel).waitForConfirmsOrDie(30000L);
	}

	@Test
	public void shouldNotWaitForConfirmsWhenPublisherConfirmsDisabled() throws Exception {

		// arrange

		var channel = mock(Channel.class);
		destination.setChannel(channel);
		destination.setExchange("test-exchange");
		destination.setRoutingKey("test-key");
		destination.setDeliveryMode(2);
		destination.setExchangeTemplated(false);
		destination.setRoutingKeyTemplated(false);
		destination.setHasPriority(false);
		destination.setTimeToLiveExpiration(null);
		destination.setStaticHeaders(new HashMap<>());
		destination.setPublisherConfirms(false);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-005", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(channel).basicPublish(eq("test-exchange"), eq("test-key"), any(AMQP.BasicProperties.class), eq(body));
		verify(channel, never()).waitForConfirmsOrDie(anyLong());
	}
}
