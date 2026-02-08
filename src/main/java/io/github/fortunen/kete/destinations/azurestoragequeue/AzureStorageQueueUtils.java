package io.github.fortunen.kete.destinations.azurestoragequeue;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.github.fortunen.kete.utils.Base64Utils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.SneakyThrows;

public final class AzureStorageQueueUtils {

	static final String HMAC_SHA256 = "HmacSHA256";
	static final String EMULATOR_ACCOUNT_NAME = "devstoreaccount1";
	static final String EMULATOR_ACCOUNT_KEY = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";
	static final String DEFAULT_EMULATOR_QUEUE_ENDPOINT = "http://127.0.0.1:10001/devstoreaccount1";

	private static final String SIGNATURE_VERB_PART = "POST\n\n\n";
	private static final String SIGNATURE_CONTENT_TYPE_PART = "\n\napplication/xml\n\n\n\n\n\n";

	public record ConnectionStringInfo(String accountName, String accountKey, String sasToken, String url, boolean useSasAuth) {}

	private AzureStorageQueueUtils() {}

	public static ConnectionStringInfo parseConnectionString(String connectionString) {

		var parts = parseKeyValuePairs(connectionString);

		// UseDevelopmentStorage=true (emulator shorthand)

		if ("true".equalsIgnoreCase(parts.get("UseDevelopmentStorage"))) {
			var proxyUri = parts.getOrDefault("DevelopmentStorageProxyUri", "").trim();
			String emulatorUrl;
			if (ValidationUtils.isNotBlank(proxyUri)) {
				if (proxyUri.endsWith("/")) proxyUri = proxyUri.substring(0, proxyUri.length() - 1);
				emulatorUrl = proxyUri + ":10001/" + EMULATOR_ACCOUNT_NAME;
			} else {
				emulatorUrl = DEFAULT_EMULATOR_QUEUE_ENDPOINT;
			}
			return new ConnectionStringInfo(EMULATOR_ACCOUNT_NAME, EMULATOR_ACCOUNT_KEY, null, emulatorUrl, false);
		}

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

		return new ConnectionStringInfo(accountName, accountKey, sasToken, url, useSasAuth);
	}

	public static String buildStringToSign(int contentLength, String date, String apiVersion, String canonicalResource) {
		return SIGNATURE_VERB_PART
			+ contentLength
			+ SIGNATURE_CONTENT_TYPE_PART
			+ "x-ms-date:" + date + "\n"
			+ "x-ms-version:" + apiVersion + "\n"
			+ canonicalResource;
	}

	@SneakyThrows
	public static String computeSignature(SecretKeySpec secretKeySpec, String stringToSign) {
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
