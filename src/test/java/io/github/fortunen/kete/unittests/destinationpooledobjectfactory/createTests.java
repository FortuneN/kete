package io.github.fortunen.kete.unittests.destinationpooledobjectfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.DestinationPooledObjectFactory;
import io.github.fortunen.kete.utils.DestinationUtils;

public class createTests {

	@Test
	public void shouldThrowWhenDestinationConfigIsNull() {

		// arrange

		var factory = new DestinationPooledObjectFactory();
		factory.setDestinationConfig(null);

		// act

		var thrown = catchThrowable(() -> factory.create());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	public void shouldThrowWhenDestinationKindIsNull() {

		// arrange

		var config = mock(DestinationConfig.class);
		when(config.getDestinationKind()).thenReturn(null);

		var factory = new DestinationPooledObjectFactory();
		factory.setDestinationConfig(config);

		// act

		var thrown = catchThrowable(() -> factory.create());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("key is required");
	}

	@Test
	public void shouldThrowWhenDestinationKindIsUnknown() {

		// arrange

		var config = mock(DestinationConfig.class);
		when(config.getDestinationKind()).thenReturn("unknown-destination");

		var factory = new DestinationPooledObjectFactory();
		factory.setDestinationConfig(config);

		// act

		var thrown = catchThrowable(() -> factory.create());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("destination kind 'unknown-destination' not found");
	}

	@Test
	public void shouldCreateDestinationWithConfig() {

		// arrange

		var config = mock(DestinationConfig.class);
		when(config.getDestinationKind()).thenReturn("http");

		var destination = mock(Destination.class);
		doNothing().when(destination).initialize();

		var factory = new DestinationPooledObjectFactory();
		factory.setDestinationConfig(config);

		try (var mockedDestinationUtils = mockStatic(DestinationUtils.class)) {

			mockedDestinationUtils.when(() -> DestinationUtils.createDestination(config))
				.thenReturn(destination);

			// act

			var result = factory.create();

			// assert

			assertThat(result).isNotNull();
			assertThat(result).isSameAs(destination);
			verify(destination).initialize();
		}
	}

	@Test
	public void shouldCallInitializeOnDestination() {

		// arrange

		var config = mock(DestinationConfig.class);
		when(config.getDestinationKind()).thenReturn("http");

		var destination = mock(Destination.class);
		doNothing().when(destination).initialize();

		var factory = new DestinationPooledObjectFactory();
		factory.setDestinationConfig(config);

		try (var mockedDestinationUtils = mockStatic(DestinationUtils.class)) {

			mockedDestinationUtils.when(() -> DestinationUtils.createDestination(config))
				.thenReturn(destination);

			// act

			factory.create();

			// assert

			verify(destination).initialize();
		}
	}

	@Test
	public void shouldCloseDestinationWhenInitializeThrows() throws Exception {

		// arrange

		var config = mock(DestinationConfig.class);
		when(config.getDestinationKind()).thenReturn("http");

		var destination = mock(Destination.class);
		doThrow(new RuntimeException("init failed")).when(destination).initialize();

		var factory = new DestinationPooledObjectFactory();
		factory.setDestinationConfig(config);

		try (var mockedDestinationUtils = mockStatic(DestinationUtils.class)) {

			mockedDestinationUtils.when(() -> DestinationUtils.createDestination(config))
				.thenReturn(destination);

			// act

			var thrown = catchThrowable(() -> factory.create());

			// assert

			assertThat(thrown)
				.isInstanceOf(RuntimeException.class)
				.hasMessage("init failed");
			verify(destination).close();
		}
	}

	@Test
	public void shouldSetConfigOnDestination() {

		// arrange

		var config = mock(DestinationConfig.class);
		when(config.getDestinationKind()).thenReturn("http");

		var destination = mock(Destination.class);
		doNothing().when(destination).initialize();

		var factory = new DestinationPooledObjectFactory();
		factory.setDestinationConfig(config);

		try (var mockedDestinationUtils = mockStatic(DestinationUtils.class)) {

			mockedDestinationUtils.when(() -> DestinationUtils.createDestination(config))
				.thenReturn(destination);

			// act

			factory.create();

			// assert

			mockedDestinationUtils.verify(() -> DestinationUtils.createDestination(config));
		}
	}
}
