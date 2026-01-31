package io.github.fortunen.kete.destinations.stomp;

import javax.net.SocketFactory;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class StompDestinationConfig extends DestinationConfig {

	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final int DEFAULT_TCP_PORT = 61613;
	public static final int DEFAULT_TLS_PORT = 61614;

	public static final String DESTINATION = "destination";
	public static final String VIRTUAL_HOST = "virtual-host";

	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";

	public static final String RECEIPT_ENABLED = "receipt-enabled";

	public static final int DEFAULT_HEART_BEAT = 30000;
	public static final String HEART_BEAT_OUTGOING = "heart-beat-outgoing";
	public static final String HEART_BEAT_INCOMING = "heart-beat-incoming";

	public static final int DEFAULT_READ_TIMEOUT_MILLIS = 30000;
	public static final String READ_TIMEOUT_MILLIS = "read-timeout-millis";

	private int port;
	private String host;
	private String destination;
	private String virtualHost;
	private String username;
	private String password;
	private boolean receiptEnabled;
	private int heartBeatOutgoing;
	private int heartBeatIncoming;
	private int readTimeoutMillis;
	private boolean messageHeadersEnabled;
	private SocketFactory socketFactory;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// host

		host = ValidationUtils.requireNonBlank(configuration.getString(HOST, "").trim(), HOST + " is required");

		// port

		port = configuration.getInt(PORT, tls.isEnabled() ? DEFAULT_TLS_PORT : DEFAULT_TCP_PORT);

		ValidationUtils.requireValidPort(port, PORT);

		// destination

		destination = ValidationUtils.requireNonBlank(configuration.getString(DESTINATION, "").trim(), DESTINATION + " is required");

		// virtualHost

		virtualHost = configuration.getString(VIRTUAL_HOST, host).trim();

		// username

		username = configuration.getString(USERNAME, "").trim();

		// password

		password = configuration.getString(PASSWORD, "").trim();

		// receiptEnabled

		receiptEnabled = configuration.getBoolean(RECEIPT_ENABLED, false);

		// heartBeat

		heartBeatOutgoing = configuration.getInt(HEART_BEAT_OUTGOING, DEFAULT_HEART_BEAT);
		heartBeatIncoming = configuration.getInt(HEART_BEAT_INCOMING, DEFAULT_HEART_BEAT);

		ValidationUtils.requireNonNegative(heartBeatOutgoing, HEART_BEAT_OUTGOING + " must be non-negative");
		ValidationUtils.requireNonNegative(heartBeatIncoming, HEART_BEAT_INCOMING + " must be non-negative");

		// readTimeoutMillis

		readTimeoutMillis = configuration.getInt(READ_TIMEOUT_MILLIS, DEFAULT_READ_TIMEOUT_MILLIS);

		ValidationUtils.requireGreaterThan(readTimeoutMillis, 0, READ_TIMEOUT_MILLIS + " must be greater than 0");

		// messageHeadersEnabled

		messageHeadersEnabled = configuration.getBoolean(MESSAGE_HEADERS_ENABLED, true);

		// socketFactory

		socketFactory = tls.isEnabled() ? tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory() : SocketFactory.getDefault();
	}
}
