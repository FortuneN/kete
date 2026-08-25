package io.github.fortunen.kete.destinations.stomp;

import java.util.HashMap;

import javax.net.SocketFactory;

import io.github.fortunen.kete.DestinationConfig;
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
public class StompDestinationConfig extends DestinationConfig {

	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final int DEFAULT_TCP_PORT = 61613;
	public static final int DEFAULT_TLS_PORT = 61614;
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String DESTINATION = "destination";
	public static final int DEFAULT_HEART_BEAT_SECONDS = 30;
	public static final String VIRTUAL_HOST = "virtual-host";
	public static final int DEFAULT_READ_TIMEOUT_SECONDS = 30;
	public static final boolean DEFAULT_RECEIPT_ENABLED = true;
	public static final String RECEIPT_ENABLED = "receipt-enabled";
	public static final String READ_TIMEOUT_SECONDS = "read-timeout-seconds";
	public static final String HEART_BEAT_OUTGOING_SECONDS = "heart-beat-outgoing-seconds";
	public static final String HEART_BEAT_INCOMING_SECONDS = "heart-beat-incoming-seconds";

	private int port;
	private String host;
	private String username;
	private String password;
	private String destination;
	private String virtualHost;
	private boolean receiptEnabled;
	private boolean isDestinationTemplated;
	private int readTimeoutSeconds;
	private SocketFactory socketFactory;
	private int heartBeatOutgoingSeconds;
	private int heartBeatIncomingSeconds;
	private HashMap<String, String> connectHeaders;

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

		receiptEnabled = configuration.getBoolean(RECEIPT_ENABLED, DEFAULT_RECEIPT_ENABLED);

		// heartBeatOutgoingSeconds

		heartBeatOutgoingSeconds = configuration.getInt(HEART_BEAT_OUTGOING_SECONDS, DEFAULT_HEART_BEAT_SECONDS);

		ValidationUtils.requireNonNegative(heartBeatOutgoingSeconds, HEART_BEAT_OUTGOING_SECONDS + " must be non-negative");

		// heartBeatIncomingSeconds

		heartBeatIncomingSeconds = configuration.getInt(HEART_BEAT_INCOMING_SECONDS, DEFAULT_HEART_BEAT_SECONDS);

		ValidationUtils.requireNonNegative(heartBeatIncomingSeconds, HEART_BEAT_INCOMING_SECONDS + " must be non-negative");

		// readTimeoutSeconds

		readTimeoutSeconds = configuration.getInt(READ_TIMEOUT_SECONDS, DEFAULT_READ_TIMEOUT_SECONDS);

		ValidationUtils.requireGreaterThan(readTimeoutSeconds, 0, READ_TIMEOUT_SECONDS + " must be greater than 0");

		// socketFactory

		socketFactory = tls.isEnabled() ? tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory() : SocketFactory.getDefault();

		// precomputed fields

		isDestinationTemplated = TemplateUtils.containsTemplate(destination);

		// connectHeaders

		connectHeaders = new HashMap<String, String>();

		if (ValidationUtils.isNotBlank(username)) {
			connectHeaders.put("login", username);
		}

		if (ValidationUtils.isNotBlank(password)) {
			connectHeaders.put("passcode", password);
		}

		connectHeaders.put("accept-version", "1.1,1.2");

		if (ValidationUtils.isNotBlank(virtualHost)) {
			connectHeaders.put("host", virtualHost);
		}

		connectHeaders.put("heart-beat", (heartBeatOutgoingSeconds * 1000) + "," + (heartBeatIncomingSeconds * 1000));
	}
}
