package io.github.fortunen.kete.destinations.mqtt3;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.DestinationUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"password"})
public class Mqtt3DestinationConfig extends DestinationConfig {

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

	private int qos;
	private int port;
	private String url;
	private String host;
	private String topic;
	private String scheme;
	private String username;
	private String password;
	private int maxInflight;
	private boolean retained;
	private boolean cleanSession;
	private String transportType;
	private String clientIdPrefix;
	private boolean isTopicTemplated;
	private int connectionTimeoutSeconds;
	private int keepAliveIntervalSeconds;
	private MqttConnectOptions connectOptions;
	private AtomicInteger clientIdCounter = new AtomicInteger(0);

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// host

		host = ValidationUtils.requireNonBlank(configuration.getString(HOST, "").trim(), HOST + " is required");

		// transport-type

		transportType = configuration.getString(TRANSPORT_TYPE, "tcp").trim().toLowerCase();

		ValidationUtils.requireTrue(transportType.equals("tcp") || transportType.equals("websocket"), TRANSPORT_TYPE + " must be 'tcp' or 'websocket'");

		// scheme + port defaults based on transport-type and TLS

		if (transportType.equals("websocket")) {
			scheme = tls.isEnabled() ? "wss://" : "ws://";
			port = configuration.getInt(PORT, tls.isEnabled() ? DEFAULT_WSS_PORT : DEFAULT_WS_PORT);
		} else {
			scheme = tls.isEnabled() ? "ssl://" : "tcp://";
			port = configuration.getInt(PORT, tls.isEnabled() ? DEFAULT_TLS_PORT : DEFAULT_TCP_PORT);
		}

		ValidationUtils.requireValidPort(port, PORT);

		// url

		url = scheme + host + ":" + port;

		// topic

		topic = ValidationUtils.requireNonBlank(configuration.getString(TOPIC, "").trim(), TOPIC + " is required");

		// qos

		qos = ValidationUtils.requireInRange(configuration.getInt(QOS, DEFAULT_QOS), MIN_QOS, MAX_QOS, QOS + " must be " + MIN_QOS + ", " + (MIN_QOS + 1) + " or " + MAX_QOS);

		// retained

		retained = configuration.getBoolean(RETAINED, false);

		// clientIdPrefix

		clientIdPrefix = configuration.getString(CLIENT_ID_PREFIX, "").trim();

		if (ValidationUtils.isBlank(clientIdPrefix)) {
			clientIdPrefix = DestinationUtils.generateClientIdPrefix();
		}

		// cleanSession

		cleanSession = configuration.getBoolean(CLEAN_SESSION, true);

		// connectionTimeoutSeconds

		connectionTimeoutSeconds = configuration.getInt(CONNECTION_TIMEOUT_SECONDS, DEFAULT_CONNECTION_TIMEOUT_SECONDS);

		// keepAliveIntervalSeconds

		keepAliveIntervalSeconds = configuration.getInt(KEEP_ALIVE_INTERVAL_SECONDS, DEFAULT_KEEP_ALIVE_INTERVAL_SECONDS);

		// maxInflight

		maxInflight = configuration.getInt(MAX_INFLIGHT, DEFAULT_MAX_INFLIGHT);

		// username

		username = configuration.getString(USERNAME, "").trim();

		// password

		password = configuration.getString(PASSWORD, "").trim();

		// precomputed fields

		isTopicTemplated = TemplateUtils.containsTemplate(topic);

		// connectOptions

		connectOptions = new MqttConnectOptions();
		connectOptions.setMaxInflight(maxInflight);
		connectOptions.setConnectionTimeout(connectionTimeoutSeconds);
		connectOptions.setAutomaticReconnect(true);

		if (configuration.containsKey(CLEAN_SESSION)) {
			connectOptions.setCleanSession(cleanSession);
		}

		if (configuration.containsKey(KEEP_ALIVE_INTERVAL_SECONDS)) {
			connectOptions.setKeepAliveInterval(keepAliveIntervalSeconds);
		}

		if (ValidationUtils.isNotBlank(username)) {
			connectOptions.setUserName(username);
		}

		if (ValidationUtils.isNotBlank(password)) {
			connectOptions.setPassword(password.toCharArray());
		}

		if (tls.isEnabled()) {
			connectOptions.setSocketFactory(tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory());
			connectOptions.setHttpsHostnameVerificationEnabled(tls.isVerifyHostname());
		}
	}
}
