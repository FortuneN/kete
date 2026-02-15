package io.github.fortunen.kete.utils;

import java.time.Duration;
import java.util.function.Consumer;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;

import org.apache.commons.configuration2.MapConfiguration;

import io.github.fortunen.kete.TlsMaterial;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.SneakyThrows;

public final class AzureUtils {

	private AzureUtils() {}

	public static final String MANAGED_IDENTITY_CLIENT_ID = "managed-identity-client-id";

	public static void configureAuthentication(String authenticationType, MapConfiguration configuration, String primaryAuthenticationType, Runnable onPrimaryAuth, Consumer<TokenCredential> onTokenCredential) {
		if (authenticationType.equals(primaryAuthenticationType)) {
			onPrimaryAuth.run();
		} else if ("managed-identity".equals(authenticationType)) {
			onTokenCredential.accept(createManagedIdentityCredential(configuration));
		} else if ("default-azure-credential".equals(authenticationType)) {
			onTokenCredential.accept(createDefaultAzureCredential());
		} else {
			throw new IllegalStateException("unsupported authentication-type: '" + authenticationType + "' — valid options: " + primaryAuthenticationType + ", managed-identity, default-azure-credential");
		}
	}

	public static TokenCredential createManagedIdentityCredential(MapConfiguration configuration) {
		
        var miBuilder = new ManagedIdentityCredentialBuilder();
		var clientId = configuration.getString(MANAGED_IDENTITY_CLIENT_ID, "").trim();
		
        if (ValidationUtils.isNotBlank(clientId)) {
			miBuilder.clientId(clientId);
		}

		return miBuilder.build();
	}

	public static TokenCredential createDefaultAzureCredential() {
		return new DefaultAzureCredentialBuilder().build();
	}

	@SneakyThrows
	public static HttpClient createHttpClient(TlsMaterial tls, Duration timeout) {
		if (tls.isEnabled()) {
			
            var sslBuilder = SslContextBuilder.forClient();
			
            if (ValidationUtils.isNotNull(tls.getTrustManagerFactory())) {
				sslBuilder.trustManager(tls.getTrustManagerFactory());
			}

			if (ValidationUtils.isNotNull(tls.getKeyManagerFactory())) {
				sslBuilder.keyManager(tls.getKeyManagerFactory());
			}

			var sslContext = sslBuilder.build();
			var reactorHttpClient = reactor.netty.http.client.HttpClient.create().secure(spec -> spec.sslContext(sslContext));
			
            return new NettyAsyncHttpClientBuilder(reactorHttpClient).connectTimeout(timeout).readTimeout(timeout).writeTimeout(timeout).build();

		} else {

			return new NettyAsyncHttpClientBuilder().connectTimeout(timeout).readTimeout(timeout).writeTimeout(timeout).build();

		}
	}
}
