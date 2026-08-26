package io.github.fortunen.kete.utils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import io.github.fortunen.kete.TlsMaterial;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.SslOptions;
import io.lettuce.core.cluster.ClusterClientOptions;

public final class RedisUtils {

	private RedisUtils() {}

	public static final String MODE_CLUSTER = "cluster";
	public static final String MODE_SENTINEL = "sentinel";
	public static final String MODE_STANDALONE = "standalone";
	public static final int DEFAULT_SENTINEL_PORT = 26379;

	// what every Redis destination needs to build its Lettuce URI and client options

	public record Settings(String host, int port, int database, boolean hasDatabase, String username, String password, String clientName, int commandTimeoutSeconds, int connectionTimeoutSeconds) {

		public Settings {
			ValidationUtils.requireNonNull(clientName, "clientName is required");
			ValidationUtils.requireNonNegative(commandTimeoutSeconds, "commandTimeoutSeconds must be non-negative");
			ValidationUtils.requireNonNegative(connectionTimeoutSeconds, "connectionTimeoutSeconds must be non-negative");
		}
	}

	public record ClientMaterial(RedisURI redisUri, ClientOptions clientOptions, List<RedisURI> clusterNodeUris, ClusterClientOptions clusterClientOptions) {}

	public static SslOptions createSslOptions(TlsMaterial tls) {

		ValidationUtils.requireNonNull(tls, "tls is required");

		if (!tls.isEnabled()) {
			return null;
		}

		return SslOptions.builder()
			.jdkSslProvider()
			.keyManager(tls.getKeyManagerFactory())
			.trustManager(tls.getTrustManagerFactory())
			.build();
	}

	public static ClientMaterial createStandalone(Settings settings, TlsMaterial tls) {

		ValidationUtils.requireNonNull(settings, "settings is required");
		ValidationUtils.requireNonNull(tls, "tls is required");
		ValidationUtils.requireNonBlank(settings.host(), "host is required for standalone mode");

		var uriBuilder = RedisURI.builder()
			.withHost(settings.host())
			.withPort(settings.port())
			.withClientName(settings.clientName());

		if (settings.hasDatabase()) {
			uriBuilder.withDatabase(settings.database());
		}

		applyCommonUriSettings(uriBuilder, settings, tls);

		return new ClientMaterial(uriBuilder.build(), createClientOptions(settings, tls), null, null);
	}

	public static ClientMaterial createSentinel(Settings settings, String sentinelMasterId, String sentinelNodes, TlsMaterial tls) {

		ValidationUtils.requireNonNull(settings, "settings is required");
		ValidationUtils.requireNonBlank(sentinelMasterId, "sentinelMasterId is required for sentinel mode");
		ValidationUtils.requireNonBlank(sentinelNodes, "sentinelNodes is required for sentinel mode");
		ValidationUtils.requireNonNull(tls, "tls is required");

		var nodes = sentinelNodes.split(",");
		var first = parseHostAndPort(nodes[0], DEFAULT_SENTINEL_PORT);
		var uriBuilder = RedisURI.Builder.sentinel(first.host(), first.port(), sentinelMasterId);

		for (var i = 1; i < nodes.length; i++) {
			var node = parseHostAndPort(nodes[i], DEFAULT_SENTINEL_PORT);
			uriBuilder.withSentinel(node.host(), node.port());
		}

		uriBuilder.withClientName(settings.clientName());

		if (settings.hasDatabase()) {
			uriBuilder.withDatabase(settings.database());
		}

		applyCommonUriSettings(uriBuilder, settings, tls);

		return new ClientMaterial(uriBuilder.build(), createClientOptions(settings, tls), null, null);
	}

	public static ClientMaterial createCluster(Settings settings, String clusterNodes, TlsMaterial tls) {

		ValidationUtils.requireNonNull(settings, "settings is required");
		ValidationUtils.requireNonBlank(clusterNodes, "clusterNodes is required for cluster mode");
		ValidationUtils.requireNonNull(tls, "tls is required");

		var nodeUris = new ArrayList<RedisURI>();

		for (var entry : clusterNodes.split(",")) {

			var node = parseHostAndPort(entry, settings.port());

			var nodeUriBuilder = RedisURI.builder()
				.withHost(node.host())
				.withPort(node.port())
				.withClientName(settings.clientName());

			applyCommonUriSettings(nodeUriBuilder, settings, tls);

			nodeUris.add(nodeUriBuilder.build());
		}

		// REJECT_COMMANDS: commands issued while disconnected fail fast instead of buffering until the timeout

		var optionsBuilder = ClusterClientOptions.builder().autoReconnect(true).disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS);

		if (settings.connectionTimeoutSeconds() > 0) {
			optionsBuilder.socketOptions(SocketOptions.builder().connectTimeout(Duration.ofSeconds(settings.connectionTimeoutSeconds())).build());
		}

		var sslOptions = createSslOptions(tls);

		if (ValidationUtils.isNotNull(sslOptions)) {
			optionsBuilder.sslOptions(sslOptions);
		}

		return new ClientMaterial(null, null, nodeUris, optionsBuilder.build());
	}

	// command timeout, authentication and TLS are the same for every mode; the Lettuce default
	// command timeout (60 s) would turn a dead connection into a minute-long stall per send

	private static void applyCommonUriSettings(RedisURI.Builder uriBuilder, Settings settings, TlsMaterial tls) {

		uriBuilder.withTimeout(Duration.ofSeconds(settings.commandTimeoutSeconds()));

		if (ValidationUtils.isNotBlank(settings.password())) {
			if (ValidationUtils.isNotBlank(settings.username())) {
				uriBuilder.withAuthentication(settings.username(), settings.password().toCharArray());
			} else {
				uriBuilder.withPassword(settings.password().toCharArray());
			}
		}

		if (tls.isEnabled()) {
			uriBuilder.withSsl(true);
			uriBuilder.withVerifyPeer(tls.isVerifyHostname());
		}
	}

	// REJECT_COMMANDS: commands issued while disconnected fail fast instead of buffering until
	// the timeout (route retry and pool replacement handle the failure)

	private static ClientOptions createClientOptions(Settings settings, TlsMaterial tls) {

		var optionsBuilder = ClientOptions.builder().autoReconnect(true).disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS);

		if (settings.connectionTimeoutSeconds() > 0) {
			optionsBuilder.socketOptions(SocketOptions.builder().connectTimeout(Duration.ofSeconds(settings.connectionTimeoutSeconds())).build());
		}

		var sslOptions = createSslOptions(tls);

		if (ValidationUtils.isNotNull(sslOptions)) {
			optionsBuilder.sslOptions(sslOptions);
		}

		return optionsBuilder.build();
	}

	private record HostAndPort(String host, int port) {}

	private static HostAndPort parseHostAndPort(String entry, int defaultPort) {

		var parts = entry.trim().split(":");

		return new HostAndPort(parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : defaultPort);
	}
}
