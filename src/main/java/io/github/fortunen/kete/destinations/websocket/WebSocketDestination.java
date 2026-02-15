package io.github.fortunen.kete.destinations.websocket;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.OAuthMaterial;
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

	private String url;
	private OAuthMaterial oauth;
	private boolean isBinaryMode;
	private boolean isUrlTemplated;
	private boolean isOauthEnabled;
	private SocketFactory socketFactory;
	private int connectionTimeoutSeconds;
	private TimeUnit connectionTimeoutUnit;
	private WebSocketClient webSocketClient;
	private boolean hasConnectionLostTimeout;
	private int connectionLostTimeoutSeconds;
	private Draft_6455 draft;
	private Map<String, String> customHeaders;
	private Set<Map.Entry<String, String>> customHeadersEntrySet;
	private ConcurrentHashMap<String, WebSocketClient> clientCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		url = config.getUrl();
		oauth = config.getOauth();
		isBinaryMode = config.isBinaryMode();
		isOauthEnabled = config.isOauthEnabled();
		socketFactory = config.getSocketFactory();
		customHeaders = config.getCustomHeaders();
		isUrlTemplated = config.isUrlTemplated();
		draft = config.getDraft();
		connectionTimeoutUnit = config.getConnectionTimeoutUnit();
		customHeadersEntrySet = config.getCustomHeadersEntrySet();
		connectionTimeoutSeconds = config.getConnectionTimeoutSeconds();
		hasConnectionLostTimeout = config.isHasConnectionLostTimeout();
		connectionLostTimeoutSeconds = config.getConnectionLostTimeoutSeconds();

		// verify connection

		if (!isUrlTemplated) {
			webSocketClient = createAndConnect(url);
		} else {
			try (var socket = new Socket()) {
				socket.connect(new InetSocketAddress(config.getHost(), config.getPort()), connectionTimeoutSeconds * 1000);
			}
		}
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// client

		var actualUrl = isUrlTemplated ? TemplateUtils.substitute(url, message) : url;

		var client = isUrlTemplated ? clientCache.computeIfAbsent(actualUrl, this::createAndConnect) : webSocketClient;

		client.clearHeaders();

		if (isOauthEnabled) {
			client.addHeader("Authorization", oauth.getAccessToken().toAuthorizationHeader());
		}

		for (var entry : customHeadersEntrySet) {
			client.addHeader(entry.getKey(), entry.getValue());
		}

		client.addHeader(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		client.addHeader(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
		client.addHeader(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());

		if (isBinaryMode) {
			client.send(message.eventBody());
		} else {
			client.send(new String(message.eventBody(), StandardCharsets.UTF_8));
		}
	}

	@SneakyThrows
	private WebSocketClient createAndConnect(String targetUrl) {

		var wsClient = ValidationUtils.isNotNull(draft)
			? new WebSocketClient(URI.create(targetUrl), draft, customHeaders, connectionTimeoutSeconds * 1000) {

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
			}
			: new WebSocketClient(URI.create(targetUrl), customHeaders) {

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

		if (ValidationUtils.isNotNull(socketFactory)) {
			wsClient.setSocketFactory(socketFactory);
		}

		if (hasConnectionLostTimeout) {
			wsClient.setConnectionLostTimeout(connectionLostTimeoutSeconds);
		}

		var connected = wsClient.connectBlocking(connectionTimeoutSeconds, connectionTimeoutUnit);

		if (!connected) {
			throw new RuntimeException("failed to connect to WebSocket server at " + targetUrl);
		}

		return wsClient;
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(webSocketClient, "webSocketClient");
		clientCache.forEach((url, client) -> ValidationUtils.tryClose(client, "client for " + url));
		clientCache.clear();
	}
}
