package io.github.fortunen.kete.destinations.redisstream;

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

		// actualStream

		var actualStream = TemplateUtils.substitute(config.getStream(), message);

		// fields

		var fields = new LinkedHashMap<String, String>();

		for (var entry : config.getCustomHeadersEntrySet()) {
			fields.put(entry.getKey(), entry.getValue());
		}

		fields.put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		fields.put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
		fields.put(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());
		fields.put(FIELD_BODY, new String(message.eventBody(), StandardCharsets.UTF_8));

		// xadd

		var xaddArgs = new XAddArgs();

		// maxlen

		if (config.getMaxLen() > 0) {
			if (config.isApproximateTrimming()) {
				xaddArgs.maxlen(config.getMaxLen()).approximateTrimming();
			} else {
				xaddArgs.maxlen(config.getMaxLen());
			}
		}

		commands.xadd(actualStream, xaddArgs, fields);
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(connection, "connection");
		ValidationUtils.tryClose(client, "client");
	}
}
