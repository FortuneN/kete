package io.github.fortunen.kete.destinations.redisstream;

import java.util.Map;
import java.util.Set;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.lettuce.core.RedisClient;
import io.lettuce.core.XAddArgs;
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
import java.util.LinkedHashMap;

@Data
@NoArgsConstructor(force = true)
@Component(name = "redis-stream")
@EqualsAndHashCode(callSuper = true)
public class RedisStreamDestination extends Destination<RedisStreamDestinationConfig> {

	public static final String FIELD_BODY = "body";

	private String stream;
	private boolean isClusterMode;
	private XAddArgs staticXAddArgs;
	private boolean isStreamTemplated;
	private Set<Map.Entry<String, String>> customHeadersEntrySet;

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

		stream = config.getStream();
		isClusterMode = config.isClusterMode();
		staticXAddArgs = config.getStaticXAddArgs();
		isStreamTemplated = config.isStreamTemplated();
		customHeadersEntrySet = config.getCustomHeadersEntrySet();

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

		var actualStream = isStreamTemplated ? TemplateUtils.substitute(stream, message) : stream;

		var fields = new LinkedHashMap<String, String>();

		for (var entry : customHeadersEntrySet) {
			fields.put(entry.getKey(), entry.getValue());
		}

		fields.put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		fields.put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
		fields.put(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());
		fields.put(FIELD_BODY, new String(message.eventBody(), StandardCharsets.UTF_8));

		if (isClusterMode) {
			clusterCommands.xadd(actualStream, staticXAddArgs, fields);
		} else {
			commands.xadd(actualStream, staticXAddArgs, fields);
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
