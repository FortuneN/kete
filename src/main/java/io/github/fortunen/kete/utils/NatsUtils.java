package io.github.fortunen.kete.utils;

import java.time.Duration;
import java.util.Arrays;

import io.github.fortunen.kete.TlsMaterial;
import io.nats.client.Options;

public final class NatsUtils {

	private NatsUtils() {}

	public static String[] parseServers(String servers) {

		ValidationUtils.requireNonBlank(servers, "servers is required");

		return Arrays.stream(servers.split(",")).map(String::trim).filter(ValidationUtils::isNotBlank).toArray(String[]::new);
	}

	// reconnectBufferSize(0): publishes during a reconnect fail fast instead of silently
	// buffering into the client (route retry and pool replacement handle the failure)

	public static Options.Builder createOptionsBuilder(String[] servers, int connectionTimeoutSeconds, int pingIntervalSeconds, String connectionName, TlsMaterial tls) {

		ValidationUtils.requireNonNull(servers, "servers is required");
		ValidationUtils.requireNonNegative(connectionTimeoutSeconds, "connectionTimeoutSeconds must be non-negative");
		ValidationUtils.requireNonNegative(pingIntervalSeconds, "pingIntervalSeconds must be non-negative");
		ValidationUtils.requireNonBlank(connectionName, "connectionName is required");
		ValidationUtils.requireNonNull(tls, "tls is required");

		var builder = new Options.Builder()
			.servers(servers)
			.connectionTimeout(Duration.ofSeconds(connectionTimeoutSeconds))
			.pingInterval(Duration.ofSeconds(pingIntervalSeconds))
			.connectionName(connectionName)
			.maxReconnects(-1)
			.reconnectBufferSize(0);

		if (tls.isEnabled()) {
			builder.sslContext(tls.isVerifyHostname() ? tls.getHostnameVerifyingSSLContext() : tls.getKeyStoreAndTrustStoreSSLContext());
		}

		return builder;
	}
}
