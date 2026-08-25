package io.github.fortunen.kete;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.MapConfiguration;
import org.keycloak.models.KeycloakSession;

import io.github.fortunen.kete.utils.ConfigurationUtils;
import io.github.fortunen.kete.utils.IocUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@NoArgsConstructor(force = true)
@ToString(exclude = {"keycloakSession", "configuration", "customHeaders", "customHeadersEntrySet"})
@EqualsAndHashCode(callSuper = false, exclude = "keycloakSession")
public abstract class DestinationConfig {

	public static final String TLS = "tls";
	public static final String POOL = "pool";
	public static final String POOL_LIFO = "lifo";
	public static final String HEADERS = "headers";
	public static final String CONTENT_ENCODING = "content-encoding";
	public static final String CONTENT_TRANSFER_ENCODING = "content-transfer-encoding";
	public static final String POOL_MIN_IDLE = "min-idle";
	public static final String POOL_MAX_IDLE = "max-idle";
	public static final String POOL_FAIRNESS = "fairness";
	public static final String POOL_MAX_TOTAL = "max-total";
	public static final String POOL_TEST_ON_CREATE = "test-on-create";
	public static final String POOL_TEST_ON_BORROW = "test-on-borrow";
	public static final String POOL_TEST_ON_RETURN = "test-on-return";
	public static final String POOL_TEST_WHILE_IDLE = "test-while-idle";
	public static final String POOL_MAX_WAIT_SECONDS = "max-wait-seconds";
	public static final String AUTHENTICATION_TYPE = "authentication-type";
	public static final String POOL_BLOCK_WHEN_EXHAUSTED = "block-when-exhausted";
	public static final String POOL_NUM_TESTS_PER_EVICTION_RUN = "num-tests-per-eviction-run";
	public static final String POOL_MIN_EVICTABLE_IDLE_TIME_SECONDS = "min-evictable-idle-time-seconds";
	public static final String POOL_TIME_BETWEEN_EVICTION_RUNS_SECONDS = "time-between-eviction-runs-seconds";
	public static final String POOL_SOFT_MIN_EVICTABLE_IDLE_TIME_SECONDS = "soft-min-evictable-idle-time-seconds";

	public static final int DEFAULT_POOL_MIN_IDLE = 1;
	public static final int DEFAULT_POOL_MAX_IDLE = 10;
	public static final int DEFAULT_POOL_MAX_TOTAL = 20;
	public static final boolean DEFAULT_POOL_LIFO = true;
	public static final boolean DEFAULT_POOL_FAIRNESS = false;
	public static final long DEFAULT_POOL_MAX_WAIT_SECONDS = 30;
	public static final boolean DEFAULT_POOL_TEST_ON_CREATE = true;
	public static final boolean DEFAULT_POOL_TEST_ON_BORROW = true;
	public static final boolean DEFAULT_POOL_TEST_ON_RETURN = true;
	public static final boolean DEFAULT_POOL_TEST_WHILE_IDLE = true;
	public static final int DEFAULT_POOL_NUM_TESTS_PER_EVICTION_RUN = 3;
	public static final boolean DEFAULT_POOL_BLOCK_WHEN_EXHAUSTED = true;
	public static final long DEFAULT_POOL_MIN_EVICTABLE_IDLE_TIME_SECONDS = -1;
	public static final long DEFAULT_POOL_TIME_BETWEEN_EVICTION_RUNS_SECONDS = 60;
	public static final long DEFAULT_POOL_SOFT_MIN_EVICTABLE_IDLE_TIME_SECONDS = 1800;

	protected TlsMaterial tls;
	protected String keycloakRealm;
	protected String destinationKind;
	protected String authenticationType;
	protected boolean hasAuthenticationType;
	protected MapConfiguration configuration;
	protected ContentEncoding contentEncoding;
	protected String contentEncodingName;
	protected ContentTransferEncoding contentTransferEncoding;
	protected String contentTransferEncodingName;
	protected boolean poolLifo = DEFAULT_POOL_LIFO;
	protected int poolMinIdle = DEFAULT_POOL_MIN_IDLE;
	protected int poolMaxIdle = DEFAULT_POOL_MAX_IDLE;
	protected int poolMaxTotal = DEFAULT_POOL_MAX_TOTAL;
	protected boolean poolFairness = DEFAULT_POOL_FAIRNESS;
	protected Map<String, String> customHeaders = new HashMap<>();
	protected Set<Map.Entry<String, String>> customHeadersEntrySet;
	protected boolean poolTestOnCreate = DEFAULT_POOL_TEST_ON_CREATE;
	protected boolean poolTestOnBorrow = DEFAULT_POOL_TEST_ON_BORROW;
	protected boolean poolTestOnReturn = DEFAULT_POOL_TEST_ON_RETURN;
	protected long poolMaxWaitSeconds = DEFAULT_POOL_MAX_WAIT_SECONDS;
	protected boolean poolTestWhileIdle = DEFAULT_POOL_TEST_WHILE_IDLE;
	protected boolean poolBlockWhenExhausted = DEFAULT_POOL_BLOCK_WHEN_EXHAUSTED;
	protected int poolNumTestsPerEvictionRun = DEFAULT_POOL_NUM_TESTS_PER_EVICTION_RUN;
	protected long poolMinEvictableIdleTimeSeconds = DEFAULT_POOL_MIN_EVICTABLE_IDLE_TIME_SECONDS;
	protected long poolTimeBetweenEvictionRunsSeconds = DEFAULT_POOL_TIME_BETWEEN_EVICTION_RUNS_SECONDS;
	protected long poolSoftMinEvictableIdleTimeSeconds = DEFAULT_POOL_SOFT_MIN_EVICTABLE_IDLE_TIME_SECONDS;

	@Setter
	protected KeycloakSession keycloakSession;

	protected abstract void doInitialize();

	public final void initialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// destinationKind

		destinationKind = ValidationUtils.requireNonBlank(configuration.getString(Constants.KIND, "").trim(), Constants.KIND + " is required");

		// pool configuration (optional)

		var poolConfig = ConfigurationUtils.getSubSet(configuration, POOL);

		poolLifo = poolConfig.getBoolean(POOL_LIFO, DEFAULT_POOL_LIFO);
		poolMinIdle = poolConfig.getInt(POOL_MIN_IDLE, DEFAULT_POOL_MIN_IDLE);
		poolMaxIdle = poolConfig.getInt(POOL_MAX_IDLE, DEFAULT_POOL_MAX_IDLE);
		poolMaxTotal = poolConfig.getInt(POOL_MAX_TOTAL, DEFAULT_POOL_MAX_TOTAL);
		poolFairness = poolConfig.getBoolean(POOL_FAIRNESS, DEFAULT_POOL_FAIRNESS);
		poolTestOnCreate = poolConfig.getBoolean(POOL_TEST_ON_CREATE, DEFAULT_POOL_TEST_ON_CREATE);
		poolTestOnBorrow = poolConfig.getBoolean(POOL_TEST_ON_BORROW, DEFAULT_POOL_TEST_ON_BORROW);
		poolTestOnReturn = poolConfig.getBoolean(POOL_TEST_ON_RETURN, DEFAULT_POOL_TEST_ON_RETURN);
		poolMaxWaitSeconds = poolConfig.getLong(POOL_MAX_WAIT_SECONDS, DEFAULT_POOL_MAX_WAIT_SECONDS);
		poolTestWhileIdle = poolConfig.getBoolean(POOL_TEST_WHILE_IDLE, DEFAULT_POOL_TEST_WHILE_IDLE);
		poolBlockWhenExhausted = poolConfig.getBoolean(POOL_BLOCK_WHEN_EXHAUSTED, DEFAULT_POOL_BLOCK_WHEN_EXHAUSTED);
		poolNumTestsPerEvictionRun = poolConfig.getInt(POOL_NUM_TESTS_PER_EVICTION_RUN, DEFAULT_POOL_NUM_TESTS_PER_EVICTION_RUN);
		poolMinEvictableIdleTimeSeconds = poolConfig.getLong(POOL_MIN_EVICTABLE_IDLE_TIME_SECONDS, DEFAULT_POOL_MIN_EVICTABLE_IDLE_TIME_SECONDS);
		poolTimeBetweenEvictionRunsSeconds = poolConfig.getLong(POOL_TIME_BETWEEN_EVICTION_RUNS_SECONDS, DEFAULT_POOL_TIME_BETWEEN_EVICTION_RUNS_SECONDS);
		poolSoftMinEvictableIdleTimeSeconds = poolConfig.getLong(POOL_SOFT_MIN_EVICTABLE_IDLE_TIME_SECONDS, DEFAULT_POOL_SOFT_MIN_EVICTABLE_IDLE_TIME_SECONDS);

		ValidationUtils.requireGreaterThan(poolMinIdle, 0, POOL_MIN_IDLE + " must be greater than 0");
		ValidationUtils.requireGreaterThan(poolMaxIdle, 0, POOL_MAX_IDLE + " must be greater than 0");
		ValidationUtils.requireGreaterThan(poolMaxTotal, 0, POOL_MAX_TOTAL + " must be greater than 0");
		ValidationUtils.requireTrue(poolMaxTotal >= poolMinIdle, POOL_MAX_TOTAL + " must be >= " + POOL_MIN_IDLE);
		ValidationUtils.requireGreaterThan(poolMaxWaitSeconds, -2, POOL_MAX_WAIT_SECONDS + " must be -1 or greater");

		// tls

		tls = TlsMaterial.builder()
			.withConfiguration(ConfigurationUtils.getSubSet(configuration, TLS))
			.build();

		// authentication-type

		authenticationType = configuration.getString(AUTHENTICATION_TYPE, "").trim().toLowerCase();
		hasAuthenticationType = ValidationUtils.isNotBlank(authenticationType);

		// headers (filter out reserved message header keys - message headers always take priority)

		var headers = ConfigurationUtils.getSubSet(configuration, HEADERS);

		headers.getKeys().forEachRemaining(key -> {

			key = key.trim();

			if (Constants.MESSAGE_HEADER_EVENT_KIND.equals(key) || Constants.MESSAGE_HEADER_EVENT_TYPE.equals(key) || Constants.MESSAGE_HEADER_CONTENT_TYPE.equals(key)) {
				return;
			}

			var value = headers.getString(key, "").trim();

			if (ValidationUtils.isNotBlank(value)) {
				customHeaders.put(key, value);
			}
		});

		customHeadersEntrySet = customHeaders.entrySet();

		// content-transfer-encoding (optional)

		var cteValue = configuration.getString(CONTENT_TRANSFER_ENCODING, "").trim().toLowerCase();

		if (ValidationUtils.isNotBlank(cteValue)) {
			contentTransferEncoding = IocUtils.get(cteValue, ContentTransferEncoding.class);
			ValidationUtils.requireNonNull(contentTransferEncoding, "unknown " + CONTENT_TRANSFER_ENCODING + ": " + cteValue);
			contentTransferEncodingName = cteValue;
		}

		// content-encoding (optional)

		var ceValue = configuration.getString(CONTENT_ENCODING, "").trim().toLowerCase();

		if (ValidationUtils.isNotBlank(ceValue)) {
			contentEncoding = IocUtils.get(ceValue, ContentEncoding.class);
			ValidationUtils.requireNonNull(contentEncoding, "unknown " + CONTENT_ENCODING + ": " + ceValue);
			contentEncodingName = ceValue;
		}

		// initialize subclass

		doInitialize();
	}
}
