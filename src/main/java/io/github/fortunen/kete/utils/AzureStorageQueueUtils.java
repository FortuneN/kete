package io.github.fortunen.kete.utils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.SneakyThrows;

public final class AzureStorageQueueUtils {

	public static final String WELL_KNOWN_ACCOUNT_KEY = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";

	static final String HMAC_SHA256 = "HmacSHA256";

	private static final String SIGNATURE_VERB_PART = "POST\n\n\n";
	private static final String SIGNATURE_CONTENT_TYPE_PART = "\n\napplication/xml\n\n\n\n\n\n\n";

	public record ConnectionStringInfo(String accountName, String accountKey, String sasToken, String url, boolean useSasAuth) {}

	private AzureStorageQueueUtils() {}

	public static ConnectionStringInfo parseConnectionString(String connectionString) {

		ValidationUtils.requireNonBlank(connectionString, "connectionString is required");

		var parts = parseKeyValuePairs(connectionString);

		// extract fields

		var csAccountName = parts.getOrDefault("AccountName", "").trim();
		var csAccountKey = parts.getOrDefault("AccountKey", "").trim();
		var csSas = parts.getOrDefault("SharedAccessSignature", "").trim();
		var csQueueEndpoint = parts.getOrDefault("QueueEndpoint", "").trim();
		var csProtocol = parts.getOrDefault("DefaultEndpointsProtocol", "https").trim();
		var csSuffix = parts.getOrDefault("EndpointSuffix", "core.windows.net").trim();

		// auth — exactly one of AccountKey or SharedAccessSignature

		var hasKey = ValidationUtils.isNotBlank(csAccountKey);
		var hasSas = ValidationUtils.isNotBlank(csSas);
		var hasAccountName = ValidationUtils.isNotBlank(csAccountName);

		ValidationUtils.requireTrue(hasKey || hasSas, "connection-string must contain AccountKey or SharedAccessSignature");
		ValidationUtils.requireFalse(hasKey && hasSas, "connection-string must not contain both AccountKey and SharedAccessSignature");

		var useSasAuth = hasSas;
		String sasToken = null;
		String accountKey = null;

		if (hasSas) {
			sasToken = csSas.startsWith("?") ? csSas.substring(1) : csSas;
		} else {
			accountKey = csAccountKey;
		}

		// account name (required for shared-key; optional for SAS if QueueEndpoint is present)

		var accountName = hasAccountName ? csAccountName : null;

		if (!useSasAuth) {
			ValidationUtils.requireNonBlank(accountName, "connection-string must contain AccountName for shared-key auth");
		}

		// url — QueueEndpoint takes precedence, else derive from protocol/account/suffix

		String url;

		if (ValidationUtils.isNotBlank(csQueueEndpoint)) {

			url = csQueueEndpoint;

			if (hasAccountName) {
				ValidationUtils.requireTrue(url.contains(csAccountName), "connection-string QueueEndpoint must contain the AccountName");
			}

		} else {

			ValidationUtils.requireNonBlank(accountName, "connection-string must contain AccountName or QueueEndpoint");

			url = csProtocol + "://" + csAccountName + ".queue." + csSuffix;
		}

		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}

		return new ConnectionStringInfo(accountName, accountKey, sasToken, url, useSasAuth);
	}

	public static String buildStringToSign(int contentLength, String date, String apiVersion, String canonicalResource) {

		ValidationUtils.requireNonNull(date, "date is required");
		ValidationUtils.requireNonNull(apiVersion, "apiVersion is required");
		ValidationUtils.requireNonNull(canonicalResource, "canonicalResource is required");

		return SIGNATURE_VERB_PART
			+ contentLength
			+ SIGNATURE_CONTENT_TYPE_PART
			+ "x-ms-date:" + date + "\n"
			+ "x-ms-version:" + apiVersion + "\n"
			+ canonicalResource;
	}

	@SneakyThrows
	public static String computeSignature(SecretKeySpec secretKeySpec, String stringToSign) {

		ValidationUtils.requireNonNull(secretKeySpec, "secretKeySpec is required");
		ValidationUtils.requireNonNull(stringToSign, "stringToSign is required");

		var mac = Mac.getInstance(HMAC_SHA256);
		mac.init(secretKeySpec);
		return Base64Utils.encode(mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
	}

	public static SecretKeySpec buildSecretKeySpec(String base64AccountKey) {
		return new SecretKeySpec(Base64Utils.decode(base64AccountKey), HMAC_SHA256);
	}

	private static Map<String, String> parseKeyValuePairs(String connectionString) {
		var parts = new HashMap<String, String>();
		for (var segment : connectionString.split(";")) {
			var eq = segment.indexOf('=');
			if (eq > 0) {
				parts.put(segment.substring(0, eq).trim(), segment.substring(eq + 1).trim());
			}
		}
		return parts;
	}
}
