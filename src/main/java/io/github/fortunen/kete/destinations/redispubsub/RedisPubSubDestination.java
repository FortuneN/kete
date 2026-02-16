package io.github.fortunen.kete.destinations.redispubsub;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
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

	private String channel;
	private boolean isClusterMode;
	private boolean isChannelTemplated;

	private RedisClient client;
	private RedisCommands<String, String> commands;
	private StatefulRedisConnection<String, String> connection;

	private RedisClusterClient clusterClient;
	private RedisAdvancedClusterCommands<String, String> clusterCommands;
	private StatefulRedisClusterConnection<String, String> clusterConnection;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		channel = config.getChannel();
		isClusterMode = config.isClusterMode();
		isChannelTemplated = config.isChannelTemplated();

		if (isClusterMode) {
			clusterClient = RedisClusterClient.create(config.getClusterNodeUris());
			clusterClient.setOptions(config.getClusterClientOptions());
			clusterConnection = clusterClient.connect(StringCodec.UTF8);
			clusterCommands = clusterConnection.sync();

			var pong = clusterCommands.ping();
			ValidationUtils.requireTrue("PONG".equalsIgnoreCase(pong), "Redis cluster connection test failed - expected PONG, got: " + pong);
		} else {
			client = RedisClient.create();
			client.setOptions(config.getClientOptions());
			connection = client.connect(StringCodec.UTF8, config.getRedisUri());
			commands = connection.sync();

			var pong = commands.ping();
			ValidationUtils.requireTrue("PONG".equalsIgnoreCase(pong), "Redis connection test failed - expected PONG, got: " + pong);
		}
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		var actualChannel = isChannelTemplated ? TemplateUtils.substitute(channel, message) : channel;
		var body = new String(message.eventBody(), StandardCharsets.UTF_8);

		if (isClusterMode) {
			clusterCommands.publish(actualChannel, body);
		} else {
			commands.publish(actualChannel, body);
		}
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(clusterConnection, "clusterConnection");
		ValidationUtils.tryClose(clusterClient, "clusterClient");
		ValidationUtils.tryClose(connection, "connection");
		ValidationUtils.tryClose(client, "client");
	}
}
