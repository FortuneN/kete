package io.github.fortunen.kete.integrationtests.natsdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.nats.client.NKey;
import io.nats.client.Nats;
import io.nats.client.support.JsonSerializable;
import io.nats.client.support.JwtUtils;

public class credentialsFileTests extends TestBase {

	private static final String SUBJECT = "test-subject";
	private static final byte[] BODY = "{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8);

	// the server trusts one operator and preloads one account; users are signed by that account

	private record OperatorMode(NKey accountKey, String accountPublicKey, String serverConfig) {
	}

	@Test
	public void shouldSendWithCredentialsFileText() throws Exception {

		// arrange

		var operatorMode = createOperatorMode();
		var credentials = issueUserCredentials(operatorMode.accountKey(), operatorMode.accountPublicKey());
		startNatsWithConfig(operatorMode.serverConfig());

		var map = new HashMap<String, Object>();
		map.put("servers", getNatsUrl());
		map.put("subject", SUBJECT);
		map.put("authentication-method", "credentials-file-text");
		map.put("credentials-file-text", credentials);
		configureDestination(new MapConfiguration(map));
		destination.initialize();

		var collector = new MessageCollector();
		try (var subscriber = createSubscriberWithAuthHandler(SUBJECT, collector, Nats.staticCredentials(credentials.getBytes(StandardCharsets.UTF_8)))) {

			// act

			destination.send(createMessage("test-event-id", "test-realm", false, "LOGIN", "application/json", BODY, null, null));

			// assert

			await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(200)).until(() -> !collector.getMessages().isEmpty());

			assertThat(collector.getMessages()).containsExactly(new String(BODY, StandardCharsets.UTF_8));
		}
	}

	@Test
	public void shouldSendWithCredentialsFileBase64() throws Exception {

		// arrange

		var operatorMode = createOperatorMode();
		var credentials = issueUserCredentials(operatorMode.accountKey(), operatorMode.accountPublicKey());
		startNatsWithConfig(operatorMode.serverConfig());

		var map = new HashMap<String, Object>();
		map.put("servers", getNatsUrl());
		map.put("subject", SUBJECT);
		map.put("authentication-method", "credentials-file-base64");
		map.put("credentials-file-base64", Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8)));
		configureDestination(new MapConfiguration(map));
		destination.initialize();

		var collector = new MessageCollector();
		try (var subscriber = createSubscriberWithAuthHandler(SUBJECT, collector, Nats.staticCredentials(credentials.getBytes(StandardCharsets.UTF_8)))) {

			// act

			destination.send(createMessage("test-event-id", "test-realm", false, "LOGIN", "application/json", BODY, null, null));

			// assert

			await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(200)).until(() -> !collector.getMessages().isEmpty());

			assertThat(collector.getMessages()).containsExactly(new String(BODY, StandardCharsets.UTF_8));
		}
	}

	@Test
	public void shouldRejectCredentialsIssuedByAnUnknownAccount() throws Exception {

		// arrange

		var operatorMode = createOperatorMode();
		var unknownAccountKey = NKey.createAccount(new SecureRandom());
		var credentials = issueUserCredentials(unknownAccountKey, new String(unknownAccountKey.getPublicKey()));
		startNatsWithConfig(operatorMode.serverConfig());

		var map = new HashMap<String, Object>();
		map.put("servers", getNatsUrl());
		map.put("subject", SUBJECT);
		map.put("authentication-method", "credentials-file-text");
		map.put("credentials-file-text", credentials);
		configureDestination(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> destination.initialize());

		// assert

		assertThat(thrown).isNotNull();
	}

	private static OperatorMode createOperatorMode() throws Exception {

		var random = new SecureRandom();
		var operatorKey = NKey.createOperator(random);
		var accountKey = NKey.createAccount(random);
		var operatorPublicKey = new String(operatorKey.getPublicKey());
		var accountPublicKey = new String(accountKey.getPublicKey());
		var issuedAt = JwtUtils.currentTimeSeconds();

		var operatorJwt = JwtUtils.issueJWT(operatorKey, operatorPublicKey, "test-operator", null, issuedAt, operatorPublicKey, claim("{\"type\":\"operator\",\"version\":2}"));
		var accountJwt = JwtUtils.issueJWT(operatorKey, accountPublicKey, "test-account", null, issuedAt, operatorPublicKey, claim("{\"limits\":{\"subs\":-1,\"data\":-1,\"payload\":-1,\"imports\":-1,\"exports\":-1,\"wildcards\":true,\"conn\":-1,\"leaf\":-1},\"type\":\"account\",\"version\":2}"));

		var serverConfig = "port: 4222\n"
			+ "http_port: 8222\n"
			+ "operator: \"" + operatorJwt + "\"\n"
			+ "resolver: MEMORY\n"
			+ "resolver_preload: {\n"
			+ "  " + accountPublicKey + ": \"" + accountJwt + "\"\n"
			+ "}\n";

		return new OperatorMode(accountKey, accountPublicKey, serverConfig);
	}

	// a .creds file: the user JWT signed by the account plus the user's seed for the nonce signature

	private static String issueUserCredentials(NKey accountKey, String accountPublicKey) throws Exception {
		var userKey = NKey.createUser(new SecureRandom());
		var userJwt = JwtUtils.issueUserJWT(accountKey, accountPublicKey, new String(userKey.getPublicKey()), "test-user");
		return String.format(JwtUtils.NATS_USER_JWT_FORMAT, userJwt, new String(userKey.getSeed()));
	}

	private static JsonSerializable claim(String json) {
		return () -> json;
	}
}
