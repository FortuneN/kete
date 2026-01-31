package io.github.fortunen.kete.destinations.websocket;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.utils.ConfigurationUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class WebSocketDestinationConfig extends DestinationConfig {

	public static final String URL = "url";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final int DEFAULT_WS_PORT = 80;
	public static final int DEFAULT_WSS_PORT = 443;
	public static final String PATH = "path";

	public static final String BINARY_MODE = "binary-mode";
	public static final String HEADERS = "headers";

	public static final int DEFAULT_CONNECTION_TIMEOUT = 10;
	public static final String CONNECTION_TIMEOUT = "connection-timeout";

	public static final int DEFAULT_CONNECTION_LOST_TIMEOUT = 60;
	public static final String CONNECTION_LOST_TIMEOUT = "connection-lost-timeout";

	private int port;
	private String url;
	private String host;
	private String path;
	private String scheme;
	private boolean binaryMode;
	private int connectionTimeout;
	private int connectionLostTimeout;
	private SocketFactory socketFactory;
	private TimeUnit connectionTimeoutUnit = TimeUnit.SECONDS;
	private Map<String, String> headers = new HashMap<>();

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// url

		var urlFromConfig = configuration.getString(URL, "").trim();

		if (ValidationUtils.isNotBlank(urlFromConfig)) {

			url = urlFromConfig;

			// parse scheme from url

			if (url.startsWith("wss://")) {

				scheme = "wss";

				if (!tls.isEnabled()) {
					var tlsConfig = ConfigurationUtils.getSubSet(configuration, TLS);
					tlsConfig.getMap().put("enabled", "true");
					tls = TlsMaterial.builder().withConfiguration(tlsConfig).build();
				}

			} else if (url.startsWith("ws://")) {
				scheme = "ws";
			} else {
				throw new IllegalStateException("url must start with 'ws://' or 'wss://'");
			}

		} else {

			// host

			host = ValidationUtils.requireNonBlank(configuration.getString(HOST, "").trim(), HOST + " is required");

			// scheme

			scheme = tls.isEnabled() ? "wss" : "ws";

			// port

			port = configuration.getInt(PORT, tls.isEnabled() ? DEFAULT_WSS_PORT : DEFAULT_WS_PORT);

			ValidationUtils.requireValidPort(port, PORT);

			// path

			path = configuration.getString(PATH, "/").trim();

			if (!path.startsWith("/")) {
				path = "/" + path;
			}

			// build url

			url = scheme + "://" + host + ":" + port + path;
		}

		// binaryMode

		binaryMode = configuration.getBoolean(BINARY_MODE, false);

		// connectionTimeout

		connectionTimeout = configuration.getInt(CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);

		ValidationUtils.requireGreaterThan(connectionTimeout, 0, CONNECTION_TIMEOUT + " must be greater than 0");

		// connectionLostTimeout

		connectionLostTimeout = configuration.getInt(CONNECTION_LOST_TIMEOUT, DEFAULT_CONNECTION_LOST_TIMEOUT);

		ValidationUtils.requireNonNegative(connectionLostTimeout, CONNECTION_LOST_TIMEOUT + " must be non-negative");

		// headers

		var headersConfig = ConfigurationUtils.getSubSet(configuration, HEADERS);
		var keysIterator = headersConfig.getKeys();

		while (keysIterator.hasNext()) {
			var key = keysIterator.next();
			headers.put(key, headersConfig.getString(key));
		}

		// socketFactory

		socketFactory = tls.isEnabled() ? tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory() : null;
	}
}
