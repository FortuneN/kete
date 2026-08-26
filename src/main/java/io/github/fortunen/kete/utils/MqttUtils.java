package io.github.fortunen.kete.utils;

import org.apache.commons.configuration2.MapConfiguration;

import io.github.fortunen.kete.TlsMaterial;

// the MQTT 3 and MQTT 5 destinations read the same options; only the Paho option objects differ

public final class MqttUtils {

	private MqttUtils() {}

	public static final int MIN_QOS = 0;
	public static final int MAX_QOS = 2;
	public static final String QOS = "qos";
	public static final int DEFAULT_QOS = 1;
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String TOPIC = "topic";
	public static final int DEFAULT_WS_PORT = 8000;
	public static final int DEFAULT_WSS_PORT = 443;
	public static final int DEFAULT_TCP_PORT = 1883;
	public static final int DEFAULT_TLS_PORT = 8883;
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String RETAINED = "retained";
	public static final int DEFAULT_MAX_INFLIGHT = 2048;
	public static final String MAX_INFLIGHT = "max-inflight";
	public static final String CLEAN_SESSION = "clean-session";
	public static final String TRANSPORT_TYPE = "transport-type";
	public static final int DEFAULT_CONNECTION_TIMEOUT_SECONDS = 10;
	public static final String CLIENT_ID_PREFIX = "client-id-prefix";
	public static final int DEFAULT_KEEP_ALIVE_INTERVAL_SECONDS = 60;
	public static final String CONNECTION_TIMEOUT_SECONDS = "connection-timeout-seconds";
	public static final String KEEP_ALIVE_INTERVAL_SECONDS = "keep-alive-interval-seconds";

	public record Settings(String host, String transportType, String scheme, int port, String url, String topic, int qos, boolean retained, String clientIdPrefix,
		boolean cleanSession, boolean hasCleanSession, int connectionTimeoutSeconds, int keepAliveIntervalSeconds, boolean hasKeepAliveInterval, int maxInflight, String username, String password) {}

	public static Settings parseSettings(MapConfiguration configuration, TlsMaterial tls) {

		ValidationUtils.requireNonNull(configuration, "configuration is required");
		ValidationUtils.requireNonNull(tls, "tls is required");

		var host = ValidationUtils.requireNonBlank(configuration.getString(HOST, "").trim(), HOST + " is required");

		var transportType = configuration.getString(TRANSPORT_TYPE, "tcp").trim().toLowerCase();

		ValidationUtils.requireTrue(transportType.equals("tcp") || transportType.equals("websocket"), TRANSPORT_TYPE + " must be 'tcp' or 'websocket'");

		// scheme + port defaults follow the transport type and TLS

		String scheme;
		int port;

		if (transportType.equals("websocket")) {
			scheme = tls.isEnabled() ? "wss://" : "ws://";
			port = configuration.getInt(PORT, tls.isEnabled() ? DEFAULT_WSS_PORT : DEFAULT_WS_PORT);
		} else {
			scheme = tls.isEnabled() ? "ssl://" : "tcp://";
			port = configuration.getInt(PORT, tls.isEnabled() ? DEFAULT_TLS_PORT : DEFAULT_TCP_PORT);
		}

		ValidationUtils.requireValidPort(port, PORT);

		var topic = ValidationUtils.requireNonBlank(configuration.getString(TOPIC, "").trim(), TOPIC + " is required");
		var qos = ValidationUtils.requireInRange(configuration.getInt(QOS, DEFAULT_QOS), MIN_QOS, MAX_QOS, QOS + " must be " + MIN_QOS + ", " + (MIN_QOS + 1) + " or " + MAX_QOS);

		var clientIdPrefix = configuration.getString(CLIENT_ID_PREFIX, "").trim();

		if (ValidationUtils.isBlank(clientIdPrefix)) {
			clientIdPrefix = DestinationUtils.generateClientIdPrefix();
		}

		return new Settings(
			host,
			transportType,
			scheme,
			port,
			scheme + host + ":" + port,
			topic,
			qos,
			configuration.getBoolean(RETAINED, false),
			clientIdPrefix,
			configuration.getBoolean(CLEAN_SESSION, true),
			configuration.containsKey(CLEAN_SESSION),
			configuration.getInt(CONNECTION_TIMEOUT_SECONDS, DEFAULT_CONNECTION_TIMEOUT_SECONDS),
			configuration.getInt(KEEP_ALIVE_INTERVAL_SECONDS, DEFAULT_KEEP_ALIVE_INTERVAL_SECONDS),
			configuration.containsKey(KEEP_ALIVE_INTERVAL_SECONDS),
			configuration.getInt(MAX_INFLIGHT, DEFAULT_MAX_INFLIGHT),
			configuration.getString(USERNAME, "").trim(),
			configuration.getString(PASSWORD, "").trim());
	}
}
