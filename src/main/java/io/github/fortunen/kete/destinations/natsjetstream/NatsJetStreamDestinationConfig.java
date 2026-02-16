package io.github.fortunen.kete.destinations.natsjetstream;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.NatsAuthMaterial;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.nats.client.Options;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.Arrays;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class NatsJetStreamDestinationConfig extends DestinationConfig {

	public static final String STREAM = "stream";
	public static final String SERVERS = "servers";
	public static final String SUBJECT = "subject";
	public static final int DEFAULT_PING_INTERVAL_SECONDS = 60;
	public static final int DEFAULT_PUBLISH_TIMEOUT_SECONDS = 10;
	public static final String CONNECTION_NAME = "connection-name";
	public static final int DEFAULT_CONNECTION_TIMEOUT_SECONDS = 10;
	public static final String PING_INTERVAL_SECONDS = "ping-interval-seconds";
	public static final String PUBLISH_TIMEOUT_SECONDS = "publish-timeout-seconds";
	public static final String CONNECTION_TIMEOUT_SECONDS = "connection-timeout-seconds";

	private String stream;
	private String subject;
	private String[] servers;
	private Options natsOptions;
	private String connectionName;
	private int pingIntervalSeconds;
	private Duration publishTimeout;
	private int publishTimeoutSeconds;
	private boolean isSubjectTemplated;
	private int connectionTimeoutSeconds;
	private NatsAuthMaterial authMaterial;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// servers

		servers = Arrays.stream(ValidationUtils.requireNonBlank(configuration.getString(SERVERS, "").trim(), SERVERS + " is required").split(",")).map(String::trim).filter(ValidationUtils::isNotBlank).toArray(String[]::new);

		// subject

		subject = ValidationUtils.requireNonBlank(configuration.getString(SUBJECT, "").trim(), SUBJECT + " is required");

		// connectionTimeoutSeconds

		connectionTimeoutSeconds = ValidationUtils.requireNonNegative(configuration.getInt(CONNECTION_TIMEOUT_SECONDS, DEFAULT_CONNECTION_TIMEOUT_SECONDS), CONNECTION_TIMEOUT_SECONDS + " must be non-negative");

		// pingIntervalSeconds

		pingIntervalSeconds = ValidationUtils.requireNonNegative(configuration.getInt(PING_INTERVAL_SECONDS, DEFAULT_PING_INTERVAL_SECONDS), PING_INTERVAL_SECONDS + " must be non-negative");

		// connectionName

		connectionName = configuration.getString(CONNECTION_NAME, Constants.ID).trim();

		// stream

		stream = ValidationUtils.requireNonBlank(configuration.getString(STREAM, "").trim(), STREAM + " is required for JetStream destination");

		// publishTimeoutSeconds

		publishTimeoutSeconds = ValidationUtils.requireNonNegative(configuration.getInt(PUBLISH_TIMEOUT_SECONDS, DEFAULT_PUBLISH_TIMEOUT_SECONDS), PUBLISH_TIMEOUT_SECONDS + " must be non-negative");

		// publishTimeout

		publishTimeout = Duration.ofSeconds(publishTimeoutSeconds);

		// authMaterial

		authMaterial = new NatsAuthMaterial();
		authMaterial.initialize(configuration);

		// precomputed fields

		isSubjectTemplated = TemplateUtils.containsTemplate(subject);

		// natsOptions

		var builder = new Options.Builder()
			.servers(servers)
			.connectionTimeout(Duration.ofSeconds(connectionTimeoutSeconds))
			.pingInterval(Duration.ofSeconds(pingIntervalSeconds))
			.connectionName(connectionName)
			.maxReconnects(-1);

		if (tls.isEnabled()) {
			builder.sslContext(tls.isVerifyHostname() ? tls.getHostnameVerifyingSSLContext() : tls.getKeyStoreAndTrustStoreSSLContext());
		}

		authMaterial.applyTo(builder);

		natsOptions = builder.build();
	}
}
