package io.github.fortunen.kete.destinations.redispubsub;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SslOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class RedisPubSubDestinationConfig extends DestinationConfig {

	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String MODE = "mode";
	public static final int DEFAULT_PORT = 6379;
	public static final int DEFAULT_DATABASE = 0;
	public static final String CHANNEL = "channel";
	public static final int DEFAULT_TLS_PORT = 6380;
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String DATABASE = "database";
	public static final String CLIENT_NAME = "client-name";
	public static final String CLUSTER_NODES = "cluster-nodes";
	public static final String SENTINEL_NODES = "sentinel-nodes";
	public static final String DEFAULT_CLIENT_NAME = "kete";
	public static final String SENTINEL_MASTER_ID = "sentinel-master-id";
	public static final int DEFAULT_COMMAND_TIMEOUT_SECONDS = 60;
	public static final int DEFAULT_CONNECTION_TIMEOUT_SECONDS = 10;
	public static final String COMMAND_TIMEOUT_SECONDS = "command-timeout-seconds";
	public static final String CONNECTION_TIMEOUT_SECONDS = "connection-timeout-seconds";

	private int port;
	private String host;
	private String mode;
	private int database;
	private String channel;
	private String username;
	private String password;
	private String clientName;
	private RedisURI redisUri;
	private boolean isClusterMode;
	private int commandTimeoutSeconds;
	private boolean isChannelTemplated;
	private ClientOptions clientOptions;
	private List<RedisURI> clusterNodeUris;
	private int connectionTimeoutSeconds;
	private ClusterClientOptions clusterClientOptions;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// mode (standalone, sentinel, cluster — default: standalone)

		mode = configuration.getString(MODE, "standalone").trim().toLowerCase();
		isClusterMode = mode.equals("cluster");

		// host (required for standalone, optional for sentinel/cluster)

		host = configuration.getString(HOST, "").trim();

		// channel

		channel = ValidationUtils.requireNonBlank(configuration.getString(CHANNEL, "").trim(), CHANNEL + " is required");

		// port

		port = configuration.getInt(PORT, tls.isEnabled() ? DEFAULT_TLS_PORT : DEFAULT_PORT);

		ValidationUtils.requireValidPort(port, PORT);

		// database

		database = ValidationUtils.requireNonNegative(configuration.getInt(DATABASE, DEFAULT_DATABASE), DATABASE + " must be non-negative");

		// username

		username = configuration.getString(USERNAME, "").trim();

		// password

		password = configuration.getString(PASSWORD, "").trim();

		// clientName

		clientName = configuration.getString(CLIENT_NAME, DEFAULT_CLIENT_NAME).trim();

		// connectionTimeoutSeconds

		connectionTimeoutSeconds = ValidationUtils.requireNonNegative(configuration.getInt(CONNECTION_TIMEOUT_SECONDS, DEFAULT_CONNECTION_TIMEOUT_SECONDS), CONNECTION_TIMEOUT_SECONDS + " must be non-negative");

		// commandTimeoutSeconds

		commandTimeoutSeconds = ValidationUtils.requireNonNegative(configuration.getInt(COMMAND_TIMEOUT_SECONDS, DEFAULT_COMMAND_TIMEOUT_SECONDS), COMMAND_TIMEOUT_SECONDS + " must be non-negative");

		// precomputed fields

		isChannelTemplated = TemplateUtils.containsTemplate(channel);

		// SSL options (shared across modes)

		SslOptions sslOptions = null;
		if (tls.isEnabled()) {
			sslOptions = SslOptions.builder()
				.jdkSslProvider()
				.keyManager(tls.getKeyManagerFactory())
				.trustManager(tls.getTrustManagerFactory())
				.build();
		}

		// mode-specific initialization

		switch (mode) {
			case "standalone" -> {
				ValidationUtils.requireNonBlank(host, HOST + " is required for standalone mode");

				var uriBuilder = RedisURI.builder()
					.withHost(host)
					.withPort(port)
					.withClientName(clientName);

				if (configuration.containsKey(DATABASE)) {
					uriBuilder.withDatabase(database);
				}
				if (configuration.containsKey(COMMAND_TIMEOUT_SECONDS)) {
					uriBuilder.withTimeout(Duration.ofSeconds(commandTimeoutSeconds));
				}
				if (ValidationUtils.isNotBlank(password)) {
					if (ValidationUtils.isNotBlank(username)) {
						uriBuilder.withAuthentication(username, password.toCharArray());
					} else {
						uriBuilder.withPassword(password.toCharArray());
					}
				}
				if (tls.isEnabled()) {
					uriBuilder.withSsl(true);
					uriBuilder.withVerifyPeer(tls.isVerifyHostname());
				}

				redisUri = uriBuilder.build();

				// REJECT_COMMANDS: commands issued while disconnected fail fast instead of
				// buffering until the timeout (route retry and pool replacement handle it)

				var clientOptionsBuilder = ClientOptions.builder().autoReconnect(true).disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS);
				if (configuration.containsKey(CONNECTION_TIMEOUT_SECONDS)) {
					clientOptionsBuilder.timeoutOptions(TimeoutOptions.builder().fixedTimeout(Duration.ofSeconds(connectionTimeoutSeconds)).build());
				}
				if (sslOptions != null) {
					clientOptionsBuilder.sslOptions(sslOptions);
				}
				clientOptions = clientOptionsBuilder.build();
			}
			case "sentinel" -> {
				var sentinelMasterId = ValidationUtils.requireNonBlank(configuration.getString(SENTINEL_MASTER_ID, "").trim(), SENTINEL_MASTER_ID + " is required for sentinel mode");
				var sentinelNodesStr = ValidationUtils.requireNonBlank(configuration.getString(SENTINEL_NODES, "").trim(), SENTINEL_NODES + " is required for sentinel mode");

				var sentinelNodes = sentinelNodesStr.split(",");
				var firstNode = sentinelNodes[0].trim().split(":");
				var sentinelHost = firstNode[0];
				var sentinelPort = firstNode.length > 1 ? Integer.parseInt(firstNode[1]) : 26379;

				var uriBuilder = RedisURI.Builder.sentinel(sentinelHost, sentinelPort, sentinelMasterId);

				for (var i = 1; i < sentinelNodes.length; i++) {
					var parts = sentinelNodes[i].trim().split(":");
					var sHost = parts[0];
					var sPort = parts.length > 1 ? Integer.parseInt(parts[1]) : 26379;
					uriBuilder.withSentinel(sHost, sPort);
				}

				uriBuilder.withClientName(clientName);
				if (configuration.containsKey(DATABASE)) {
					uriBuilder.withDatabase(database);
				}
				if (configuration.containsKey(COMMAND_TIMEOUT_SECONDS)) {
					uriBuilder.withTimeout(Duration.ofSeconds(commandTimeoutSeconds));
				}
				if (ValidationUtils.isNotBlank(password)) {
					if (ValidationUtils.isNotBlank(username)) {
						uriBuilder.withAuthentication(username, password.toCharArray());
					} else {
						uriBuilder.withPassword(password.toCharArray());
					}
				}
				if (tls.isEnabled()) {
					uriBuilder.withSsl(true);
					uriBuilder.withVerifyPeer(tls.isVerifyHostname());
				}

				redisUri = uriBuilder.build();

				// REJECT_COMMANDS: commands issued while disconnected fail fast instead of
				// buffering until the timeout (route retry and pool replacement handle it)

				var clientOptionsBuilder = ClientOptions.builder().autoReconnect(true).disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS);
				if (configuration.containsKey(CONNECTION_TIMEOUT_SECONDS)) {
					clientOptionsBuilder.timeoutOptions(TimeoutOptions.builder().fixedTimeout(Duration.ofSeconds(connectionTimeoutSeconds)).build());
				}
				if (sslOptions != null) {
					clientOptionsBuilder.sslOptions(sslOptions);
				}
				clientOptions = clientOptionsBuilder.build();
			}
			case "cluster" -> {
				var clusterNodesStr = ValidationUtils.requireNonBlank(configuration.getString(CLUSTER_NODES, "").trim(), CLUSTER_NODES + " is required for cluster mode");
				var nodeEntries = clusterNodesStr.split(",");

				clusterNodeUris = new ArrayList<>();
				for (var entry : nodeEntries) {
					var parts = entry.trim().split(":");
					var nodeHost = parts[0];
					var nodePort = parts.length > 1 ? Integer.parseInt(parts[1]) : port;

					var nodeUriBuilder = RedisURI.builder()
						.withHost(nodeHost)
						.withPort(nodePort)
						.withClientName(clientName);

					if (ValidationUtils.isNotBlank(password)) {
						if (ValidationUtils.isNotBlank(username)) {
							nodeUriBuilder.withAuthentication(username, password.toCharArray());
						} else {
							nodeUriBuilder.withPassword(password.toCharArray());
						}
					}
					if (tls.isEnabled()) {
						nodeUriBuilder.withSsl(true);
						nodeUriBuilder.withVerifyPeer(tls.isVerifyHostname());
					}
					clusterNodeUris.add(nodeUriBuilder.build());
				}

				// REJECT_COMMANDS: commands issued while disconnected fail fast instead of
				// buffering until the timeout (route retry and pool replacement handle it)

				var clusterOptionsBuilder = ClusterClientOptions.builder().autoReconnect(true).disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS);
				if (configuration.containsKey(CONNECTION_TIMEOUT_SECONDS)) {
					clusterOptionsBuilder.timeoutOptions(TimeoutOptions.builder().fixedTimeout(Duration.ofSeconds(connectionTimeoutSeconds)).build());
				}
				if (sslOptions != null) {
					clusterOptionsBuilder.sslOptions(sslOptions);
				}
				clusterClientOptions = clusterOptionsBuilder.build();
			}
			default -> throw new IllegalStateException("unsupported " + MODE + ": '" + mode + "' — valid options: standalone, sentinel, cluster");
		}
	}
}
