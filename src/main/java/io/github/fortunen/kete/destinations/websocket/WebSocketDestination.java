package io.github.fortunen.kete.destinations.websocket;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Component(name = "websocket")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class WebSocketDestination extends Destination<WebSocketDestinationConfig> {

	private ConcurrentHashMap<String, WebSocketClient> clientCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		// verify connection

		try (var socket = new Socket()) {
			socket.connect(new InetSocketAddress(config.getHost(), config.getPort()), config.getConnectionTimeoutSeconds() * 1000);
		}
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// client

		var actualUrl = TemplateUtils.substitute(config.getUrl(), message);

		var client = clientCache.computeIfAbsent(actualUrl, url -> {
			try {
				var wsClient = new WebSocketClient(URI.create(url), config.getCustomHeaders()) {

					@Override
					public void onOpen(ServerHandshake handshake) {}

					@Override
					public void onMessage(String msg) {}

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
					wsClient.setSocketFactory(config.getSocketFactory());
				}

				wsClient.setConnectionLostTimeout(config.getConnectionLostTimeoutSeconds());

				var connected = wsClient.connectBlocking(config.getConnectionTimeoutSeconds(), config.getConnectionTimeoutUnit());

				if (!connected) {
					throw new RuntimeException("failed to connect to WebSocket server at " + url);
				}

				return wsClient;

			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		});

		client.clearHeaders();

		if (config.isOauthEnabled()) {
			client.addHeader("Authorization", config.getOauth().getAccessToken().toAuthorizationHeader());
		}

		for (var entry : config.getCustomHeadersEntrySet()) {
			client.addHeader(entry.getKey(), entry.getValue());
		}

		client.addHeader(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		client.addHeader(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
		client.addHeader(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());

		if (config.isBinaryMode()) {
			client.send(message.eventBody());
		} else {
			client.send(new String(message.eventBody(), StandardCharsets.UTF_8));
		}
	}

	@Override
	public void close() {
		clientCache.forEach((url, client) -> ValidationUtils.tryClose(client, "client for " + url));
		clientCache.clear();
	}
}
