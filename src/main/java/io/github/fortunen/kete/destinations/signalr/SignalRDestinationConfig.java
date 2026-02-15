package io.github.fortunen.kete.destinations.signalr;

import javax.net.ssl.X509TrustManager;

import com.microsoft.signalr.HttpHubConnectionBuilder;
import com.microsoft.signalr.HubConnectionBuilder;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.OAuthMaterial;
import io.github.fortunen.kete.utils.ConfigurationUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.reactivex.rxjava3.core.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class SignalRDestinationConfig extends DestinationConfig {

	public static final String URL = "url";
	public static final String OAUTH = "oauth";
	public static final String HUB_METHOD = "hub-method";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String ACCESS_TOKEN = "access-token";
	public static final String DEFAULT_HUB_METHOD = "SendEvent";
	public static final String TIMEOUT_SECONDS = "timeout-seconds";

	private String url;
	private String hubMethod;
	private int timeoutSeconds;
	private String accessToken;
	private OAuthMaterial oauth;
	private boolean oauthEnabled;
	private boolean isHubMethodTemplated;
	private HttpHubConnectionBuilder hubConnectionBuilder;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// url (required)

		url = ValidationUtils.requireNonBlank(configuration.getString(URL, "").trim(), URL + " is required");

		// hub-method

		hubMethod = configuration.getString(HUB_METHOD, DEFAULT_HUB_METHOD).trim();

		ValidationUtils.requireNonBlank(hubMethod, HUB_METHOD + " must not be blank");

		// timeout-seconds

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");

		// access-token

		accessToken = configuration.getString(ACCESS_TOKEN, "").trim();

		// oauth

		oauth = OAuthMaterial.builder().withKeycloakRealm(keycloakRealm).withKeycloakSession(keycloakSession).withConfiguration(ConfigurationUtils.getSubSet(configuration, OAUTH)).build();

		oauthEnabled = ValidationUtils.isNotNull(oauth) && oauth.isEnabled();

		// precomputed fields

		isHubMethodTemplated = TemplateUtils.containsTemplate(hubMethod);

		// builder

		var builder = HubConnectionBuilder.create(url);

		// headers

		for (var entry : customHeaders.entrySet()) {
			builder.withHeader(entry.getKey(), entry.getValue());
		}

		// access-token / oauth

		if (oauthEnabled) {
			builder.withAccessTokenProvider(Single.fromCallable(() -> oauth.getAccessToken().getValue()));
		} else if (ValidationUtils.isNotBlank(accessToken)) {
			builder.withAccessTokenProvider(Single.just(accessToken));
		}

		// timeout

		builder.withHandshakeResponseTimeout(timeoutSeconds * 1000L);

		// tls

		if (tls.isEnabled()) {
			var sslContext = tls.getKeyStoreAndTrustStoreSSLContext();
			builder.setHttpClientBuilderCallback(httpBuilder -> {
				if (tls.getTrustManagerFactory() != null) {
					var trustManager = (X509TrustManager) tls.getTrustManagerFactory().getTrustManagers()[0];
					httpBuilder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
				}
				if (!tls.isVerifyHostname()) {
					httpBuilder.hostnameVerifier((hostname, session) -> true);
				}
			});
		}

		hubConnectionBuilder = builder;
	}
}
