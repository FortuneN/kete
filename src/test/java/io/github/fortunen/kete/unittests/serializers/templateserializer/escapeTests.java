package io.github.fortunen.kete.unittests.serializers.templateserializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.serializers.TemplateSerializer;

public class escapeTests {

	// region json / json-encode

	@Test
	public void shouldEscapeJsonQuotes() {
		assertThat(TemplateSerializer.escape("he said \"hello\"", "json")).isEqualTo("he said \\\"hello\\\"");
	}

	@Test
	public void shouldEscapeJsonBackslash() {
		assertThat(TemplateSerializer.escape("path\\to\\file", "json")).isEqualTo("path\\\\to\\\\file");
	}

	@Test
	public void shouldEscapeJsonNewline() {
		assertThat(TemplateSerializer.escape("line1\nline2", "json")).isEqualTo("line1\\nline2");
	}

	@Test
	public void shouldEscapeJsonTab() {
		assertThat(TemplateSerializer.escape("col1\tcol2", "json")).isEqualTo("col1\\tcol2");
	}

	@Test
	public void shouldEscapeJsonCarriageReturn() {
		assertThat(TemplateSerializer.escape("line1\rline2", "json")).isEqualTo("line1\\rline2");
	}

	@Test
	public void shouldEscapeJsonBackspace() {
		assertThat(TemplateSerializer.escape("back\bspace", "json")).isEqualTo("back\\bspace");
	}

	@Test
	public void shouldEscapeJsonFormFeed() {
		assertThat(TemplateSerializer.escape("form\ffeed", "json")).isEqualTo("form\\ffeed");
	}

	@Test
	public void shouldEscapeJsonControlCharacter() {
		assertThat(TemplateSerializer.escape("null\u0000char", "json")).isEqualTo("null\\u0000char");
	}

	@Test
	public void shouldHandleJsonEncodeAlias() {
		assertThat(TemplateSerializer.escape("\"test\"", "json-encode")).isEqualTo("\\\"test\\\"");
	}

	// endregion

	// region xml / xml-encode

	@Test
	public void shouldEscapeXmlAmpersand() {
		assertThat(TemplateSerializer.escape("a&b", "xml")).isEqualTo("a&amp;b");
	}

	@Test
	public void shouldEscapeXmlLessThan() {
		assertThat(TemplateSerializer.escape("a<b", "xml")).isEqualTo("a&lt;b");
	}

	@Test
	public void shouldEscapeXmlGreaterThan() {
		assertThat(TemplateSerializer.escape("a>b", "xml")).isEqualTo("a&gt;b");
	}

	@Test
	public void shouldEscapeXmlDoubleQuote() {
		assertThat(TemplateSerializer.escape("a\"b", "xml")).isEqualTo("a&quot;b");
	}

	@Test
	public void shouldEscapeXmlSingleQuote() {
		assertThat(TemplateSerializer.escape("a'b", "xml")).isEqualTo("a&apos;b");
	}

	@Test
	public void shouldHandleXmlEncodeAlias() {
		assertThat(TemplateSerializer.escape("<tag>", "xml-encode")).isEqualTo("&lt;tag&gt;");
	}

	// endregion

	// region url / url-encode

	@Test
	public void shouldUrlEncodeSpaces() {
		assertThat(TemplateSerializer.escape("hello world", "url")).isEqualTo("hello+world");
	}

	@Test
	public void shouldUrlEncodeSpecialChars() {
		assertThat(TemplateSerializer.escape("a=b&c=d", "url")).isEqualTo("a%3Db%26c%3Dd");
	}

	@Test
	public void shouldHandleUrlEncodeAlias() {
		assertThat(TemplateSerializer.escape("hello world", "url-encode")).isEqualTo("hello+world");
	}

	// endregion

	// region csv / csv-quote

	@Test
	public void shouldCsvQuoteWhenContainsComma() {
		assertThat(TemplateSerializer.escape("a,b", "csv")).isEqualTo("\"a,b\"");
	}

	@Test
	public void shouldCsvQuoteWhenContainsDoubleQuote() {
		assertThat(TemplateSerializer.escape("he said \"hi\"", "csv")).isEqualTo("\"he said \"\"hi\"\"\"");
	}

	@Test
	public void shouldCsvQuoteWhenContainsNewline() {
		assertThat(TemplateSerializer.escape("line1\nline2", "csv")).isEqualTo("\"line1\nline2\"");
	}

	@Test
	public void shouldNotCsvQuoteSimpleValue() {
		assertThat(TemplateSerializer.escape("simple", "csv")).isEqualTo("simple");
	}

	@Test
	public void shouldHandleCsvQuoteAlias() {
		assertThat(TemplateSerializer.escape("a,b", "csv-quote")).isEqualTo("\"a,b\"");
	}

	// endregion

	// region b64 / base64

	@Test
	public void shouldBase64Encode() {
		assertThat(TemplateSerializer.escape("hello", "b64")).isEqualTo("aGVsbG8=");
	}

	@Test
	public void shouldHandleBase64Alias() {
		assertThat(TemplateSerializer.escape("hello", "base64")).isEqualTo("aGVsbG8=");
	}

	// endregion

	// region none

	@Test
	public void shouldPassthroughWithNone() {
		assertThat(TemplateSerializer.escape("<\"test\">&", "none")).isEqualTo("<\"test\">&");
	}

	// endregion

	// region null/edge cases

	@Test
	public void shouldReturnEmptyStringForNull() {
		assertThat(TemplateSerializer.escape(null, "json")).isEmpty();
	}

	@Test
	public void shouldPassthroughForUnknownFilter() {
		assertThat(TemplateSerializer.escape("test", "unknown-filter")).isEqualTo("test");
	}

	@Test
	public void shouldHandleEmptyString() {
		assertThat(TemplateSerializer.escape("", "json")).isEmpty();
	}

	// endregion

	// region snakeToCamelCase

	@Test
	public void shouldConvertSnakeCaseToCamelCase() {
		assertThat(TemplateSerializer.snakeToCamelCase("redirect_uri")).isEqualTo("redirectUri");
	}

	@Test
	public void shouldHandleMultipleUnderscores() {
		assertThat(TemplateSerializer.snakeToCamelCase("auth_session_state")).isEqualTo("authSessionState");
	}

	@Test
	public void shouldReturnSameWhenNoCamelCase() {
		assertThat(TemplateSerializer.snakeToCamelCase("username")).isEqualTo("username");
	}

	@Test
	public void shouldHandleNullInput() {
		assertThat(TemplateSerializer.snakeToCamelCase(null)).isNull();
	}

	// endregion
}
