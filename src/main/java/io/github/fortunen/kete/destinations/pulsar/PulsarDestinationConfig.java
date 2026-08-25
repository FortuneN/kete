package io.github.fortunen.kete.destinations.pulsar;

import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.impl.auth.AuthenticationBasic;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.OAuthMaterial;
import io.github.fortunen.kete.utils.ConfigurationUtils;
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
@ToString(callSuper = true, exclude = {"token", "password"})
public class PulsarDestinationConfig extends DestinationConfig {

	public static final String TOPIC = "topic";
	public static final String TOKEN = "token";
	public static final String OAUTH = "oauth";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String SERVICE_URL = "service-url";
	public static final int DEFAULT_SEND_TIMEOUT_SECONDS = 30;
	public static final String PRODUCER_NAME = "producer-name";
	public static final String LISTENER_NAME = "listener-name";
	public static final String DEFAULT_COMPRESSION_TYPE = "LZ4";
	public static final int DEFAULT_MAX_PENDING_MESSAGES = 1_000;
	public static final int DEFAULT_BATCHING_MAX_MESSAGES = 1_000;
	public static final boolean DEFAULT_BLOCK_IF_QUEUE_FULL = true;
	public static final int DEFAULT_OPERATION_TIMEOUT_SECONDS = 30;
	public static final int DEFAULT_CONNECTION_TIMEOUT_SECONDS = 10;
	public static final String COMPRESSION_TYPE = "compression-type";
	public static final int DEFAULT_KEEP_ALIVE_INTERVAL_SECONDS = 30;
	public static final String BLOCK_IF_QUEUE_FULL = "block-if-queue-full";
	public static final long DEFAULT_BATCHING_MAX_PUBLISH_DELAY_SECONDS = 1;
	public static final String MAX_PENDING_MESSAGES = "max-pending-messages";
	public static final String SEND_TIMEOUT_SECONDS = "send-timeout-seconds";
	public static final String BATCHING_MAX_MESSAGES = "batching-max-messages";
	public static final String OPERATION_TIMEOUT_SECONDS = "operation-timeout-seconds";
	public static final String CONNECTION_TIMEOUT_SECONDS = "connection-timeout-seconds";
	public static final String KEEP_ALIVE_INTERVAL_SECONDS = "keep-alive-interval-seconds";
	public static final String BATCHING_MAX_PUBLISH_DELAY_SECONDS = "batching-max-publish-delay-seconds";

	private String topic;
	private String token;
	private String username;
	private String password;
	private String serviceUrl;
	private String producerName;
	private String listenerName;
	private OAuthMaterial oauthMaterial;
	private int sendTimeoutSeconds;
	private int maxPendingMessages;
	private boolean hasProducerName;
	private boolean hasSendTimeout;
	private int batchingMaxMessages;
	private boolean hasBatchingMaxMessages;
	private boolean batchingEnabled;
	private boolean hasBatchingMaxPublishDelay;
	private boolean isTopicTemplated;
	private boolean blockIfQueueFull;
	private int operationTimeoutSeconds;
	private ClientBuilder clientBuilder;
	private int connectionTimeoutSeconds;
	private long batchingMaxPublishDelay;
	private int keepAliveIntervalSeconds;
	private CompressionType compressionType;
	private TimeUnit batchingMaxPublishDelayUnit;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// serviceUrl

		serviceUrl = ValidationUtils.requireNonBlank(configuration.getString(SERVICE_URL, "").trim(), SERVICE_URL + " is required");

		// topic

		topic = ValidationUtils.requireNonBlank(configuration.getString(TOPIC, "").trim(), TOPIC + " is required");

		// compressionType

		var compressionTypeStr = configuration.getString(COMPRESSION_TYPE, DEFAULT_COMPRESSION_TYPE).trim().toUpperCase();

		try {
			compressionType = CompressionType.valueOf(compressionTypeStr);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(COMPRESSION_TYPE + " must be one of: NONE, LZ4, ZLIB, ZSTD, SNAPPY");
		}

		// batchingMaxPublishDelay

		var batchingMaxPublishDelaySeconds = ValidationUtils.requireGreaterThan(configuration.getLong(BATCHING_MAX_PUBLISH_DELAY_SECONDS, DEFAULT_BATCHING_MAX_PUBLISH_DELAY_SECONDS), 0, BATCHING_MAX_PUBLISH_DELAY_SECONDS + " must be greater than 0");
		batchingMaxPublishDelay = batchingMaxPublishDelaySeconds * 1000;
		batchingMaxPublishDelayUnit = TimeUnit.MILLISECONDS;
		hasBatchingMaxPublishDelay = configuration.containsKey(BATCHING_MAX_PUBLISH_DELAY_SECONDS);

		// batchingMaxMessages

		if (configuration.containsKey(BATCHING_MAX_MESSAGES)) {
			batchingMaxMessages = ValidationUtils.requireGreaterThan(configuration.getInt(BATCHING_MAX_MESSAGES, DEFAULT_BATCHING_MAX_MESSAGES), 0, BATCHING_MAX_MESSAGES + " must be greater than 0");
			hasBatchingMaxMessages = true;
		}

		// batching is opt-in: sends are synchronous, so the batch timer only adds latency unless batching was asked for

		batchingEnabled = hasBatchingMaxPublishDelay || hasBatchingMaxMessages;

		// blockIfQueueFull

		blockIfQueueFull = configuration.getBoolean(BLOCK_IF_QUEUE_FULL, DEFAULT_BLOCK_IF_QUEUE_FULL);

		// maxPendingMessages

		maxPendingMessages = ValidationUtils.requireGreaterThan(configuration.getInt(MAX_PENDING_MESSAGES, DEFAULT_MAX_PENDING_MESSAGES), 0, MAX_PENDING_MESSAGES + " must be greater than 0");

		// authentication

		if (hasAuthenticationType) {
			switch (authenticationType) {
				case "token" -> {
					token = ValidationUtils.requireNonBlank(configuration.getString(TOKEN, "").trim(), TOKEN + " is required when authentication-type is 'token'");
				}
				case "basic" -> {
					username = ValidationUtils.requireNonBlank(configuration.getString(USERNAME, "").trim(), USERNAME + " is required when authentication-type is 'basic'");
					password = configuration.getString(PASSWORD, "").trim();
				}
				case "oauth" -> {
					oauthMaterial = OAuthMaterial.builder().withKeycloakRealm(keycloakRealm).withKeycloakSession(keycloakSession).withConfiguration(ConfigurationUtils.getSubSet(configuration, OAUTH)).build();
					ValidationUtils.requireTrue(ValidationUtils.isNotNull(oauthMaterial) && oauthMaterial.isEnabled(), "oauth configuration is required when authentication-type is 'oauth'");
				}
				default -> throw new IllegalStateException(AUTHENTICATION_TYPE + " must be one of: token, basic, oauth");
			}
		}

		// timeouts

		if (configuration.containsKey(SEND_TIMEOUT_SECONDS)) {
			sendTimeoutSeconds = ValidationUtils.requireNonNegative(configuration.getInt(SEND_TIMEOUT_SECONDS, DEFAULT_SEND_TIMEOUT_SECONDS), SEND_TIMEOUT_SECONDS + " must be non-negative");
			hasSendTimeout = true;
		}

		operationTimeoutSeconds = ValidationUtils.requireNonNegative(configuration.getInt(OPERATION_TIMEOUT_SECONDS, DEFAULT_OPERATION_TIMEOUT_SECONDS), OPERATION_TIMEOUT_SECONDS + " must be non-negative");
		connectionTimeoutSeconds = ValidationUtils.requireNonNegative(configuration.getInt(CONNECTION_TIMEOUT_SECONDS, DEFAULT_CONNECTION_TIMEOUT_SECONDS), CONNECTION_TIMEOUT_SECONDS + " must be non-negative");
		keepAliveIntervalSeconds = ValidationUtils.requireNonNegative(configuration.getInt(KEEP_ALIVE_INTERVAL_SECONDS, DEFAULT_KEEP_ALIVE_INTERVAL_SECONDS), KEEP_ALIVE_INTERVAL_SECONDS + " must be non-negative");

		// optional settings

		producerName = configuration.getString(PRODUCER_NAME, "").trim();
		listenerName = configuration.getString(LISTENER_NAME, "").trim();

		// precomputed fields

		isTopicTemplated = TemplateUtils.containsTemplate(topic);
		hasProducerName = ValidationUtils.isNotBlank(producerName);

		// clientBuilder

		clientBuilder = PulsarClient.builder()
			.serviceUrl(serviceUrl);

		if (configuration.containsKey(OPERATION_TIMEOUT_SECONDS)) {
			clientBuilder.operationTimeout(operationTimeoutSeconds, TimeUnit.SECONDS);
		}

		if (configuration.containsKey(CONNECTION_TIMEOUT_SECONDS)) {
			clientBuilder.connectionTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS);
		}

		if (configuration.containsKey(KEEP_ALIVE_INTERVAL_SECONDS)) {
			clientBuilder.keepAliveInterval(keepAliveIntervalSeconds, TimeUnit.SECONDS);
		}

		// listenerName (for multi-listener clusters)

		if (ValidationUtils.isNotBlank(listenerName)) {
			clientBuilder.listenerName(listenerName);
		}

		// authentication

		if (ValidationUtils.isNotNull(oauthMaterial)) {
			clientBuilder.authentication(AuthenticationFactory.token(() -> oauthMaterial.getAccessToken().getValue()));
		} else if (ValidationUtils.isNotBlank(token)) {
			clientBuilder.authentication(AuthenticationFactory.token(token));
		} else if (ValidationUtils.isNotBlank(username)) {
			var basicAuth = new AuthenticationBasic();
			basicAuth.configure("{\"userId\":\"" + username + "\",\"password\":\"" + password + "\"}");
			clientBuilder.authentication(basicAuth);
		}

		// tls

		if (tls.isEnabled()) {

			// hostname verification

			clientBuilder.enableTlsHostnameVerification(tls.isVerifyHostname());

			// use JKS-based TLS (is required even for just truststore-only TLS)

			clientBuilder.useKeyStoreTls(true);

			// truststore

			clientBuilder.tlsTrustStoreType(tls.getTrustStoreType());
			clientBuilder.tlsTrustStorePath(tls.getTrustStoreFilePath());
			clientBuilder.tlsTrustStorePassword(tls.getTrustStorePassword());

			// keystore (only for mTLS - when key-store was explicitly configured)

			if (tls.isKeyStoreConfigured()) {
				clientBuilder.tlsKeyStoreType(tls.getKeyStoreType());
				clientBuilder.tlsKeyStorePath(tls.getKeyStoreFilePath());
				clientBuilder.tlsKeyStorePassword(tls.getKeyStorePassword());
			}
		}
	}
}
