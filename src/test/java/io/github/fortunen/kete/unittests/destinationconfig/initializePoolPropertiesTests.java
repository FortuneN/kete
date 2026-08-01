 package io.github.fortunen.kete.unittests.destinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSession;

import io.github.fortunen.kete.DestinationConfig;

public class initializePoolPropertiesTests {

	@Test
	public void shouldReadDefaultPoolMinIdle() {

		// arrange

		var config = createConfigWithoutPoolProperties();
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolMinIdle()).isEqualTo(1);
	}

	@Test
	public void shouldReadDefaultPoolMaxIdle() {

		// arrange

		var config = createConfigWithoutPoolProperties();
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolMaxIdle()).isEqualTo(10);
	}

	@Test
	public void shouldReadDefaultPoolMaxTotal() {

		// arrange

		var config = createConfigWithoutPoolProperties();
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolMaxTotal()).isEqualTo(20);
	}

	@Test
	public void shouldReadDefaultPoolMaxWaitSeconds() {

		// arrange

		var config = createConfigWithoutPoolProperties();
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolMaxWaitSeconds()).isEqualTo(-1);
	}

	@Test
	public void shouldReadDefaultPoolBlockWhenExhausted() {

		// arrange

		var config = createConfigWithoutPoolProperties();
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.isPoolBlockWhenExhausted()).isTrue();
	}

	@Test
	public void shouldReadDefaultPoolLifo() {

		// arrange

		var config = createConfigWithoutPoolProperties();
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.isPoolLifo()).isTrue();
	}

	@Test
	public void shouldReadDefaultPoolFairness() {

		// arrange

		var config = createConfigWithoutPoolProperties();
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.isPoolFairness()).isFalse();
	}

	@Test
	public void shouldReadDefaultPoolTestOnCreate() {

		// arrange

		var config = createConfigWithoutPoolProperties();
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.isPoolTestOnCreate()).isTrue();
	}

	@Test
	public void shouldReadDefaultPoolTestOnBorrow() {

		// arrange

		var config = createConfigWithoutPoolProperties();
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.isPoolTestOnBorrow()).isTrue();
	}

	@Test
	public void shouldReadDefaultPoolTestOnReturn() {

		// arrange

		var config = createConfigWithoutPoolProperties();
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.isPoolTestOnReturn()).isTrue();
	}

	@Test
	public void shouldReadDefaultPoolTestWhileIdle() {

		// arrange

		var config = createConfigWithoutPoolProperties();
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.isPoolTestWhileIdle()).isTrue();
	}

	@Test
	public void shouldReadDefaultPoolTimeBetweenEvictionRunsSeconds() {

		// arrange

		var config = createConfigWithoutPoolProperties();
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolTimeBetweenEvictionRunsSeconds()).isEqualTo(60);
	}

	@Test
	public void shouldReadDefaultPoolMinEvictableIdleTimeSeconds() {

		// arrange

		var config = createConfigWithoutPoolProperties();
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolMinEvictableIdleTimeSeconds()).isEqualTo(-1);
	}

	@Test
	public void shouldReadDefaultPoolSoftMinEvictableIdleTimeSeconds() {

		// arrange

		var config = createConfigWithoutPoolProperties();
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolSoftMinEvictableIdleTimeSeconds()).isEqualTo(1800);
	}

	@Test
	public void shouldReadDefaultPoolNumTestsPerEvictionRun() {

		// arrange

		var config = createConfigWithoutPoolProperties();
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolNumTestsPerEvictionRun()).isEqualTo(3);
	}

	@Test
	public void shouldReadCustomPoolMinIdle() {

		// arrange

		var poolProps = new HashMap<String, Object>();
		poolProps.put("min-idle", 50);
		poolProps.put("max-total", 50); // max-total must be >= min-idle
		var config = createConfigWithPoolProperties(poolProps);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolMinIdle()).isEqualTo(50);
	}

	@Test
	public void shouldReadCustomPoolMaxIdle() {

		// arrange

		var config = createConfigWithPoolProperty("max-idle", 100);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolMaxIdle()).isEqualTo(100);
	}

	@Test
	public void shouldReadCustomPoolMaxTotal() {

		// arrange

		var config = createConfigWithPoolProperty("max-total", 200);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolMaxTotal()).isEqualTo(200);
	}

	@Test
	public void shouldReadCustomPoolMaxWaitSeconds() {

		// arrange

		var config = createConfigWithPoolProperty("max-wait-seconds", 5L);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolMaxWaitSeconds()).isEqualTo(5);
	}

	@Test
	public void shouldReadCustomPoolBlockWhenExhausted() {

		// arrange

		var config = createConfigWithPoolProperty("block-when-exhausted", false);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.isPoolBlockWhenExhausted()).isFalse();
	}

	@Test
	public void shouldReadCustomPoolLifo() {

		// arrange

		var config = createConfigWithPoolProperty("lifo", false);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.isPoolLifo()).isFalse();
	}

	@Test
	public void shouldReadCustomPoolFairness() {

		// arrange

		var config = createConfigWithPoolProperty("fairness", true);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.isPoolFairness()).isTrue();
	}

	@Test
	public void shouldReadCustomPoolTestOnCreate() {

		// arrange

		var config = createConfigWithPoolProperty("test-on-create", false);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.isPoolTestOnCreate()).isFalse();
	}

	@Test
	public void shouldReadCustomPoolTestOnBorrow() {

		// arrange

		var config = createConfigWithPoolProperty("test-on-borrow", false);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.isPoolTestOnBorrow()).isFalse();
	}

	@Test
	public void shouldReadCustomPoolTestOnReturn() {

		// arrange

		var config = createConfigWithPoolProperty("test-on-return", false);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.isPoolTestOnReturn()).isFalse();
	}

	@Test
	public void shouldReadCustomPoolTestWhileIdle() {

		// arrange

		var config = createConfigWithPoolProperty("test-while-idle", false);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.isPoolTestWhileIdle()).isFalse();
	}

	@Test
	public void shouldReadCustomPoolTimeBetweenEvictionRunsSeconds() {

		// arrange

		var config = createConfigWithPoolProperty("time-between-eviction-runs-seconds", 120L);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolTimeBetweenEvictionRunsSeconds()).isEqualTo(120);
	}

	@Test
	public void shouldReadCustomPoolMinEvictableIdleTimeSeconds() {

		// arrange

		var config = createConfigWithPoolProperty("min-evictable-idle-time-seconds", 300L);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolMinEvictableIdleTimeSeconds()).isEqualTo(300);
	}

	@Test
	public void shouldReadCustomPoolSoftMinEvictableIdleTimeSeconds() {

		// arrange

		var config = createConfigWithPoolProperty("soft-min-evictable-idle-time-seconds", 120L);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolSoftMinEvictableIdleTimeSeconds()).isEqualTo(120);
	}

	@Test
	public void shouldReadCustomPoolNumTestsPerEvictionRun() {

		// arrange

		var config = createConfigWithPoolProperty("num-tests-per-eviction-run", 10);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getPoolNumTestsPerEvictionRun()).isEqualTo(10);
	}

	@Test
	public void shouldReadAllCustomPoolProperties() {

		// arrange

		var poolProps = new HashMap<String, Object>();
		poolProps.put("min-idle", 50);
		poolProps.put("max-idle", 150);
		poolProps.put("max-total", 200);
	poolProps.put("max-wait-seconds", 5L);
	poolProps.put("block-when-exhausted", false);
	poolProps.put("lifo", false);
	poolProps.put("fairness", true);
	poolProps.put("test-on-create", true);
	poolProps.put("test-on-borrow", true);
	poolProps.put("test-on-return", true);
	poolProps.put("test-while-idle", true);
	poolProps.put("time-between-eviction-runs-seconds", 30L);
	poolProps.put("min-evictable-idle-time-seconds", 600L);
	poolProps.put("soft-min-evictable-idle-time-seconds", 300L);
	poolProps.put("num-tests-per-eviction-run", 5);

	var config = createConfigWithPoolProperties(poolProps);
	var destinationConfig = createTestDestinationConfig(config);

	// act

	destinationConfig.initialize();

	// assert

	assertThat(destinationConfig.getPoolMinIdle()).isEqualTo(50);
	assertThat(destinationConfig.getPoolMaxIdle()).isEqualTo(150);
	assertThat(destinationConfig.getPoolMaxTotal()).isEqualTo(200);
	assertThat(destinationConfig.getPoolMaxWaitSeconds()).isEqualTo(5);
	assertThat(destinationConfig.isPoolBlockWhenExhausted()).isFalse();
	assertThat(destinationConfig.isPoolLifo()).isFalse();
	assertThat(destinationConfig.isPoolFairness()).isTrue();
	assertThat(destinationConfig.isPoolTestOnCreate()).isTrue();
	assertThat(destinationConfig.isPoolTestOnBorrow()).isTrue();
	assertThat(destinationConfig.isPoolTestOnReturn()).isTrue();
	assertThat(destinationConfig.isPoolTestWhileIdle()).isTrue();
	assertThat(destinationConfig.getPoolTimeBetweenEvictionRunsSeconds()).isEqualTo(30);
	assertThat(destinationConfig.getPoolMinEvictableIdleTimeSeconds()).isEqualTo(600);
	assertThat(destinationConfig.getPoolSoftMinEvictableIdleTimeSeconds()).isEqualTo(300);
	}

	@Test
	public void shouldThrowWhenPoolMinIdleIsZero() {

		// arrange

		var config = createConfigWithPoolProperty("min-idle", 0);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		var thrown = catchThrowable(() -> destinationConfig.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("min-idle must be greater than 0");
	}

	@Test
	public void shouldThrowWhenPoolMinIdleIsNegative() {

		// arrange

		var config = createConfigWithPoolProperty("min-idle", -5);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		var thrown = catchThrowable(() -> destinationConfig.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("min-idle must be greater than 0");
	}

	@Test
	public void shouldThrowWhenPoolMaxIdleIsZero() {

		// arrange

		var config = createConfigWithPoolProperty("max-idle", 0);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		var thrown = catchThrowable(() -> destinationConfig.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("max-idle must be greater than 0");
	}

	@Test
	public void shouldThrowWhenPoolMaxIdleIsNegative() {

		// arrange

		var config = createConfigWithPoolProperty("max-idle", -5);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		var thrown = catchThrowable(() -> destinationConfig.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("max-idle must be greater than 0");
	}

	@Test
	public void shouldThrowWhenPoolMaxTotalIsZero() {

		// arrange

		var config = createConfigWithPoolProperty("max-total", 0);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		var thrown = catchThrowable(() -> destinationConfig.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("max-total must be greater than 0");
	}

	@Test
	public void shouldThrowWhenPoolMaxTotalIsNegative() {

		// arrange

		var config = createConfigWithPoolProperty("max-total", -5);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		var thrown = catchThrowable(() -> destinationConfig.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("max-total must be greater than 0");
	}

	@Test
	public void shouldThrowWhenPoolMaxTotalIsLessThanMinIdle() {

		// arrange

		var poolProps = new HashMap<String, Object>();
		poolProps.put("min-idle", 50);
		poolProps.put("max-total", 30);
		var config = createConfigWithPoolProperties(poolProps);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		var thrown = catchThrowable(() -> destinationConfig.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("max-total must be >= min-idle");
	}

	@Test
	public void shouldThrowWhenPoolMaxWaitSecondsIsLessThanMinusOne() {

		// arrange

		var config = createConfigWithPoolProperty("max-wait-seconds", -2L);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		var thrown = catchThrowable(() -> destinationConfig.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("max-wait-seconds must be -1 or greater");
	}

	@Test
	public void shouldThrowWhenPoolMaxWaitSecondsIsNegative() {

		// arrange

		var config = createConfigWithPoolProperty("max-wait-seconds", -1000L);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		var thrown = catchThrowable(() -> destinationConfig.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("max-wait-seconds must be -1 or greater");
	}

	// Helper methods

	private MapConfiguration createConfigWithoutPoolProperties() {

		var config = new HashMap<String, Object>();
		config.put("kind", "test");
		return new MapConfiguration(config);
	}

	private MapConfiguration createConfigWithPoolProperty(String key, Object value) {

		var config = new HashMap<String, Object>();
		config.put("kind", "test");
		config.put("pool." + key, value);
		return new MapConfiguration(config);
	}

	private MapConfiguration createConfigWithPoolProperties(Map<String, Object> poolProps) {

		var config = new HashMap<String, Object>();
		config.put("kind", "test");
		poolProps.forEach((key, value) -> config.put("pool." + key, value));
		return new MapConfiguration(config);
	}

	private DestinationConfig createTestDestinationConfig(MapConfiguration config) {

		var destinationConfig = new TestDestinationConfig();
		destinationConfig.setConfiguration(config);
		destinationConfig.setKeycloakSession(mock(KeycloakSession.class));
		return destinationConfig;
	}

	// Test implementation of abstract DestinationConfig

	private static class TestDestinationConfig extends DestinationConfig {

		@Override
		protected void doInitialize() {
			// No-op for testing
		}
	}
}
