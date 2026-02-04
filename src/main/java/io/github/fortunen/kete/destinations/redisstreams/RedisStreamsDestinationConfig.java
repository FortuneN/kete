package io.github.fortunen.kete.destinations.redisstreams;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SslOptions;
import io.lettuce.core.TimeoutOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.time.Duration;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class RedisStreamsDestinationConfig extends DestinationConfig {

	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final int DEFAULT_PORT = 6379;
	public static final int DEFAULT_TLS_PORT = 6380;

	public static final String STREAM = "stream";

	public static final String MAX_LEN = "max-len";
	public static final long DEFAULT_MAX_LEN = 0;

	public static final boolean DEFAULT_APPROXIMATE_TRIMMING = true;
	public static final String APPROXIMATE_TRIMMING = "approximate-trimming";

	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String DATABASE = "database";
	public static final int DEFAULT_DATABASE = 0;

	public static final int DEFAULT_CONNECTION_TIMEOUT_SECONDS = 10;
	public static final String CONNECTION_TIMEOUT_SECONDS = "connection-timeout-seconds";

	public static final int DEFAULT_COMMAND_TIMEOUT_SECONDS = 60;
	public static final String COMMAND_TIMEOUT_SECONDS = "command-timeout-seconds";

	public static final String CLIENT_NAME = "client-name";

	private int port;
	private String host;
	private long maxLen;
	private int database;
	private String stream;
	private String username;
	private String password;
	private String clientName;
	private RedisURI redisUri;
	private int commandTimeoutSeconds;
	private boolean approximateTrimming;
	private ClientOptions clientOptions;
	private int connectionTimeoutSeconds;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// host

		host = ValidationUtils.requireNonBlank(configuration.getString(HOST, "").trim(), HOST + " is required");

		// stream

		stream = ValidationUtils.requireNonBlank(configuration.getString(STREAM, "").trim(), STREAM + " is required");

		// port

		port = configuration.getInt(PORT, tls.isEnabled() ? DEFAULT_TLS_PORT : DEFAULT_PORT);

		ValidationUtils.requireValidPort(port, PORT);

		// maxLen

		maxLen = ValidationUtils.requireNonNegative(configuration.getLong(MAX_LEN, DEFAULT_MAX_LEN), MAX_LEN + " must be non-negative");

		// approximateTrimming

		approximateTrimming = configuration.getBoolean(APPROXIMATE_TRIMMING, DEFAULT_APPROXIMATE_TRIMMING);

		// database

		database = ValidationUtils.requireNonNegative(configuration.getInt(DATABASE, DEFAULT_DATABASE), DATABASE + " must be non-negative");

		// username

		username = configuration.getString(USERNAME, "").trim();

		// password

		password = configuration.getString(PASSWORD, "").trim();

		// clientName

		clientName = configuration.getString(CLIENT_NAME, Constants.ID).trim();

		// connectionTimeoutSeconds

		connectionTimeoutSeconds = ValidationUtils.requireNonNegative(configuration.getInt(CONNECTION_TIMEOUT_SECONDS, DEFAULT_CONNECTION_TIMEOUT_SECONDS), CONNECTION_TIMEOUT_SECONDS + " must be non-negative");

		// commandTimeoutSeconds

		commandTimeoutSeconds = ValidationUtils.requireNonNegative(configuration.getInt(COMMAND_TIMEOUT_SECONDS, DEFAULT_COMMAND_TIMEOUT_SECONDS), COMMAND_TIMEOUT_SECONDS + " must be non-negative");

		// redisUri

		var uriBuilder = RedisURI.builder()
			.withHost(host)
			.withPort(port)
			.withDatabase(database)
			.withClientName(clientName)
			.withTimeout(Duration.ofSeconds(commandTimeoutSeconds));

		if (ValidationUtils.isNotBlank(password)) {
			if (ValidationUtils.isNotBlank(username)) {
				uriBuilder.withAuthentication(username, password.toCharArray());
			} else {
				uriBuilder.withPassword(password.toCharArray());
			}
		}

		if (tls.isEnabled()) {
			uriBuilder.withSsl(true);
		}

		redisUri = uriBuilder.build();

		// clientOptions

		var clientOptionsBuilder = ClientOptions.builder()
			.autoReconnect(true)
			.timeoutOptions(TimeoutOptions.builder().fixedTimeout(Duration.ofSeconds(connectionTimeoutSeconds)).build());

		if (tls.isEnabled()) {

			var sslOptions = SslOptions.builder()
				.jdkSslProvider()
				.trustManager(tls.getTrustManagerFactory())
				.keyManager(tls.getKeyManagerFactory())
				.build();

			clientOptionsBuilder.sslOptions(sslOptions);
		}

		clientOptions = clientOptionsBuilder.build();
	}
}
