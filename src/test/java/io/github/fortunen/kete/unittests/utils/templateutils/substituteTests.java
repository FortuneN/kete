package io.github.fortunen.kete.unittests.utils.templateutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import org.junit.jupiter.api.Test;

class substituteTests {

	private EventMessage createMessage(
		String realm,
		String eventId,
		byte[] eventBody,
		String eventType,
		String contentType,
		String resourceType,
		boolean isAdminEvent,
		String operationType,
		String result
	) {
		return new EventMessage(realm, eventId, eventBody, eventType, contentType, resourceType, isAdminEvent ? Constants.ADMIN_EVENT : Constants.EVENT, operationType, result);
	}

	@Test
	void shouldReturnNullWhenTemplateIsNull() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, "LOGIN", null, null, false, null, Constants.SUCCESS);

		// act

		var result = TemplateUtils.substitute(null, message);

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldReturnEmptyStringWhenTemplateIsEmpty() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, "LOGIN", null, null, false, null, Constants.SUCCESS);

		// act

		var result = TemplateUtils.substitute("", message);

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	void shouldReturnBlankStringWhenTemplateIsBlank() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, "LOGIN", null, null, false, null, Constants.SUCCESS);

		// act

		var result = TemplateUtils.substitute("   ", message);

		// assert

		assertThat(result).isEqualTo("   ");
	}

	@Test
	void shouldReturnTemplateWhenMessageIsNull() {

		// arrange

		TemplateUtils.clearCache();

		var template = "events/${eventTypeLowerCase}";

		// act

		var result = TemplateUtils.substitute(template, null);

		// assert

		assertThat(result).isEqualTo(template);
	}

	@Test
	void shouldSubstituteEventTypeLowerCase() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, "LOGIN", null, null, false, null, Constants.SUCCESS);
		var template = "events/${eventTypeLowerCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("events/login");
	}

	@Test
	void shouldSubstituteEventTypeUpperCase() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, "LOGIN", null, null, false, null, Constants.SUCCESS);
		var template = "events/${eventTypeUpperCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("events/LOGIN");
	}

	@Test
	void shouldSubstituteRealmLowerCase() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage("MASTER", null, null, null, null, null, false, null, null);
		var template = "realms/${realmLowerCase}/events";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("realms/master/events");
	}

	@Test
	void shouldSubstituteRealmUpperCase() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage("MASTER", null, null, null, null, null, false, null, null);
		var template = "realms/${realmUpperCase}/events";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("realms/MASTER/events");
	}

	@Test
	void shouldSubstituteKindLowerCaseForEvent() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, null, null, null, false, null, null);
		var template = "keycloak/${kindLowerCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("keycloak/event");
	}

	@Test
	void shouldSubstituteKindUpperCaseForEvent() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, null, null, null, false, null, null);
		var template = "keycloak/${kindUpperCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("keycloak/EVENT");
	}

	@Test
	void shouldSubstituteKindLowerCaseForAdminEvent() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, null, null, null, true, null, null);
		var template = "keycloak/${kindLowerCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("keycloak/admin_event");
	}

	@Test
	void shouldSubstituteKindUpperCaseForAdminEvent() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, null, null, null, true, null, null);
		var template = "keycloak/${kindUpperCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("keycloak/ADMIN_EVENT");
	}

	@Test
	void shouldSubstituteResourceTypeLowerCase() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, null, null, "USER", false, null, null);
		var template = "admin/${resourceTypeLowerCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("admin/user");
	}

	@Test
	void shouldSubstituteResourceTypeUpperCase() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, null, null, "USER", false, null, null);
		var template = "admin/${resourceTypeUpperCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("admin/USER");
	}

	@Test
	void shouldSubstituteOperationTypeLowerCase() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, null, null, null, false, "CREATE", null);
		var template = "operations/${operationTypeLowerCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("operations/create");
	}

	@Test
	void shouldSubstituteOperationTypeUpperCase() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, null, null, null, false, "CREATE", null);
		var template = "operations/${operationTypeUpperCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("operations/CREATE");
	}

	@Test
	void shouldSubstituteMultipleVariables() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage("MASTER", null, null, "LOGIN", null, null, false, null, Constants.SUCCESS);
		var template = "keycloak/${realmLowerCase}/events/${eventTypeLowerCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("keycloak/master/events/login");
	}

	@Test
	void shouldLeaveUnknownVariablesUnchanged() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, "LOGIN", null, null, false, null, Constants.SUCCESS);
		var template = "events/${eventTypeLowerCase}/${unknownVariable}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("events/login/${unknownVariable}");
	}

	@Test
	void shouldLeaveVariableUnchangedWhenValueIsNull() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, null, null, null, false, null, null);
		var template = "events/${eventTypeLowerCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("events/${eventTypeLowerCase}");
	}

	@Test
	void shouldLeaveVariableUnchangedWhenValueIsEmpty() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, "", null, null, false, null, null);
		var template = "events/${eventTypeLowerCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("events/${eventTypeLowerCase}");
	}

	@Test
	void shouldReturnTemplateWithNoVariables() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, "LOGIN", null, null, false, null, Constants.SUCCESS);
		var template = "static/path/no/variables";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("static/path/no/variables");
	}

	@Test
	void shouldCacheSubstitutionResults() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage("MASTER", null, null, "LOGIN", null, null, false, null, Constants.SUCCESS);
		var template = "keycloak/${realmLowerCase}/events/${eventTypeLowerCase}";

		// act - call twice

		var result1 = TemplateUtils.substitute(template, message);
		var result2 = TemplateUtils.substitute(template, message);

		// assert - both should return same result

		assertThat(result1).isEqualTo("keycloak/master/events/login");
		assertThat(result2).isEqualTo(result1);
	}

	@Test
	void shouldSubstituteResultUpperCaseSuccess() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, "LOGIN", null, null, false, null, Constants.SUCCESS);
		var template = "events/${resultUpperCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("events/SUCCESS");
	}

	@Test
	void shouldSubstituteResultLowerCaseSuccess() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, "LOGIN", null, null, false, null, Constants.SUCCESS);
		var template = "events/${resultLowerCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("events/success");
	}

	@Test
	void shouldSubstituteResultUpperCaseError() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, "LOGIN_ERROR", null, null, false, null, "ERROR");
		var template = "events/${resultUpperCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("events/ERROR");
	}

	@Test
	void shouldSubstituteResultLowerCaseError() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage(null, null, null, "LOGIN_ERROR", null, null, false, null, "ERROR");
		var template = "events/${resultLowerCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("events/error");
	}

	@Test
	void shouldSubstituteComplexAdminEventTemplate() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage("PRODUCTION", null, null, null, null, "USER", true, "CREATE", Constants.SUCCESS);
		var template = "${kindLowerCase}/${realmLowerCase}/${resourceTypeLowerCase}/${operationTypeLowerCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("admin_event/production/user/create");
	}

	@Test
	void shouldSubstituteComplexTemplateWithResult() {

		// arrange

		TemplateUtils.clearCache();

		var message = createMessage("MASTER", null, null, "LOGIN_ERROR", null, null, false, null, "ERROR");
		var template = "${realmLowerCase}/${eventTypeLowerCase}/${resultLowerCase}";

		// act

		var result = TemplateUtils.substitute(template, message);

		// assert

		assertThat(result).isEqualTo("master/login_error/error");
	}
}
