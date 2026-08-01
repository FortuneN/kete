package io.github.fortunen.kete.destinations.socketio;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.OAuthMaterial;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.socket.client.IO;
import io.socket.client.Socket;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@Component(name = "socketio")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class SocketIODestination extends Destination<SocketIODestinationConfig> {

	private Socket socket;
	private String eventName;
	private int timeoutSeconds;
	private OAuthMaterial oauth;
	private boolean isOauthEnabled;
	private boolean isEventNameTemplated;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		oauth = config.getOauth();
		eventName = config.getEventName();
		isOauthEnabled = config.isOauthEnabled();
		timeoutSeconds = config.getTimeoutSeconds();
		isEventNameTemplated = config.isEventNameTemplated();

		// inject oauth authorization header into options before socket creation

		var options = config.getOptions();

		if (isOauthEnabled) {
			
			if (options.extraHeaders == null) {
				options.extraHeaders = new HashMap<>();
			}

			options.extraHeaders.put("Authorization", Collections.singletonList(oauth.getAccessToken().toAuthorizationHeader()));
		}

		// create and verify connection

		socket = IO.socket(URI.create(config.getSocketUrl()), options);

		var latch = new CountDownLatch(1);

		socket.on(Socket.EVENT_CONNECT, args -> latch.countDown());
		socket.connect();

		var connected = latch.await(timeoutSeconds, TimeUnit.SECONDS);

		ValidationUtils.requireTrue(connected, "failed to connect to Socket.IO server within " + timeoutSeconds + " seconds");
	}

	@Override
	public boolean isHealthy() {
		return ValidationUtils.isNotNull(socket) && socket.connected();
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// emit() on a disconnected socket buffers silently instead of throwing; fail fast so
		// route retry and pool replacement handle the outage

		ValidationUtils.requireTrue(socket.connected(), "socket is not connected");

		var actualEventName = isEventNameTemplated ? TemplateUtils.substitute(eventName, message) : eventName;

		var body = new String(message.eventBody(), StandardCharsets.UTF_8);

		if (message.contentType().contains("json")) {
			socket.emit(actualEventName, new JSONObject(body));
		} else {
			socket.emit(actualEventName, body);
		}
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(socket, "socket");
	}
}
