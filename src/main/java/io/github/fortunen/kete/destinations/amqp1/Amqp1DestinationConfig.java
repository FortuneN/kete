package io.github.fortunen.kete.destinations.amqp1;

import org.apache.commons.lang3.Strings;
import org.apache.qpid.jms.JmsConnectionFactory;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.utils.ConfigurationUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import jakarta.jms.DeliveryMode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class Amqp1DestinationConfig extends DestinationConfig {

	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final int DEFAULT_TCP_PORT = 5672;
	public static final int DEFAULT_TLS_PORT = 5671;
	public static final String TRANSPORT_TYPE = "transport-type";

	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";

	public static final String TLS = "tls";
	public static final String TLS_ENABLED = TLS + ".enabled";

	public static final int DEFAULT_IDLE_TIMEOUT_SECONDS = 60;
	public static final String IDLE_TIMEOUT_SECONDS = "idle-timeout-seconds";

	public static final String DESTINATION_NAME = "destination-name";
	public static final String DESTINATION_TYPE = "destination-type";

	public static final int MIN_PRIORITY = 0;
	public static final int MAX_PRIORITY = 9;
	public static final int DEFAULT_PRIORITY = 4;
	public static final String PRIORITY = "priority";
	public static final long DEFAULT_TIME_TO_LIVE_SECONDS = 0L;
	public static final String DELIVERY_MODE = "delivery-mode";
	public static final String TIME_TO_LIVE_SECONDS = "time-to-live-seconds";

	private int port;
	private String url;
	private String host;
	private int priority;
	private String scheme;
	private String username;
	private String password;
	private int deliveryMode;
	private String transportType;
	private long timeToLiveSeconds;
	private int idleTimeoutSeconds;
	private String destinationType;
	private String queueOrTopicName;
	private String deliveryModeString;
	private boolean destinationIsQueue;
	private JmsConnectionFactory connectionFactory;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// host

		host = ValidationUtils.requireNonBlank(configuration.getString(HOST, "").trim(), HOST + " is required");

		// tls - auto-enable for Azure Service Bus

		if (!tls.isEnabled() && Strings.CS.contains(host, "servicebus")) {
			configuration.setProperty(TLS_ENABLED, "true");
			tls = TlsMaterial.builder().withConfiguration(ConfigurationUtils.getSubSet(configuration, TLS)).build();
		}

		// transportType

		transportType = configuration.getString(TRANSPORT_TYPE, "amqp").trim().toLowerCase();

		ValidationUtils.requireTrue(transportType.equals("amqp") || transportType.equals("amqp-web-sockets"), TRANSPORT_TYPE + " must be 'amqp' or 'amqp-web-sockets'");

		// username

		username = configuration.getString(USERNAME, "").trim();

		// password

		password = configuration.getString(PASSWORD, "").trim();

		// queueOrTopicName

		queueOrTopicName = ValidationUtils.requireNonBlank(configuration.getString(DESTINATION_NAME, "").trim(), DESTINATION_NAME + " is required");

		// destinationType

		destinationType = configuration.getString(DESTINATION_TYPE, "queue").trim().toLowerCase();

		ValidationUtils.requireTrue(destinationType.equals("queue") || destinationType.equals("topic"), DESTINATION_TYPE + " must be 'queue' or 'topic'");

		destinationIsQueue = destinationType.equals("queue");

		// scheme

		if (transportType.equals("amqp-web-sockets")) {
			scheme = tls.isEnabled() ? "amqpwss" : "amqpws";
			port = configuration.getInt(PORT, 443);
		} else {
			scheme = tls.isEnabled() ? "amqps" : "amqp";
			port = configuration.getInt(PORT, tls.isEnabled() ? DEFAULT_TLS_PORT : DEFAULT_TCP_PORT);
		}

		ValidationUtils.requireValidPort(port, PORT);

		// idleTimeoutSeconds

		idleTimeoutSeconds = ValidationUtils.requireNonNegative(configuration.getInt(IDLE_TIMEOUT_SECONDS, DEFAULT_IDLE_TIMEOUT_SECONDS), IDLE_TIMEOUT_SECONDS + " must be non-negative");

		// url (AMQP protocol expects idle timeout in milliseconds)

		if (idleTimeoutSeconds > 0) {
			url = scheme + "://" + host + ":" + port + "?amqp.idleTimeout=" + (idleTimeoutSeconds * 1000);
		} else {
			url = scheme + "://" + host + ":" + port + "?amqp.idleTimeout=0";
		}

		// deliveryMode

		deliveryModeString = configuration.getString(DELIVERY_MODE, "persistent").trim().toLowerCase();

		ValidationUtils.requireTrue(deliveryModeString.equals("persistent") || deliveryModeString.equals("non-persistent"), DELIVERY_MODE + " must be 'persistent' or 'non-persistent'");

		deliveryMode = deliveryModeString.equals("persistent") ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;

		// priority

		priority = ValidationUtils.requireInRange(configuration.getInt(PRIORITY, DEFAULT_PRIORITY), MIN_PRIORITY, MAX_PRIORITY, PRIORITY + " must be between " + MIN_PRIORITY + " and " + MAX_PRIORITY);

		// timeToLiveSeconds

		timeToLiveSeconds = ValidationUtils.requireNonNegative(configuration.getLong(TIME_TO_LIVE_SECONDS, DEFAULT_TIME_TO_LIVE_SECONDS), TIME_TO_LIVE_SECONDS + " must be non-negative");

		// connectionFactory

		connectionFactory = new JmsConnectionFactory(url);

		if (tls.isEnabled()) {
			connectionFactory.setSslContext(tls.getKeyStoreAndTrustStoreSSLContext());
		}

		if (ValidationUtils.isNotBlank(username)) {
			connectionFactory.setUsername(username);
		}

		if (ValidationUtils.isNotBlank(password)) {
			connectionFactory.setPassword(password);
		}
	}
}
