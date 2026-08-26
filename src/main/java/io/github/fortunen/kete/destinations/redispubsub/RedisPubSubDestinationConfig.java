package io.github.fortunen.kete.destinations.redispubsub;

import io.github.fortunen.kete.utils.RedisUtils;
import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.SneakyThrows;

import java.util.List;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"password"})
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
	public static final int DEFAULT_COMMAND_TIMEOUT_SECONDS = 10;
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

		// mode-specific initialization

		var settings = new RedisUtils.Settings(host, port, database, configuration.containsKey(DATABASE), username, password, clientName, commandTimeoutSeconds, connectionTimeoutSeconds);

		var material = switch (mode) {
			case RedisUtils.MODE_STANDALONE -> {
				ValidationUtils.requireNonBlank(host, HOST + " is required for standalone mode");
				yield RedisUtils.createStandalone(settings, tls);
			}
			case RedisUtils.MODE_SENTINEL -> RedisUtils.createSentinel(settings,
				ValidationUtils.requireNonBlank(configuration.getString(SENTINEL_MASTER_ID, "").trim(), SENTINEL_MASTER_ID + " is required for sentinel mode"),
				ValidationUtils.requireNonBlank(configuration.getString(SENTINEL_NODES, "").trim(), SENTINEL_NODES + " is required for sentinel mode"),
				tls);
			case RedisUtils.MODE_CLUSTER -> RedisUtils.createCluster(settings,
				ValidationUtils.requireNonBlank(configuration.getString(CLUSTER_NODES, "").trim(), CLUSTER_NODES + " is required for cluster mode"),
				tls);
			default -> throw new IllegalStateException("unsupported " + MODE + ": '" + mode + "' — valid options: standalone, sentinel, cluster");
		};

		redisUri = material.redisUri();
		clientOptions = material.clientOptions();
		clusterNodeUris = material.clusterNodeUris();
		clusterClientOptions = material.clusterClientOptions();
	}
}
