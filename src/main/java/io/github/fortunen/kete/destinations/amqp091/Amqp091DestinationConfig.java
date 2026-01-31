package io.github.fortunen.kete.destinations.amqp091;

import com.rabbitmq.client.ConnectionFactory;

import io.github.fortunen.kete.DestinationConfig;
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
	public static final int DEFAULT_PORT = 5672;

	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";

	public static final String DEFAULT_VIRTUAL_HOST = "/";
	public static final String VIRTUAL_HOST = "virtual-host";

	public static final String EXCHANGE = "exchange";
	public static final String ROUTING_KEY = "routing-key";

	public static final int MIN_PRIORITY = 0;
	public static final int MAX_PRIORITY = 9;
	public static final int DEFAULT_PRIORITY = 4;
	public static final String PRIORITY = "priority";

	public static final long DEFAULT_TIME_TO_LIVE = 0L;
	public static final String TIME_TO_LIVE = "time-to-live";
	public static final String DELIVERY_MODE = "delivery-mode";

	public static final int DEFAULT_HANDSHAKE_TIMEOUT_MS = 10000;
	public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 10000;
	public static final int DEFAULT_CHANNEL_RPC_TIMEOUT_MS = 10000;
	public static final String HANDSHAKE_TIMEOUT = "handshake-timeout";
	public static final String CONNECTION_TIMEOUT = "connection-timeout";
	public static final String CHANNEL_RPC_TIMEOUT = "channel-rpc-timeout";

	public static final int DEFAULT_REQUESTED_HEARTBEAT_SECONDS = 30;
	public static final int DEFAULT_NETWORK_RECOVERY_INTERVAL_MS = 5000;
	public static final String REQUESTED_HEARTBEAT = "requested-heartbeat";
	public static final String NETWORK_RECOVERY_INTERVAL = "network-recovery-interval";
	public static final String TOPOLOGY_RECOVERY_ENABLED = "topology-recovery-enabled";
	public static final String AUTOMATIC_RECOVERY_ENABLED = "automatic-recovery-enabled";

	private int port;
	private String host;
	private int priority;
	private long timeToLive;
	private int deliveryMode;
	private String username;
	private String password;
	private String exchange;
	private String routingKey;
	private String virtualHost;
	private boolean hasPriority;
	private boolean hasTimeToLive;
	private String deliveryModeString;
	private int handshakeTimeout;
	private int connectionTimeout;
	private int channelRpcTimeout;
	private int requestedHeartbeat;
	private int networkRecoveryInterval;
	private boolean messageHeadersEnabled;
	private boolean topologyRecoveryEnabled;
	private boolean automaticRecoveryEnabled;
	private ConnectionFactory connectionFactory;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// host

		host = ValidationUtils.requireNonBlank(configuration.getString(HOST, "").trim(), HOST + " is required");

		// exchange

		exchange = ValidationUtils.requireNonBlank(configuration.getString(EXCHANGE, "").trim(), EXCHANGE + " is required");

		// routing key

		routingKey = configuration.getString(ROUTING_KEY, "").trim();

		// username

		username = configuration.getString(USERNAME, "").trim();

		// password

		password = configuration.getString(PASSWORD, "").trim();

		// virtual host (RabbitMQ default)

		virtualHost = configuration.getString(VIRTUAL_HOST, DEFAULT_VIRTUAL_HOST).trim();

		// port

		port = configuration.getInt(PORT, DEFAULT_PORT);

		ValidationUtils.requireValidPort(port, PORT);

		// timeouts

		handshakeTimeout = ValidationUtils.requireNonNegative(configuration.getInt(HANDSHAKE_TIMEOUT, DEFAULT_HANDSHAKE_TIMEOUT_MS), HANDSHAKE_TIMEOUT + " must be non-negative");
		connectionTimeout = ValidationUtils.requireNonNegative(configuration.getInt(CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT_MS), CONNECTION_TIMEOUT + " must be non-negative");
		channelRpcTimeout = ValidationUtils.requireNonNegative(configuration.getInt(CHANNEL_RPC_TIMEOUT, DEFAULT_CHANNEL_RPC_TIMEOUT_MS), CHANNEL_RPC_TIMEOUT + " must be non-negative");

		// heartbeat / recovery (RabbitMQ-friendly defaults)

		automaticRecoveryEnabled = configuration.getBoolean(AUTOMATIC_RECOVERY_ENABLED, true);
		topologyRecoveryEnabled = configuration.getBoolean(TOPOLOGY_RECOVERY_ENABLED, automaticRecoveryEnabled);
		requestedHeartbeat = ValidationUtils.requireNonNegative(configuration.getInt(REQUESTED_HEARTBEAT, DEFAULT_REQUESTED_HEARTBEAT_SECONDS), REQUESTED_HEARTBEAT + " must be non-negative");
		networkRecoveryInterval = ValidationUtils.requirePositive(configuration.getInt(NETWORK_RECOVERY_INTERVAL, DEFAULT_NETWORK_RECOVERY_INTERVAL_MS), NETWORK_RECOVERY_INTERVAL + " must be positive");

		// priority (optional)

		if (configuration.containsKey(PRIORITY)) {

			priority = configuration.getInt(PRIORITY);

			ValidationUtils.requireTrue(priority >= MIN_PRIORITY && priority <= MAX_PRIORITY, PRIORITY + " must be between " + MIN_PRIORITY + " and " + MAX_PRIORITY);

			hasPriority = true;

		} else {
			priority = DEFAULT_PRIORITY;
		}

		// deliveryMode

		deliveryModeString = configuration.getString(DELIVERY_MODE, "persistent").trim().toLowerCase();

		ValidationUtils.requireTrue(deliveryModeString.equals("persistent") || deliveryModeString.equals("non-persistent"), DELIVERY_MODE + " must be 'persistent' or 'non-persistent'");

		deliveryMode = deliveryModeString.equals("persistent") ? 2 : 1;

		// timeToLive

		if (configuration.containsKey(TIME_TO_LIVE)) {
			timeToLive = ValidationUtils.requireNonNegative(configuration.getLong(TIME_TO_LIVE, DEFAULT_TIME_TO_LIVE), TIME_TO_LIVE + " must be non-negative");
			hasTimeToLive = true;
		} else {
			timeToLive = DEFAULT_TIME_TO_LIVE;
		}

		// messageHeadersEnabled

		messageHeadersEnabled = configuration.getBoolean(MESSAGE_HEADERS_ENABLED, true);

		// connection factory

		connectionFactory = new ConnectionFactory();

		connectionFactory.setHost(host);
		connectionFactory.setPort(port);

		connectionFactory.setHandshakeTimeout(handshakeTimeout);
		connectionFactory.setConnectionTimeout(connectionTimeout);
		connectionFactory.setChannelRpcTimeout(channelRpcTimeout);
		connectionFactory.setRequestedHeartbeat(requestedHeartbeat);

		connectionFactory.setNetworkRecoveryInterval(networkRecoveryInterval);
		connectionFactory.setTopologyRecoveryEnabled(topologyRecoveryEnabled);
		connectionFactory.setAutomaticRecoveryEnabled(automaticRecoveryEnabled);

		if (ValidationUtils.isNotBlank(username)) {
			connectionFactory.setUsername(username);
		}

		if (ValidationUtils.isNotBlank(password)) {
			connectionFactory.setPassword(password);
		}

		if (ValidationUtils.isNotBlank(virtualHost)) {
			connectionFactory.setVirtualHost(virtualHost);
		}

		if (tls.isEnabled()) {
			connectionFactory.useSslProtocol(tls.getKeyStoreAndTrustStoreSSLContext());
		}
	}
}
