package io.github.fortunen.kete.unittests.destinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSession;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.DestinationConfig;

public class initializeHeadersTests {

	// =========================================================================
	// Custom Headers Parsing
	// =========================================================================

	@Test
	public void shouldParseCustomHeaders() {

		// arrange

		var config = createConfigWithHeaders(Map.of(
			"headers.X-Custom-Header", "custom-value",
			"headers.Authorization", "Bearer token123"
		));

		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getCustomHeaders())
			.containsEntry("X-Custom-Header", "custom-value")
			.containsEntry("Authorization", "Bearer token123")
			.hasSize(2);
	}

	@Test
	public void shouldTrimHeaderKeys() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "test");
		configMap.put("headers.  X-Padded-Key  ", "value");

		var destinationConfig = createTestDestinationConfig(new MapConfiguration(configMap));

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getCustomHeaders())
			.containsKey("X-Padded-Key")
			.doesNotContainKey("  X-Padded-Key  ");
	}

	@Test
	public void shouldTrimHeaderValues() {

		// arrange

		var config = createConfigWithHeaders(Map.of(
			"headers.Trimmed", "  value with spaces  "
		));

		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getCustomHeaders())
			.containsEntry("Trimmed", "value with spaces");
	}

	@Test
	public void shouldIgnoreEmptyHeaders() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "test");
		configMap.put("headers.Empty-Header", "");
		configMap.put("headers.Valid-Header", "value");

		var destinationConfig = createTestDestinationConfig(new MapConfiguration(configMap));

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getCustomHeaders())
			.doesNotContainKey("Empty-Header")
			.containsEntry("Valid-Header", "value");
	}

	@Test
	public void shouldIgnoreBlankHeaders() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "test");
		configMap.put("headers.Blank-Header", "   ");
		configMap.put("headers.Valid-Header", "value");

		var destinationConfig = createTestDestinationConfig(new MapConfiguration(configMap));

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getCustomHeaders())
			.doesNotContainKey("Blank-Header")
			.containsEntry("Valid-Header", "value");
	}

	@Test
	public void shouldHaveEmptyHeadersByDefault() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test"));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getCustomHeaders()).isEmpty();
	}

	// =========================================================================
	// Custom Headers Entry Set
	// =========================================================================

	@Test
	public void shouldPopulateCustomHeadersEntrySet() {

		// arrange

		var config = createConfigWithHeaders(Map.of(
			"headers.X-Header-1", "value1",
			"headers.X-Header-2", "value2"
		));

		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getCustomHeadersEntrySet())
			.isNotNull()
			.hasSize(2);

		var keys = destinationConfig.getCustomHeadersEntrySet().stream()
			.map(Map.Entry::getKey)
			.toList();

		assertThat(keys).containsExactlyInAnyOrder("X-Header-1", "X-Header-2");
	}

	@Test
	public void shouldHaveEmptyCustomHeadersEntrySetWhenNoHeaders() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test"));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getCustomHeadersEntrySet())
			.isNotNull()
			.isEmpty();
	}

	// =========================================================================
	// Reserved Header Key Filtering
	// =========================================================================

	@Test
	public void shouldFilterOutEventKindReservedHeader() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "test");
		configMap.put("headers." + Constants.MESSAGE_HEADER_EVENT_KIND, "should-be-ignored");
		configMap.put("headers.X-Custom", "allowed");

		var destinationConfig = createTestDestinationConfig(new MapConfiguration(configMap));

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getCustomHeaders())
			.doesNotContainKey(Constants.MESSAGE_HEADER_EVENT_KIND)
			.containsEntry("X-Custom", "allowed")
			.hasSize(1);
	}

	@Test
	public void shouldFilterOutEventTypeReservedHeader() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "test");
		configMap.put("headers." + Constants.MESSAGE_HEADER_EVENT_TYPE, "should-be-ignored");
		configMap.put("headers.X-Custom", "allowed");

		var destinationConfig = createTestDestinationConfig(new MapConfiguration(configMap));

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getCustomHeaders())
			.doesNotContainKey(Constants.MESSAGE_HEADER_EVENT_TYPE)
			.containsEntry("X-Custom", "allowed")
			.hasSize(1);
	}

	@Test
	public void shouldFilterOutContentTypeReservedHeader() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "test");
		configMap.put("headers." + Constants.MESSAGE_HEADER_CONTENT_TYPE, "should-be-ignored");
		configMap.put("headers.X-Custom", "allowed");

		var destinationConfig = createTestDestinationConfig(new MapConfiguration(configMap));

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getCustomHeaders())
			.doesNotContainKey(Constants.MESSAGE_HEADER_CONTENT_TYPE)
			.containsEntry("X-Custom", "allowed")
			.hasSize(1);
	}

	@Test
	public void shouldFilterOutAllReservedHeaders() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "test");
		configMap.put("headers." + Constants.MESSAGE_HEADER_EVENT_KIND, "ignored1");
		configMap.put("headers." + Constants.MESSAGE_HEADER_EVENT_TYPE, "ignored2");
		configMap.put("headers." + Constants.MESSAGE_HEADER_CONTENT_TYPE, "ignored3");
		configMap.put("headers.X-Custom-1", "allowed1");
		configMap.put("headers.X-Custom-2", "allowed2");

		var destinationConfig = createTestDestinationConfig(new MapConfiguration(configMap));

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getCustomHeaders())
			.doesNotContainKey(Constants.MESSAGE_HEADER_EVENT_KIND)
			.doesNotContainKey(Constants.MESSAGE_HEADER_EVENT_TYPE)
			.doesNotContainKey(Constants.MESSAGE_HEADER_CONTENT_TYPE)
			.containsEntry("X-Custom-1", "allowed1")
			.containsEntry("X-Custom-2", "allowed2")
			.hasSize(2);
	}

	@Test
	public void shouldNotFilterSimilarButDifferentHeaderNames() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "test");
		configMap.put("headers.eventkind-custom", "allowed1");
		configMap.put("headers.my-eventtype", "allowed2");
		configMap.put("headers.x-contenttype", "allowed3");

		var destinationConfig = createTestDestinationConfig(new MapConfiguration(configMap));

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getCustomHeaders())
			.containsEntry("eventkind-custom", "allowed1")
			.containsEntry("my-eventtype", "allowed2")
			.containsEntry("x-contenttype", "allowed3")
			.hasSize(3);
	}

	@Test
	public void shouldBeCaseSensitiveForReservedHeaders() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "test");
		configMap.put("headers.EVENTKIND", "allowed1");
		configMap.put("headers.EventType", "allowed2");
		configMap.put("headers.ContentType", "allowed3");
		configMap.put("headers." + Constants.MESSAGE_HEADER_EVENT_KIND, "filtered");

		var destinationConfig = createTestDestinationConfig(new MapConfiguration(configMap));

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getCustomHeaders())
			.containsEntry("EVENTKIND", "allowed1")
			.containsEntry("EventType", "allowed2")
			.containsEntry("ContentType", "allowed3")
			.doesNotContainKey(Constants.MESSAGE_HEADER_EVENT_KIND)
			.hasSize(3);
	}

	// Helper methods

	private MapConfiguration createConfigWithHeaders(Map<String, Object> headers) {

		var config = new HashMap<String, Object>();
		config.put("kind", "test");
		config.putAll(headers);
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
