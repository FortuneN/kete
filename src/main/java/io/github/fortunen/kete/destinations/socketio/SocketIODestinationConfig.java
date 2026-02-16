package io.github.fortunen.kete.destinations.socketio;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.X509TrustManager;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.OAuthMaterial;
import io.github.fortunen.kete.utils.ConfigurationUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.socket.client.IO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class SocketIODestinationConfig extends DestinationConfig {

    public static final String URL = "url";
	public static final String PATH = "path";
	public static final String OAUTH = "oauth";
	public static final String NAMESPACE = "namespace";
	public static final String EVENT_NAME = "event-name";
	public static final int DEFAULT_TIMEOUT_SECONDS = 20;
	public static final String DEFAULT_PATH = "/socket.io/";
	public static final String TIMEOUT_SECONDS = "timeout-seconds";

	private String url;
	private String path;
	private String socketUrl;
	private String eventName;
	private String namespace;
	private int timeoutSeconds;
	private OAuthMaterial oauth;
	private boolean oauthEnabled;
	private IO.Options options;
	private boolean isEventNameTemplated;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// url (required)

		url = ValidationUtils.requireNonBlank(configuration.getString(URL, "").trim(), URL + " is required");

		// event-name (required)

		eventName = ValidationUtils.requireNonBlank(configuration.getString(EVENT_NAME, "").trim(), EVENT_NAME + " is required");

		// namespace (optional)

		namespace = configuration.getString(NAMESPACE, "").trim();

		// path

		path = configuration.getString(PATH, DEFAULT_PATH).trim();

		// timeout-seconds

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");

		// precomputed fields

		isEventNameTemplated = TemplateUtils.containsTemplate(eventName);

		// oauth

		oauth = OAuthMaterial.builder().withKeycloakRealm(keycloakRealm).withKeycloakSession(keycloakSession).withConfiguration(ConfigurationUtils.getSubSet(configuration, OAUTH)).build();

		oauthEnabled = ValidationUtils.isNotNull(oauth) && oauth.isEnabled();

		// IO.Options

		options = new IO.Options();
		options.reconnection = true;
		options.timeout = timeoutSeconds * 1000L;

		if (configuration.containsKey(PATH)) {
			options.path = path;
		}

		// extra headers from custom headers

		if (!customHeaders.isEmpty()) {
			
            var extraHeaders = new HashMap<String, List<String>>();
			
            for (var entry : customHeaders.entrySet()) {
				extraHeaders.put(entry.getKey(), Collections.singletonList(entry.getValue()));
			}

			options.extraHeaders = extraHeaders;
		}

		// tls

		if (tls.isEnabled()) {

			var sslContext = tls.getKeyStoreAndTrustStoreSSLContext();
			var clientBuilder = new OkHttpClient.Builder().readTimeout(timeoutSeconds, TimeUnit.SECONDS);

			if (ValidationUtils.isNotNull(tls.getTrustManagerFactory())) {
				var trustManager = (X509TrustManager) tls.getTrustManagerFactory().getTrustManagers()[0];
				clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
			}

			if (!tls.isVerifyHostname()) {
				clientBuilder.hostnameVerifier((hostname, session) -> true);
			}

			var okHttpClient = clientBuilder.build();
			options.callFactory = okHttpClient;
			options.webSocketFactory = okHttpClient;
		}

		// socket url

		socketUrl = ValidationUtils.isNotBlank(namespace) ? url + (namespace.startsWith("/") ? namespace : "/" + namespace) : url;
	}
}
