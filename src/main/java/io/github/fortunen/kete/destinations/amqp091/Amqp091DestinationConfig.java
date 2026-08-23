package io.github.fortunen.kete.destinations.amqp091;

import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.ConnectionFactory;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class Amqp091DestinationConfig extends DestinationConfig {

	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final int MIN_PRIORITY = 0;
	public static final int MAX_PRIORITY = 9;
	public static final int DEFAULT_PORT = 5672;
	public static final int DEFAULT_PRIORITY = 4;
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String EXCHANGE = "exchange";
	public static final String PRIORITY = "priority";
	public static final String DEFAULT_VIRTUAL_HOST = "/";
	public static final String ROUTING_KEY = "routing-key";
	public static final String VIRTUAL_HOST = "virtual-host";
	public static final long DEFAULT_TIME_TO_LIVE_SECONDS = 0L;
	public static final String DELIVERY_MODE = "delivery-mode";
	public static final int DEFAULT_HANDSHAKE_TIMEOUT_SECONDS = 10;
	public static final int DEFAULT_CONNECTION_TIMEOUT_SECONDS = 10;
	public static final int DEFAULT_CHANNEL_RPC_TIMEOUT_SECONDS = 10;
	public static final int DEFAULT_REQUESTED_HEARTBEAT_SECONDS = 30;
	public static final int DEFAULT_NETWORK_RECOVERY_INTERVAL_SECONDS = 5;
	public static final String TIME_TO_LIVE_SECONDS = "time-to-live-seconds";
	public static final boolean DEFAULT_PUBLISHER_CONFIRMS = true;
	public static final String PUBLISHER_CONFIRMS = "publisher-confirms";
	public static final int DEFAULT_CONFIRM_TIMEOUT_SECONDS = 30;
	public static final String CONFIRM_TIMEOUT_SECONDS = "confirm-timeout-seconds";
	public static final String HANDSHAKE_TIMEOUT_SECONDS = "handshake-timeout-seconds";
	public static final String TOPOLOGY_RECOVERY_ENABLED = "topology-recovery-enabled";
	public static final String CONNECTION_TIMEOUT_SECONDS = "connection-timeout-seconds";
	public static final String AUTOMATIC_RECOVERY_ENABLED = "automatic-recovery-enabled";
	public static final String CHANNEL_RPC_TIMEOUT_SECONDS = "channel-rpc-timeout-seconds";
	public static final String REQUESTED_HEARTBEAT_SECONDS = "requested-heartbeat-seconds";
	public static final String NETWORK_RECOVERY_INTERVAL_SECONDS = "network-recovery-interval-seconds";

	private int port;
	private String host;
	private int priority;
	private String username;
	private String password;
	private String exchange;
	private int deliveryMode;
	private String routingKey;
	private String virtualHost;
	private boolean hasPriority;
	private long timeToLiveSeconds;
	private String deliveryModeString;
	private int handshakeTimeoutSeconds;
	private boolean isExchangeTemplated;
	private String timeToLiveExpiration;
	private boolean hasTimeToLiveSeconds;
	private int connectionTimeoutSeconds;
	private int channelRpcTimeoutSeconds;
	private int requestedHeartbeatSeconds;
	private boolean isRoutingKeyTemplated;
	private boolean topologyRecoveryEnabled;
	private boolean automaticRecoveryEnabled;
	private boolean publisherConfirms;
	private int confirmTimeoutSeconds;
	private Map<String, Object> staticHeaders;
	private int networkRecoveryIntervalSeconds;
	private ConnectionFactory connectionFactory;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// host

		host = ValidationUtils.requireNonBlank(configuration.getString(HOST, "").trim(), HOST + " is required");

		// exchange

		exchange = ValidationUtils.requireNonBlank(configuration.getString(EXCHANGE, "").trim(), EXCHANGE + " is required");

		// routingKey

		routingKey = configuration.getString(ROUTING_KEY, "").trim();

		// username

		username = configuration.getString(USERNAME, "").trim();

		// password

		password = configuration.getString(PASSWORD, "").trim();

		// virtualHost

		virtualHost = configuration.getString(VIRTUAL_HOST, DEFAULT_VIRTUAL_HOST).trim();

		// port

		port = configuration.getInt(PORT, DEFAULT_PORT);

		ValidationUtils.requireValidPort(port, PORT);

		// timeouts

		handshakeTimeoutSeconds = ValidationUtils.requireNonNegative(configuration.getInt(HANDSHAKE_TIMEOUT_SECONDS, DEFAULT_HANDSHAKE_TIMEOUT_SECONDS), HANDSHAKE_TIMEOUT_SECONDS + " must be non-negative");
		connectionTimeoutSeconds = ValidationUtils.requireNonNegative(configuration.getInt(CONNECTION_TIMEOUT_SECONDS, DEFAULT_CONNECTION_TIMEOUT_SECONDS), CONNECTION_TIMEOUT_SECONDS + " must be non-negative");
		channelRpcTimeoutSeconds = ValidationUtils.requireNonNegative(configuration.getInt(CHANNEL_RPC_TIMEOUT_SECONDS, DEFAULT_CHANNEL_RPC_TIMEOUT_SECONDS), CHANNEL_RPC_TIMEOUT_SECONDS + " must be non-negative");

		// heartbeat

		automaticRecoveryEnabled = configuration.getBoolean(AUTOMATIC_RECOVERY_ENABLED, true);
		topologyRecoveryEnabled = configuration.getBoolean(TOPOLOGY_RECOVERY_ENABLED, automaticRecoveryEnabled);
		requestedHeartbeatSeconds = ValidationUtils.requireNonNegative(configuration.getInt(REQUESTED_HEARTBEAT_SECONDS, DEFAULT_REQUESTED_HEARTBEAT_SECONDS), REQUESTED_HEARTBEAT_SECONDS + " must be non-negative");
		networkRecoveryIntervalSeconds = ValidationUtils.requirePositive(configuration.getInt(NETWORK_RECOVERY_INTERVAL_SECONDS, DEFAULT_NETWORK_RECOVERY_INTERVAL_SECONDS), NETWORK_RECOVERY_INTERVAL_SECONDS + " must be positive");

		// priority (optional)

		if (configuration.containsKey(PRIORITY)) {

			priority = configuration.getInt(PRIORITY);

			ValidationUtils.requireTrue(priority >= MIN_PRIORITY && priority <= MAX_PRIORITY, PRIORITY + " must be between " + MIN_PRIORITY + " and " + MAX_PRIORITY);

			hasPriority = true;
		}

		// publisherConfirms (broker acks each publish; without them basicPublish is fire-and-forget)

		publisherConfirms = configuration.getBoolean(PUBLISHER_CONFIRMS, DEFAULT_PUBLISHER_CONFIRMS);
		confirmTimeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(CONFIRM_TIMEOUT_SECONDS, DEFAULT_CONFIRM_TIMEOUT_SECONDS), CONFIRM_TIMEOUT_SECONDS + " must be positive");

		// deliveryMode

		deliveryModeString = configuration.getString(DELIVERY_MODE, "persistent").trim().toLowerCase();

		ValidationUtils.requireTrue(deliveryModeString.equals("persistent") || deliveryModeString.equals("non-persistent"), DELIVERY_MODE + " must be 'persistent' or 'non-persistent'");

		deliveryMode = deliveryModeString.equals("persistent") ? 2 : 1;

		// timeToLiveSeconds

		if (configuration.containsKey(TIME_TO_LIVE_SECONDS)) {
			timeToLiveSeconds = ValidationUtils.requireNonNegative(configuration.getLong(TIME_TO_LIVE_SECONDS, DEFAULT_TIME_TO_LIVE_SECONDS), TIME_TO_LIVE_SECONDS + " must be non-negative");
			hasTimeToLiveSeconds = true;
		} else {
			timeToLiveSeconds = DEFAULT_TIME_TO_LIVE_SECONDS;
		}

		// precomputed fields

		isExchangeTemplated = TemplateUtils.containsTemplate(exchange);
		isRoutingKeyTemplated = TemplateUtils.containsTemplate(routingKey);
		timeToLiveExpiration = (hasTimeToLiveSeconds && timeToLiveSeconds > 0) ? String.valueOf(timeToLiveSeconds * 1000) : null;

		staticHeaders = new HashMap<>();

		for (var entry : customHeadersEntrySet) {
			staticHeaders.put(entry.getKey(), entry.getValue());
		}

		// connectionFactory

		connectionFactory = new ConnectionFactory();

		connectionFactory.setHost(host);
		connectionFactory.setPort(port);

		connectionFactory.setRequestedHeartbeat(requestedHeartbeatSeconds);
		connectionFactory.setConnectionTimeout(connectionTimeoutSeconds * 1000);
		connectionFactory.setChannelRpcTimeout(channelRpcTimeoutSeconds * 1000);

		if (configuration.containsKey(HANDSHAKE_TIMEOUT_SECONDS)) {
			connectionFactory.setHandshakeTimeout(handshakeTimeoutSeconds * 1000);
		}

		if (configuration.containsKey(AUTOMATIC_RECOVERY_ENABLED)) {
			connectionFactory.setAutomaticRecoveryEnabled(automaticRecoveryEnabled);
		}

		if (configuration.containsKey(TOPOLOGY_RECOVERY_ENABLED)) {
			connectionFactory.setTopologyRecoveryEnabled(topologyRecoveryEnabled);
		}

		if (configuration.containsKey(NETWORK_RECOVERY_INTERVAL_SECONDS)) {
			connectionFactory.setNetworkRecoveryInterval(networkRecoveryIntervalSeconds * 1000);
		}

		if (ValidationUtils.isNotBlank(username)) {
			connectionFactory.setUsername(username);
		}

		if (ValidationUtils.isNotBlank(password)) {
			connectionFactory.setPassword(password);
		}

		if (ValidationUtils.isNotBlank(virtualHost) && configuration.containsKey(VIRTUAL_HOST)) {
			connectionFactory.setVirtualHost(virtualHost);
		}

		if (tls.isEnabled()) {
			var sslContext = tls.getKeyStoreAndTrustStoreSSLContext();
			connectionFactory.setSslContextFactory(name -> sslContext);
			connectionFactory.setSocketFactory(sslContext.getSocketFactory());
			if (tls.isVerifyHostname()) {
				connectionFactory.enableHostnameVerification();
			}
		}
	}
}
