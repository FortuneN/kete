package io.github.fortunen.kete.unittests.providerfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;

import io.github.fortunen.kete.ProviderFactory;

public class logSupportMessageTests {

	// =========================================================================
	// Support Message Shown (Default Behavior)
	// =========================================================================

	@Test
	public void shouldShowSupportMessageWhenSupportTheProjectIsTrue() {

		// arrange

		var env = new HashMap<String, String>();
		// Default: support-the-project-message = true (not set, defaults to true)

		var factory = spy(new ProviderFactory());

		var scope = mock(Scope.class);
		when(scope.getPropertyNames()).thenReturn(Set.of());
		factory.init(scope);

		factory.setEnvironment(env);

		var session = mock(KeycloakSession.class);
		var realmProvider = mock(RealmProvider.class);
		var realm = mock(RealmModel.class);

		when(session.realms()).thenReturn(realmProvider);
		when(realmProvider.getRealmsStream()).thenReturn(Stream.of(realm));
		when(realm.getName()).thenReturn("master");
		when(realm.getEventsListenersStream()).thenReturn(Stream.of());

		// act

		factory.run(session);

		// assert

		assertThat(factory.getConfiguration().isSupportTheProject())
			.as("supportTheProject should be true by default")
			.isTrue();

		verify(factory, times(1)).logSupportMessage();

		// cleanup

		factory.close();
	}

	@Test
	public void shouldShowSupportMessageWhenExplicitlyEnabled() {

		// arrange

		var env = new HashMap<String, String>();
		env.put("kete.support-the-project-message", "true");

		var factory = spy(new ProviderFactory());

		var scope = mock(Scope.class);
		when(scope.getPropertyNames()).thenReturn(Set.of());
		factory.init(scope);

		factory.setEnvironment(env);

		var session = mock(KeycloakSession.class);
		var realmProvider = mock(RealmProvider.class);
		var realm = mock(RealmModel.class);

		when(session.realms()).thenReturn(realmProvider);
		when(realmProvider.getRealmsStream()).thenReturn(Stream.of(realm));
		when(realm.getName()).thenReturn("master");
		when(realm.getEventsListenersStream()).thenReturn(Stream.of());

		// act

		factory.run(session);

		// assert

		assertThat(factory.getConfiguration().isSupportTheProject())
			.as("supportTheProject should be true when explicitly enabled")
			.isTrue();

		verify(factory, times(1)).logSupportMessage();

		// cleanup

		factory.close();
	}

	// =========================================================================
	// Support Message Hidden (User Disabled) - CRITICAL: Must work to avoid dark pattern
	// =========================================================================

	@Test
	public void shouldNotShowSupportMessageWhenSupportTheProjectIsFalse() {

		// arrange

		var env = new HashMap<String, String>();
		env.put("kete.support-the-project-message", "false");

		var factory = spy(new ProviderFactory());

		var scope = mock(Scope.class);
		when(scope.getPropertyNames()).thenReturn(Set.of());
		factory.init(scope);

		factory.setEnvironment(env);

		var session = mock(KeycloakSession.class);
		var realmProvider = mock(RealmProvider.class);
		var realm = mock(RealmModel.class);

		when(session.realms()).thenReturn(realmProvider);
		when(realmProvider.getRealmsStream()).thenReturn(Stream.of(realm));
		when(realm.getName()).thenReturn("master");
		when(realm.getEventsListenersStream()).thenReturn(Stream.of());

		// act

		factory.run(session);

		// assert - CRITICAL: Message must NOT be shown when disabled

		assertThat(factory.getConfiguration().isSupportTheProject())
			.as("supportTheProject should be false when disabled - this is critical to avoid dark pattern behavior")
			.isFalse();

		verify(factory, times(0)).logSupportMessage();

		// cleanup

		factory.close();
	}

	@Test
	public void shouldRespectDisabledSettingCaseInsensitively() {

		// arrange

		var env = new HashMap<String, String>();
		env.put("kete.support-the-project-message", "FALSE");

		var factory = spy(new ProviderFactory());

		var scope = mock(Scope.class);
		when(scope.getPropertyNames()).thenReturn(Set.of());
		factory.init(scope);

		factory.setEnvironment(env);

		var session = mock(KeycloakSession.class);
		var realmProvider = mock(RealmProvider.class);
		var realm = mock(RealmModel.class);

		when(session.realms()).thenReturn(realmProvider);
		when(realmProvider.getRealmsStream()).thenReturn(Stream.of(realm));
		when(realm.getName()).thenReturn("master");
		when(realm.getEventsListenersStream()).thenReturn(Stream.of());

		// act

		factory.run(session);

		// assert - CRITICAL: Message must NOT be shown when disabled (case insensitive)

		assertThat(factory.getConfiguration().isSupportTheProject())
			.as("supportTheProject should be false when disabled with FALSE (case insensitive)")
			.isFalse();

		verify(factory, times(0)).logSupportMessage();

		// cleanup

		factory.close();
	}
}
