package io.github.fortunen.kete.destinations.websocket;

import java.net.URI;
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
	public static final String PATH = "path";
	public static final int DEFAULT_WS_PORT = 80;
	public static final int DEFAULT_WSS_PORT = 443;

	public static final String BINARY_MODE = "binary-mode";

	public static final int DEFAULT_CONNECTION_TIMEOUT_SECONDS = 10;
	public static final String CONNECTION_TIMEOUT_SECONDS = "connection-timeout-seconds";

	public static final int DEFAULT_CONNECTION_LOST_TIMEOUT_SECONDS = 60;
	public static final String CONNECTION_LOST_TIMEOUT_SECONDS = "connection-lost-timeout-seconds";

	private int port;
	private String url;
	private String host;
	private String path;
	private String scheme;
	private boolean binaryMode;
	private SocketFactory socketFactory;
	private int connectionTimeoutSeconds;
	private int connectionLostTimeoutSeconds;
	private TimeUnit connectionTimeoutUnit = TimeUnit.SECONDS;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// url

		var urlFromConfig = configuration.getString(URL, "").trim();

		if (ValidationUtils.isNotBlank(urlFromConfig)) {

			url = urlFromConfig;

			var parsedUri = URI.create(url);

			// host

			host = ValidationUtils.requireNonBlank(parsedUri.getHost(), "url must contain a valid host");

			// scheme

			if (url.startsWith("wss://")) {

				scheme = "wss";

				// port

				port = parsedUri.getPort() > 0 ? parsedUri.getPort() : DEFAULT_WSS_PORT;

				if (!tls.isEnabled()) {
					var tlsConfig = ConfigurationUtils.getSubSet(configuration, TLS);
					tlsConfig.getMap().put("enabled", "true");
					tls = TlsMaterial.builder().withConfiguration(tlsConfig).build();
				}

			} else if (url.startsWith("ws://")) {

				scheme = "ws";

				// port

				port = parsedUri.getPort() > 0 ? parsedUri.getPort() : DEFAULT_WS_PORT;

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

			// url

			url = scheme + "://" + host + ":" + port + path;
		}

		// binaryMode

		binaryMode = configuration.getBoolean(BINARY_MODE, false);

		// connectionTimeoutSeconds

		connectionTimeoutSeconds = configuration.getInt(CONNECTION_TIMEOUT_SECONDS, DEFAULT_CONNECTION_TIMEOUT_SECONDS);

		ValidationUtils.requireGreaterThan(connectionTimeoutSeconds, 0, CONNECTION_TIMEOUT_SECONDS + " must be greater than 0");

		// connectionLostTimeoutSeconds

		connectionLostTimeoutSeconds = configuration.getInt(CONNECTION_LOST_TIMEOUT_SECONDS, DEFAULT_CONNECTION_LOST_TIMEOUT_SECONDS);

		ValidationUtils.requireNonNegative(connectionLostTimeoutSeconds, CONNECTION_LOST_TIMEOUT_SECONDS + " must be non-negative");

		// socketFactory

		socketFactory = tls.isEnabled() ? tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory() : null;
	}
}
