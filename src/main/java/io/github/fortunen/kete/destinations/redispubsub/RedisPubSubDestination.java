package io.github.fortunen.kete.destinations.redispubsub;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.StringCodec;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;

@Data
@NoArgsConstructor(force = true)
@Component(name = "redis-pubsub")
@EqualsAndHashCode(callSuper = true)
public class RedisPubSubDestination extends Destination<RedisPubSubDestinationConfig> {

	private RedisClient client;
	private RedisCommands<String, String> commands;
	private StatefulRedisConnection<String, String> connection;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		// client

		client = RedisClient.create();
		client.setOptions(config.getClientOptions());

		// connection

		connection = client.connect(StringCodec.UTF8, config.getRedisUri());
		commands = connection.sync();

		// ping

		var pong = commands.ping();

		ValidationUtils.requireTrue("PONG".equalsIgnoreCase(pong), "Redis connection test failed - expected PONG, got: " + pong);
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// actualChannel

		var actualChannel = TemplateUtils.substitute(config.getChannel(), message);

		// publish

		commands.publish(actualChannel, new String(message.eventBody(), StandardCharsets.UTF_8));
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(connection, "connection");
		ValidationUtils.tryClose(client, "client");
	}
}
