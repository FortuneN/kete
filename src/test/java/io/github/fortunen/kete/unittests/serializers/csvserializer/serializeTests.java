package io.github.fortunen.kete.unittests.serializers.csvserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.serializers.CsvSerializer;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;

public class serializeTests {

	private Map<String, String> parseCsvRow(byte[] csvData) throws Exception {
		var reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(csvData)));
		var headerLine = reader.readLine();
		var dataLine = reader.readLine();
		reader.close();

		if (headerLine == null || dataLine == null) {
			return new HashMap<>();
		}

		var headers = headerLine.split(",");
		var values = dataLine.split(",", -1); // -1 to preserve trailing empty strings

		var result = new HashMap<String, String>();
		for (var i = 0; i < headers.length && i < values.length; i++) {
			result.put(headers[i].trim(), values[i].trim());
		}
		return result;
	}

	@Test
	public void shouldSerializeEventToCsv() throws Exception {

		// arrange

		var serializer = new CsvSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("test-realm");
		event.setUserId("test-user");

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();

		var row = parseCsvRow(result);
		assertThat(row.get("type")).isEqualTo("LOGIN");
		assertThat(row.get("realmId")).isEqualTo("test-realm");
		assertThat(row.get("userId")).isEqualTo("test-user");
	}

	@Test
	public void shouldSerializeAdminEventToCsv() throws Exception {

		// arrange

		var serializer = new CsvSerializer();
		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setRealmId("test-realm");
		adminEvent.setResourceType(org.keycloak.events.admin.ResourceType.USER);

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();

		var row = parseCsvRow(result);
		assertThat(row.get("operationType")).isEqualTo("CREATE");
		assertThat(row.get("realmId")).isEqualTo("test-realm");
		assertThat(row.get("resourceType")).isEqualTo("USER");
	}

	@Test
	public void shouldThrowWhenEventIsNull() {

		// arrange

		var serializer = new CsvSerializer();
		Event event = null;

		// act

		var thrown = catchThrowable(() -> serializer.serialize(event));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("event is required");
	}

	@Test
	public void shouldThrowWhenAdminEventIsNull() {

		// arrange

		var serializer = new CsvSerializer();
		AdminEvent adminEvent = null;

		// act

		var thrown = catchThrowable(() -> serializer.serialize(adminEvent));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("adminEvent is required");
	}

	@Test
	public void shouldSerializeEventWithNullFields() {

		// arrange

		var serializer = new CsvSerializer();
		var event = new Event();

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
	}

	@Test
	public void shouldSerializeAdminEventWithNullFields() {

		// arrange

		var serializer = new CsvSerializer();
		var adminEvent = new AdminEvent();

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
	}

	@Test
	public void shouldReturnTextCsvContentType() {

		// arrange

		var serializer = new CsvSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("text/csv");
	}

	@Test
	public void shouldIncludeHeaderRow() {

		// arrange

		var serializer = new CsvSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);

		// act

		var result = serializer.serialize(event);

		// assert

		var csv = new String(result);
		// Should have header row with column names
		assertThat(csv.contains("\n") || csv.contains("\r")).isTrue();
	}

	@Test
	public void shouldProduceDifferentOutputForDifferentEvents() throws Exception {

		// arrange

		var serializer = new CsvSerializer();
		var event1 = new Event();
		event1.setType(EventType.LOGIN);
		var event2 = new Event();
		event2.setType(EventType.LOGOUT);

		// act

		var result1 = serializer.serialize(event1);
		var result2 = serializer.serialize(event2);

		// assert

		assertThat(result1).isNotNull();
		assertThat(result2).isNotNull();

		var row1 = parseCsvRow(result1);
		var row2 = parseCsvRow(result2);
		assertThat(row1.get("type")).isEqualTo("LOGIN");
		assertThat(row2.get("type")).isEqualTo("LOGOUT");
	}

	@Test
	public void shouldSerializeConsistently() {

		// arrange

		var serializer = new CsvSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("test");

		// act

		var result1 = serializer.serialize(event);
		var result2 = serializer.serialize(event);

		// assert

		assertThat(result2).isEqualTo(result1);
	}

	@Test
	public void shouldSerializeEventWithAllFields() throws Exception {

		// arrange

		var serializer = new CsvSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("realm");
		event.setUserId("user");
		event.setSessionId("session");
		event.setIpAddress("127.0.0.1");
		event.setClientId("client");
		var eventTime = 1234567890L;
		event.setTime(eventTime);

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();

		var row = parseCsvRow(result);
		assertThat(row.get("type")).isEqualTo("LOGIN");
		assertThat(row.get("realmId")).isEqualTo("realm");
		assertThat(row.get("userId")).isEqualTo("user");
		assertThat(row.get("sessionId")).isEqualTo("session");
		assertThat(row.get("ipAddress")).isEqualTo("127.0.0.1");
		assertThat(row.get("clientId")).isEqualTo("client");
		assertThat(row.get("time")).isEqualTo(String.valueOf(eventTime));
	}

	@Test
	public void shouldHandleNewlinesInValues() {

		// arrange

		var serializer = new CsvSerializer();
		var event = new Event();
		event.setRealmId("realm\nwith\nnewlines");

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
		// CSV should properly quote fields with newlines
	}
}
