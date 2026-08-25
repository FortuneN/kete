package io.github.fortunen.kete;

import java.time.Duration;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.keycloak.models.KeycloakSession;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.github.fortunen.kete.utils.MatcherUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.github.resilience4j.retry.Retry;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@NoArgsConstructor(force = true)
public class Route implements AutoCloseable {

	public static final int ACCEPT_CACHE_MAX_SIZE = 1000;

	private Retry retry;
	private String name;
	private Serializer serializer;
	private String serializerKind;
	private String destinationKind;
	private DestinationConfig destinationConfig;
	private Matcher[] realmMatchers = new Matcher[0];
	private Matcher[] eventMatchers = new Matcher[0];
	private MatchMode realmMatchMode = MatchMode.DEFAULT;
	private MatchMode eventMatchMode = MatchMode.DEFAULT;
	private GenericObjectPool<Destination<?>> destinationPool;
	private Cache<String, Boolean> acceptRealmCache = CacheBuilder.newBuilder().maximumSize(ACCEPT_CACHE_MAX_SIZE).build();
	private Cache<String, Boolean> acceptEventCache = CacheBuilder.newBuilder().maximumSize(ACCEPT_CACHE_MAX_SIZE).build();

	public void initialize(KeycloakSession session) {

		initializeConfig(session);
		initializeDestinations();
	}

	public void initializeConfig(KeycloakSession session) {

		ValidationUtils.requireNonNull(session, "session is required");
		ValidationUtils.requireNonNull(serializer, "serializer is required");
		ValidationUtils.requireNonNull(realmMatchers, "realmMatchers is required");
		ValidationUtils.requireNonNull(eventMatchers, "eventMatchers is required");
		ValidationUtils.requireNonNull(destinationConfig, "destinationConfig is required");

		// initialize destination config (may read or write the Keycloak model, so it stays on the caller's thread)

		destinationConfig.setKeycloakSession(session);
		destinationConfig.initialize();

		// destinationKind

		destinationKind = destinationConfig.getDestinationKind();
	}

	@SneakyThrows
	public void initializeDestinations() {

		ValidationUtils.requireNonNull(destinationConfig, "destinationConfig is required");

		// create destination pool (only if not already set - for testing)

		if (ValidationUtils.isNull(destinationPool)) {

			var poolConfig = new GenericObjectPoolConfig<Destination<?>>();

			poolConfig.setLifo(destinationConfig.isPoolLifo());
			poolConfig.setMinIdle(destinationConfig.getPoolMinIdle());
			poolConfig.setMaxIdle(destinationConfig.getPoolMaxIdle());
			poolConfig.setFairness(destinationConfig.isPoolFairness());
			poolConfig.setMaxTotal(destinationConfig.getPoolMaxTotal());
			poolConfig.setTestOnCreate(destinationConfig.isPoolTestOnCreate());
			poolConfig.setTestOnBorrow(destinationConfig.isPoolTestOnBorrow());
			poolConfig.setTestOnReturn(destinationConfig.isPoolTestOnReturn());
			poolConfig.setTestWhileIdle(destinationConfig.isPoolTestWhileIdle());
			poolConfig.setBlockWhenExhausted(destinationConfig.isPoolBlockWhenExhausted());
			poolConfig.setMaxWait(Duration.ofSeconds(destinationConfig.getPoolMaxWaitSeconds()));
			poolConfig.setNumTestsPerEvictionRun(destinationConfig.getPoolNumTestsPerEvictionRun());
			poolConfig.setMinEvictableIdleDuration(Duration.ofSeconds(destinationConfig.getPoolMinEvictableIdleTimeSeconds()));
			poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(destinationConfig.getPoolTimeBetweenEvictionRunsSeconds()));
			poolConfig.setSoftMinEvictableIdleDuration(Duration.ofSeconds(destinationConfig.getPoolSoftMinEvictableIdleTimeSeconds()));

			var poolFactory = new DestinationPooledObjectFactory();

			poolFactory.setDestinationConfig(destinationConfig);

			destinationPool = new GenericObjectPool<>(poolFactory, poolConfig);
		}

		// test pool by borrowing and returning

		Destination<?> testDestination = null;

		try {

			testDestination = destinationPool.borrowObject();
			destinationPool.returnObject(testDestination);

		} catch (Exception exception) {

			if (ValidationUtils.isNotNull(testDestination)) {
				try {
					destinationPool.invalidateObject(testDestination);
				} catch (Exception invalidateException) {
					log.debug("Failed to invalidate test destination", invalidateException);
				}
			}

			throw exception;
		}
	}

	@SneakyThrows
	public boolean acceptRealm(String realm) {

		if (ValidationUtils.isBlank(realm)) {
			return false;
		}

		if (ValidationUtils.isNullOrEmpty(realmMatchers)) {
			return true;
		}

		return acceptRealmCache.get(realm, () -> MatcherUtils.matchWithMode(realm, realmMatchers, realmMatchMode));
	}

	@SneakyThrows
	public boolean acceptEvent(String eventType) {

		if (ValidationUtils.isBlank(eventType)) {
			return false;
		}

		if (ValidationUtils.isNullOrEmpty(eventMatchers)) {
			return true;
		}

		return acceptEventCache.get(eventType, () -> MatcherUtils.matchWithMode(eventType, eventMatchers, eventMatchMode));
	}

	@SneakyThrows
	public void send(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		if (ValidationUtils.isNotNull(retry)) {

			retry.executeCallable(() -> {
				doSend(message);
				return null;
			});

		} else {

			doSend(message);

		}
	}

	@SneakyThrows
	private void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");
		ValidationUtils.requireNonNull(destinationPool, "destinationPool is required");

		Destination<?> destination = null;

		try {

			destination = destinationPool.borrowObject();
			destination.send(message);
			destinationPool.returnObject(destination);

		} catch (Exception exception) {

			if (ValidationUtils.isNotNull(destination)) {
				try {
					destinationPool.invalidateObject(destination);
				} catch (Exception invalidateException) {
					log.debug("Failed to invalidate destination", invalidateException);
				}
			}

			throw exception;
		}
	}

	@Override
	public void close() {
		if (ValidationUtils.isNotNull(destinationPool)) {
			try {
				destinationPool.close();
			} catch (Exception exception) {
				log.warn("Failed to close destination pool for route: " + name, exception);
			}
		}
	}
}
