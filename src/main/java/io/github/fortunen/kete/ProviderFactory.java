package io.github.fortunen.kete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.keycloak.Config.Scope;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.KeycloakSessionTask;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.PostMigrationEvent;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.provider.ProviderEventListener;

import io.github.fortunen.kete.utils.ConfigurationUtils;
import io.github.fortunen.kete.utils.ExecutorUtils;
import io.github.fortunen.kete.utils.MetricsUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ProviderFactory implements EventListenerProviderFactory, ProviderEventListener, KeycloakSessionTask, BiConsumer<AdminEvent, Boolean>, Consumer<Event> {

	private static final Set<String> ALL_EVENT_TYPES = Arrays.stream(EventType.values()).map(EventType::name).collect(Collectors.toSet());

	private Scope scope;
	private Configuration configuration;
	private SerializerRoutes[] serializersWithRoutes;
	private KeycloakSessionFactory postInitSessionFactory;
	private Map<String, String> environment = System.getenv();
	private ExecutorService eventExecutor = ExecutorUtils.createService();

	@Override
	public String getId() { return Constants.ID; }

	@Override
	public void init(Scope scope) {

		ValidationUtils.requireNonNull(scope, "scope is required");

		this.scope = scope;
	}

	@Override
	public void postInit(KeycloakSessionFactory sessionFactory) {

		ValidationUtils.requireNonNull(sessionFactory, "sessionFactory is required");

		postInitSessionFactory = sessionFactory;
		postInitSessionFactory.register(this);
	}

	@Override
	public void onEvent(ProviderEvent providerEvent) {

		ValidationUtils.requireNonNull(providerEvent, "providerEvent is required");
		ValidationUtils.requireNonNull(postInitSessionFactory, "postInitSessionFactory is required");

		if (providerEvent instanceof PostMigrationEvent) {
			KeycloakModelUtils.runJobInTransaction(postInitSessionFactory, this);
		}
	}

	@Override
	public void run(KeycloakSession session) {

		ValidationUtils.requireNonNull(session, "session is required");

		// realmProvider

		var realmProvider = ValidationUtils.requireNonNull(session.realms(), "realmProvider is required");
		var realms = ValidationUtils.requireNonNull(realmProvider.getRealmsStream().toArray(RealmModel[]::new), "realms are required");

		// moduleConfiguration

		configuration = ConfigurationUtils.createConfiguration(scope, environment);

		if (ValidationUtils.isNull(configuration)) {

			log.info("{} disabled", Constants.ID);

			for (var realm : realms) {

				var realmEventListeners = ValidationUtils.requireNonNull(realm.getEventsListenersStream(), "events listeners for realm " + realm.getName() + " are required").collect(Collectors.toSet());

				realmEventListeners.remove(Constants.ID);

				realm.setEventsListeners(realmEventListeners);
			}

			return;
		}

		log.info("{} initializing", Constants.ID);

		// metrics

		MetricsUtils.configure(configuration.isMetricsEnabled());

		// routes

		var routes = configuration.getRoutes();
		var routeRealms = ConcurrentHashMap.newKeySet();
		var serializersWithRoutesMap = new ConcurrentHashMap<Serializer, List<Route>>();

		if (ValidationUtils.isNotNull(routes)) {
			ExecutorUtils.forEach(eventExecutor, routes, route -> {
				try {

					route.initialize(session);

					MetricsUtils.registerPoolMetrics(route.getName(), route.getDestinationPool());

					var serializer = route.getSerializer();

					for (var realm : realms) {
						if (route.acceptRealm(realm.getName())) {
							routeRealms.add(realm.getName());
						}
					}

					serializersWithRoutesMap.computeIfAbsent(serializer, k -> new ArrayList<>()).add(route);

					log.info("{} Route '{}' initialized: destination={}, serializer={}, realmMatchers={}, eventMatchers={}",
						Constants.ID,
						route.getName(),
						route.getDestinationConfig().getClass().getSimpleName(),
						route.getSerializer().getClass().getSimpleName(),
						route.getRealmMatchers().length,
						route.getEventMatchers().length);

				} catch (Exception exception) {

					log.warn("Failed to initialize route : " + route.getName(), exception);
					ValidationUtils.tryClose(route, "route : " + route.getName());

				}
			});
		}

		serializersWithRoutes = serializersWithRoutesMap.entrySet().stream().map(entry -> new SerializerRoutes(entry.getKey(), entry.getValue())).toArray(SerializerRoutes[]::new);

		log.info("{} initialized", Constants.ID);

		MetricsUtils.recordActiveRoutes(serializersWithRoutesMap.values().stream().mapToInt(List::size).sum());

		// listeners

		for (var realm : realms) {

			var realmEventListeners = ValidationUtils.requireNonNull(realm.getEventsListenersStream(), "events listeners for realm " + realm.getName() + " are required").collect(Collectors.toSet());

			if (!routeRealms.contains(realm.getName())) {
				realmEventListeners.remove(Constants.ID);
				continue;
			}

			realmEventListeners.add(Constants.ID);

			realm.setEventsEnabled(true);
			realm.setAdminEventsEnabled(true);
			realm.setEnabledEventTypes(ALL_EVENT_TYPES);
			realm.setAdminEventsDetailsEnabled(true);

			realm.setEventsListeners(realmEventListeners);
		}
	}

	@Override
	public EventListenerProvider create(KeycloakSession session) {

		ValidationUtils.requireNonNull(session, "session is required");

		var transactionManager = ValidationUtils.requireNonNull(session.getTransactionManager(), "transactionManager is required");
		var transaction = new EventListenerTransaction(this, this);

		transactionManager.enlistAfterCompletion(transaction);

		return new Provider(transaction);
	}

	private void sendMessage(String eventId, String realm, String eventType, String result, boolean isAdminEvent, String resourceType, String operationType, Function<Serializer, byte[]> serializeFunction) {

		var eventKind = isAdminEvent ? Constants.ADMIN_EVENT : Constants.EVENT;

		for (var serializerWithRoutes : serializersWithRoutes) {
			ExecutorUtils.execute(eventExecutor, () -> {
				try {

					var serializer = serializerWithRoutes.serializer();
					var contentType = serializer.getContentType();
					var serializedEventBody = serializeFunction.apply(serializer);
					var message = new EventMessage(realm, eventId, serializedEventBody, eventType, contentType, resourceType, eventKind, operationType, result);

					for (var route : serializerWithRoutes.routes()) {
						ExecutorUtils.execute(eventExecutor, () -> {
							try {

								if (route.acceptRealm(realm) && route.acceptEvent(eventType)) {

									MetricsUtils.timeForward(route.getName(), () -> {
										try {
											route.send(message);
										} catch (Exception e) {
											throw new RuntimeException(e);
										}
									});

									MetricsUtils.recordEventForwarded(route.getName(), eventType, realm);
								}

							} catch (Exception exception) {
								MetricsUtils.recordEventFailed(route.getName(), eventType, realm, exception.getClass().getSimpleName());
								log.warn("Failed to send " + eventType + " : " + eventId + " : to route : " + route.getName(), exception);
							}
						});
					}

				} catch (Exception exception) {
					log.warn("Unhandled exception in " + eventType + " processing", exception);
				}
			});
		}
	}

	@Override
	public void accept(Event event) {

		if (ValidationUtils.isNull(event)) {
			return;
		}

		if (ValidationUtils.isNullOrEmpty(serializersWithRoutes)) {
			return;
		}

		if (ValidationUtils.isNull(event.getType())) {
			log.warn("Event has null type, skipping: " + event.getId());
			return;
		}

		var eventId = event.getId();
		var realm = event.getRealmName();
		var eventType = event.getType().name();
		var result = ValidationUtils.isBlank(event.getError()) ? "SUCCESS" : "ERROR";

		sendMessage(eventId, realm, eventType, result, false, null, null, serializer -> serializer.serialize(event));
	}

	@Override
	public void accept(AdminEvent event, Boolean includeRepresentation) {

		if (ValidationUtils.isNull(event)) {
			return;
		}

		if (ValidationUtils.isNullOrEmpty(serializersWithRoutes)) {
			return;
		}

		if (ValidationUtils.isNull(event.getResourceType()) || ValidationUtils.isNull(event.getOperationType())) {
			log.warn("AdminEvent has null resourceType or operationType, skipping: " + event.getId());
			return;
		}

		var eventId = event.getId();
		var realm = event.getRealmName();
		var resourceType = event.getResourceType().name();
		var operationType = event.getOperationType().name();
		var eventType = resourceType + '_' + operationType;
		var result = ValidationUtils.isBlank(event.getError()) ? "SUCCESS" : "ERROR";

		sendMessage(eventId, realm, eventType, result, true, resourceType, operationType, serializer -> serializer.serialize(event));
	}

	@Override
	public void close() {

		// log - closing

		if (ValidationUtils.isNotNull(configuration)) {
			log.info("{} closing", Constants.ID);
		}

		// eventExecutor

		ExecutorUtils.shutdown(eventExecutor, "event executor");

		// routes

		if (ValidationUtils.isNotNull(serializersWithRoutes)) {
			for (var serializerRoutes : serializersWithRoutes) {
				for (var route : serializerRoutes.routes()) {
					ValidationUtils.tryClose(route, "route : " + route.getName());
				}
			}
		}

		// log - closed

		if (ValidationUtils.isNotNull(configuration)) {
			log.info("{} closed", Constants.ID);
		}
	}
}
