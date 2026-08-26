package io.github.fortunen.kete;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
import io.github.fortunen.kete.utils.JsonUtils;
import io.github.fortunen.kete.utils.LogThrottle;
import io.github.fortunen.kete.utils.MetricsUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ProviderFactory implements EventListenerProviderFactory, ProviderEventListener, KeycloakSessionTask, BiConsumer<AdminEvent, Boolean>, Consumer<Event> {

	private static final Set<String> ALL_EVENT_TYPES = Arrays.stream(EventType.values()).map(EventType::name).collect(Collectors.toSet());
	private static final Duration INITIAL_INITIALIZATION_RETRY_DELAY = Duration.ofSeconds(30);
	private static final Duration MAX_INITIALIZATION_RETRY_DELAY = Duration.ofMinutes(5);

	private Scope scope;
	private Configuration configuration;
	private volatile SerializerRoutes[] serializersWithRoutes;
	private KeycloakSessionFactory postInitSessionFactory;
	private Map<String, String> environment = System.getenv();
	private ExecutorService eventExecutor = ExecutorUtils.createService();
	private final LogThrottle failureLogThrottle = new LogThrottle(Duration.ofMinutes(1));
	private final AtomicInteger unrecoverableRoutes = new AtomicInteger();
	private final List<Route> pendingRoutes = new CopyOnWriteArrayList<>();
	private final ScheduledExecutorService initializationRetryScheduler = ExecutorUtils.createScheduler(Constants.ID + "-initialization-retry");

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
			return;
		}

		if (providerEvent instanceof RealmModel.RealmPostCreateEvent realmPostCreateEvent) {
			registerRealm(ValidationUtils.requireNonNull(realmPostCreateEvent.getCreatedRealm(), "created realm is required"));
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

			log.info("{} ({}) disabled", Constants.ID, Constants.VERSION);

			for (var realm : realms) {

				var realmEventListeners = ValidationUtils.requireNonNull(realm.getEventsListenersStream(), "events listeners for realm " + realm.getName() + " are required").collect(Collectors.toSet());

				realmEventListeners.remove(Constants.ID);

				realm.setEventsListeners(realmEventListeners);
			}

			return;
		}

		log.info("{} ({}) initializing", Constants.ID, Constants.VERSION);

		// metrics

		MetricsUtils.configure(configuration.isMetricsEnabled());

		// routes

		var routes = configuration.getRoutes();
		var configuredRoutes = new ArrayList<Route>();
		var serializersWithRoutesMap = new ConcurrentHashMap<Serializer, List<Route>>();

		if (ValidationUtils.isNotNull(routes)) {

			// destination configs first, one after another: they may read or write the Keycloak model through this thread's session

			for (var route : routes) {
				try {

					route.initializeConfig(session);
					configuredRoutes.add(route);

				} catch (Exception exception) {

					unrecoverableRoutes.incrementAndGet();
					log.warn("Failed to initialize route : " + route.getName(), exception);
					ValidationUtils.tryClose(route, "route : " + route.getName());

				}
			}

			// then the destination handshakes, in parallel

			ExecutorUtils.forEach(eventExecutor, configuredRoutes, route -> {
				try {

					route.initializeDestinations();

					MetricsUtils.registerPoolMetrics(route.getName(), route.getDestinationPool());
					MetricsUtils.registerInFlight(route.getName(), route.getInFlight());

					var serializer = route.getSerializer();

					serializersWithRoutesMap.computeIfAbsent(serializer, k -> new ArrayList<>()).add(route);

					log.info("{} Route '{}' initialized: destination={}, serializer={}, realmMatchers={}, eventMatchers={}",
						Constants.ID,
						route.getName(),
						route.getDestinationKind(),
						route.getSerializerKind(),
						route.getRealmMatchers().length,
						route.getEventMatchers().length);

				} catch (Exception exception) {

					// the destination is unreachable right now: keep the route and retry in the background

					log.warn("Failed to initialize route : " + route.getName() + " (will retry in the background)", exception);
					route.resetDestinations();
					pendingRoutes.add(route);

				}
			});
		}

		serializersWithRoutes = serializersWithRoutesMap.entrySet().stream().map(entry -> new SerializerRoutes(entry.getKey(), entry.getValue())).toArray(SerializerRoutes[]::new);

		log.info("{} initialized", Constants.ID);

		if (configuration.isSupportTheProject()) {
			logSupportMessage();
		}

		MetricsUtils.recordActiveRoutes(activeRouteCount());
		MetricsUtils.recordFailedRoutes(unrecoverableRoutes.get() + pendingRoutes.size());

		scheduleInitializationRetry(INITIAL_INITIALIZATION_RETRY_DELAY);

		// listeners

		for (var realm : realms) {
			registerRealm(realm);
		}
	}

	private int activeRouteCount() {
		return ValidationUtils.isNull(serializersWithRoutes) ? 0 : Arrays.stream(serializersWithRoutes).mapToInt(serializerRoutes -> serializerRoutes.routes().size()).sum();
	}

	private void scheduleInitializationRetry(Duration delay) {

		if (pendingRoutes.isEmpty() || initializationRetryScheduler.isShutdown()) {
			return;
		}

		var doubled = delay.multipliedBy(2);
		var nextDelay = doubled.compareTo(MAX_INITIALIZATION_RETRY_DELAY) > 0 ? MAX_INITIALIZATION_RETRY_DELAY : doubled;

		initializationRetryScheduler.schedule(() -> {
			retryPendingRoutes();
			scheduleInitializationRetry(nextDelay);
		}, delay.toMillis(), TimeUnit.MILLISECONDS);
	}

	// one attempt for every route whose destination was unreachable; routes that come up join the send path

	public void retryPendingRoutes() {

		for (var route : pendingRoutes) {

			try {

				route.initializeDestinations();

			} catch (Exception exception) {

				route.resetDestinations();
				log.warn("Failed to initialize route : " + route.getName() + " (will retry)", exception);
				continue;
			}

			pendingRoutes.remove(route);
			activateRoute(route);
		}
	}

	private void activateRoute(Route route) {

		MetricsUtils.registerPoolMetrics(route.getName(), route.getDestinationPool());
		MetricsUtils.registerInFlight(route.getName(), route.getInFlight());

		// publish the route to the send path: the array is replaced as a whole, never edited in place

		var updated = new ArrayList<SerializerRoutes>();
		var joined = false;

		if (ValidationUtils.isNotNull(serializersWithRoutes)) {
			for (var serializerRoutes : serializersWithRoutes) {
				if (serializerRoutes.serializer().equals(route.getSerializer())) {
					var routes = new ArrayList<>(serializerRoutes.routes());
					routes.add(route);
					updated.add(new SerializerRoutes(serializerRoutes.serializer(), routes));
					joined = true;
				} else {
					updated.add(serializerRoutes);
				}
			}
		}

		if (!joined) {
			updated.add(new SerializerRoutes(route.getSerializer(), new ArrayList<>(List.of(route))));
		}

		serializersWithRoutes = updated.toArray(SerializerRoutes[]::new);

		MetricsUtils.recordActiveRoutes(activeRouteCount());
		MetricsUtils.recordFailedRoutes(unrecoverableRoutes.get() + pendingRoutes.size());

		log.info("{} Route '{}' initialized: destination={}, serializer={}, realmMatchers={}, eventMatchers={}",
			Constants.ID,
			route.getName(),
			route.getDestinationKind(),
			route.getSerializerKind(),
			route.getRealmMatchers().length,
			route.getEventMatchers().length);

		// realms only this route accepts have not been registered yet

		if (ValidationUtils.isNotNull(postInitSessionFactory)) {
			KeycloakModelUtils.runJobInTransaction(postInitSessionFactory, session -> {
				var realmProvider = ValidationUtils.requireNonNull(session.realms(), "realmProvider is required");
				realmProvider.getRealmsStream().forEach(this::registerRealm);
			});
		}
	}

	private void registerRealm(RealmModel realm) {

		var realmName = ValidationUtils.requireNonNull(realm.getName(), "realm name is required");
		var realmEventListeners = ValidationUtils.requireNonNull(realm.getEventsListenersStream(), "events listeners for realm " + realmName + " are required").collect(Collectors.toSet());

		var accepted = ValidationUtils.isNotNullOrEmpty(serializersWithRoutes)
			&& Arrays.stream(serializersWithRoutes).flatMap(serializerRoutes -> serializerRoutes.routes().stream()).anyMatch(route -> route.acceptRealm(realmName));

		if (!accepted) {
			if (realmEventListeners.remove(Constants.ID)) {
				realm.setEventsListeners(realmEventListeners);
			}
			return;
		}

		realmEventListeners.add(Constants.ID);

		realm.setEventsEnabled(true);
		realm.setAdminEventsEnabled(true);
		realm.setEnabledEventTypes(ALL_EVENT_TYPES);
		realm.setAdminEventsDetailsEnabled(true);

		realm.setEventsListeners(realmEventListeners);
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

									MetricsUtils.timeForward(route.getName(), () -> route.send(message));

									MetricsUtils.recordEventForwarded(route.getName(), eventType, realm);
								}

							} catch (Exception exception) {
								MetricsUtils.recordEventFailed(route.getName(), eventType, realm, exception.getClass().getSimpleName());
								warnThrottled("route:" + route.getName(), "Failed to send " + eventType + " : " + eventId + " : to route : " + route.getName(), exception);
							}
						});
					}

				} catch (Exception exception) {
					var serializerName = serializerWithRoutes.serializer().getClass().getSimpleName();
					MetricsUtils.recordSerializationFailed(serializerName, eventType, realm, exception.getClass().getSimpleName());
					warnThrottled("serializer:" + serializerName, "Failed to serialize " + eventType + " : " + eventId + " : with " + serializerName, exception);
				}
			});
		}
	}

	// one stack trace per route (or serializer) per minute: an outage would otherwise log once per event; the metrics count every failure

	private void warnThrottled(String key, String message, Exception exception) {

		var suppressed = failureLogThrottle.permit(key);

		if (suppressed < 0) {
			return;
		}

		log.warn(suppressed > 0 ? message + " (" + suppressed + " similar failures suppressed in the last minute)" : message, exception);
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
		var eventToSerialize = Boolean.FALSE.equals(includeRepresentation) ? withoutRepresentation(event) : event;

		sendMessage(eventId, realm, eventType, result, true, resourceType, operationType, serializer -> serializer.serialize(eventToSerialize));
	}

	private static AdminEvent withoutRepresentation(AdminEvent event) {

		if (ValidationUtils.isNull(event.getRepresentation())) {
			return event;
		}

		// the realm asked for no representation: serialize a copy without it (a Jackson round trip keeps runtime-only fields such as `details`)

		var copy = JsonUtils.copy(event, AdminEvent.class);

		copy.setRepresentation(null);

		return copy;
	}

	@Override
	public void close() {

		// log - closing

		if (ValidationUtils.isNotNull(configuration)) {
			log.info("{} closing", Constants.ID);
		}

		// eventExecutor

		ExecutorUtils.shutdown(eventExecutor, "event executor");

		// initialization retries

		ExecutorUtils.shutdown(initializationRetryScheduler, "initialization retry scheduler");

		for (var route : pendingRoutes) {
			ValidationUtils.tryClose(route, "route : " + route.getName());
		}

		pendingRoutes.clear();

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

	public void logSupportMessage() {

		var message = String.join("\n",
			"",
			"╔══════════════════════════════════════════════════════════════════════════════════╗",
			"║ Thank you for using KETE. Please consider supporting the project (optional)      ║",
			"╠══════════════════════════════════════════════════════════════════════════════════╣",
			"║ Platform          │ Type                 │ Link                                  ║",
			"╟───────────────────┼──────────────────────┼───────────────────────────────────────╢",
			"║ GitHub (Stars)    │ Free                 │ https://github.com/FortuneN/kete      ║",
			"║ GitHub (Sponsors) │ One-time / Recurring │ https://github.com/sponsors/FortuneN  ║",
			"║ PayPal            │ One-time / Recurring │ https://paypal.me/FortuneNgwenya      ║",
			"║ Buy Me a Coffee   │ One-time / Recurring │ https://buymeacoffee.com/FortuneN     ║",
			"║ Ko-fi             │ One-time / Recurring │ https://ko-fi.com/FortuneN            ║",
			"║ Liberapay         │ Recurring            │ https://liberapay.com/FortuneN        ║",
			"╠══════════════════════════════════════════════════════════════════════════════════╣",
			"║ To disable this message (recommended) : kete.support-the-project-message = false ║",
			"╚══════════════════════════════════════════════════════════════════════════════════╝",
			""
		);

		log.info(message);
	}
}
