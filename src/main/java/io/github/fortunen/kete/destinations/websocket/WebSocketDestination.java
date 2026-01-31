package io.github.fortunen.kete.destinations.websocket;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Component(name = "websocket")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class WebSocketDestination extends Destination<WebSocketDestinationConfig> {

	private WebSocketClient client;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		client = new WebSocketClient(URI.create(config.getUrl()), config.getHeaders()) {

			@Override
			public void onOpen(ServerHandshake handshake) {}

			@Override
			public void onMessage(String message) {}

			@Override
			public void onClose(int code, String reason, boolean remote) {
				log.debug("WebSocket closed: code={}, reason={}, remote={}", code, reason, remote);
			}

			@Override
			public void onError(Exception exception) {
				log.warn("WebSocket error", exception);
			}
		};

		if (config.getSocketFactory() != null) {
			client.setSocketFactory(config.getSocketFactory());
		}

		// connectionLostTimeout enables ping/pong heartbeat (0 = disabled)
		client.setConnectionLostTimeout(config.getConnectionLostTimeout());

		var connected = client.connectBlocking(config.getConnectionTimeout(), config.getConnectionTimeoutUnit());

		ValidationUtils.requireTrue(connected, "failed to connect to WebSocket server at " + config.getUrl());
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		if (config.isBinaryMode()) {
			client.send(message.eventBody());
		} else {
			client.send(new String(message.eventBody(), StandardCharsets.UTF_8));
		}
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(client, "client");
	}
}
