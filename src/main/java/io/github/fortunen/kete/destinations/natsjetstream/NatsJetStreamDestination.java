package io.github.fortunen.kete.destinations.natsjetstream;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.JetStream;
import io.nats.client.JetStreamOptions;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor(force = true)
@Component(name = "nats-jetstream")
@EqualsAndHashCode(callSuper = true)
public class NatsJetStreamDestination extends Destination<NatsJetStreamDestinationConfig> {

	private String subject;
	private volatile JetStream jetStream;
	private volatile Connection connection;
	private boolean isSubjectTemplated;
	private Set<Map.Entry<String, String>> customHeadersEntrySet;

	private volatile boolean closed;
	private final Object connectionLock = new Object();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		subject = config.getSubject();
		isSubjectTemplated = config.isSubjectTemplated();
		customHeadersEntrySet = config.getCustomHeadersEntrySet();

		reconnectIfNeeded();
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// subject

		var actualSubject = isSubjectTemplated ? TemplateUtils.substitute(subject, message) : subject;

		// message

		var messageBuilder = NatsMessage.builder()
			.subject(actualSubject)
			.data(message.eventBody());

		// headers (message headers take priority over custom headers)

		var headers = new Headers();

		for (var entry : customHeadersEntrySet) {
			headers.add(entry.getKey(), entry.getValue());
		}

		headers.put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		headers.put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
		headers.put(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());

		messageBuilder.headers(headers);

		var natsMessage = messageBuilder.build();

		// the client never recovers from a terminal CLOSED on its own; re-establish first

		var currentConnection = connection;

		if (currentConnection != null && currentConnection.getStatus() == Connection.Status.CLOSED) {
			reconnectIfNeeded();
		}

		// publish with ack (blocks until server acknowledges or timeout)

		try {
			jetStream.publish(natsMessage);
		} catch (IOException | IllegalStateException exception) {

			// on a healthy connection the failure is a bare ack timeout and the publish may
			// have reached the stream; retrying could double-publish, so propagate instead

			var connectionAfterFailure = connection;

			if (connectionAfterFailure != null && connectionAfterFailure.getStatus() == Connection.Status.CONNECTED) {
				throw exception;
			}

			// the connection died underneath the publish; re-establish once and retry,
			// otherwise let the failure propagate

			reconnectIfNeeded();
			jetStream.publish(natsMessage);
		}
	}

	@Override
	public void close() {

		Connection currentConnection;

		synchronized (connectionLock) {
			closed = true;
			currentConnection = connection;
		}

		ValidationUtils.tryClose(currentConnection, "connection");
	}

	private Options buildOptions() {
		return new Options.Builder(config.getNatsOptions())
			.connectionListener(this::onConnectionEvent)
			.build();
	}

	private JetStreamOptions buildJetStreamOptions() {
		return JetStreamOptions.builder()
			.publishNoAck(false)
			.requestTimeout(config.getPublishTimeout())
			.build();
	}

	private void reconnectIfNeeded() {

		synchronized (connectionLock) {

			if (closed) {
				throw new IllegalStateException("destination is closed");
			}

			var currentConnection = connection;

			if (currentConnection != null && currentConnection.getStatus() == Connection.Status.CONNECTED) {
				return;
			}

			connect();

			ValidationUtils.tryClose(currentConnection, "connection");
		}
	}

	@SneakyThrows
	protected void connect() {

		var newConnection = Nats.connect(buildOptions());

		try {

			// verify connection

			ValidationUtils.requireTrue(newConnection.getStatus() == Connection.Status.CONNECTED, "failed to connect to NATS server");

			// verify stream exists

			var jsm = newConnection.jetStreamManagement();

			if (jsm.getStreamInfo(config.getStream()) == null) {
				throw new IllegalStateException("stream '" + config.getStream() + "' does not exist on the NATS server");
			}

			// jetStream

			jetStream = newConnection.jetStream(buildJetStreamOptions());
			connection = newConnection;

		} catch (Exception exception) {

			ValidationUtils.tryClose(newConnection, "connection");

			if (connection == newConnection) {
				connection = null;
				jetStream = null;
			}

			throw exception;
		}
	}

	private void onConnectionEvent(Connection eventConnection, ConnectionListener.Events event) {

		switch (event) {
			case DISCONNECTED -> log.warn("NATS connection lost, client is reconnecting");
			case RECONNECTED -> log.warn("NATS connection re-established by client reconnect");
			case CONNECTED -> adoptConnection(eventConnection);
			case CLOSED -> onConnectionClosed(eventConnection);
			default -> { /* not relevant */ }
		}
	}

	private void onConnectionClosed(Connection eventConnection) {

		synchronized (connectionLock) {

			if (closed || eventConnection != connection) {
				return;
			}

			// terminal state (e.g. repeated auth failures during reconnect); the client will
			// not recover this connection on its own, so let it dial a new one in the
			// background using its own reconnect policy; the new connection arrives via a
			// CONNECTED event and is adopted there; the dial retries indefinitely (matching
			// maxReconnects(-1)) because giving up would permanently stop publishing, and it
			// logs one error per terminal close, not per attempt
			log.error("NATS connection closed permanently, re-establishing in the background");

			try {
				Nats.connectAsynchronously(buildOptions(), true);
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				log.error("Interrupted while starting NATS background reconnect");
			}
		}
	}

	@SneakyThrows
	private void adoptConnection(Connection newConnection) {

		Connection redundantConnection;

		synchronized (connectionLock) {

			if (newConnection == connection) {
				return; // synchronous connect; connect() adopts it itself
			}

			if (newConnection.getStatus() != Connection.Status.CONNECTED) {
				return; // died before adoption (e.g. a failed synchronous connect closed it)
			}

			var currentConnection = connection;

			if (closed || (currentConnection != null && currentConnection.getStatus() == Connection.Status.CONNECTED)) {
				// destination closed, or a send already re-established the connection
				redundantConnection = newConnection;
			} else {
				jetStream = newConnection.jetStream(buildJetStreamOptions());
				connection = newConnection;
				redundantConnection = currentConnection;
				log.warn("NATS JetStream connection re-established after terminal close");
			}
		}

		// close outside the lock; closing can block on the connection's callback threads

		ValidationUtils.tryClose(redundantConnection, "connection");
	}
}
