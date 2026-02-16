package io.github.fortunen.kete.unittests.serializers.urlencodedformserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.serializers.UrlEncodedFormSerializer;

public class initializeTests {

	@Test
	public void shouldDefaultToBracketNotation() {

		// arrange

		var serializer = new UrlEncodedFormSerializer();
		var map = new HashMap<String, Object>();
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		serializer.initialize();

		// assert

		assertThat(serializer.getNestingNotation()).isEqualTo("bracket");
	}

	@Test
	public void shouldDefaultWhenConfigurationIsNull() {

		// arrange

		var serializer = new UrlEncodedFormSerializer();

		// act

		serializer.initialize();

		// assert

		assertThat(serializer.getNestingNotation()).isEqualTo("bracket");
	}

	@Test
	public void shouldAcceptBracketNotation() {

		// arrange

		var serializer = new UrlEncodedFormSerializer();
		var map = new HashMap<String, Object>();
		map.put("nesting-notation", "bracket");
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		serializer.initialize();

		// assert

		assertThat(serializer.getNestingNotation()).isEqualTo("bracket");
	}

	@Test
	public void shouldAcceptDotNotation() {

		// arrange

		var serializer = new UrlEncodedFormSerializer();
		var map = new HashMap<String, Object>();
		map.put("nesting-notation", "dot");
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		serializer.initialize();

		// assert

		assertThat(serializer.getNestingNotation()).isEqualTo("dot");
	}

	@Test
	public void shouldThrowWhenNestingNotationIsInvalid() {

		// arrange

		var serializer = new UrlEncodedFormSerializer();
		var map = new HashMap<String, Object>();
		map.put("nesting-notation", "invalid");
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> serializer.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown).hasMessageContaining("nesting-notation must be 'bracket' or 'dot'");
	}

	@Test
	public void shouldTrimAndLowercaseNestingNotation() {

		// arrange

		var serializer = new UrlEncodedFormSerializer();
		var map = new HashMap<String, Object>();
		map.put("nesting-notation", "  DOT  ");
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		serializer.initialize();

		// assert

		assertThat(serializer.getNestingNotation()).isEqualTo("dot");
	}
}
