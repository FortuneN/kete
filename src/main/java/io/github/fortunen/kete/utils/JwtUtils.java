package io.github.fortunen.kete.utils;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.token.AccessToken;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JwtUtils {

	public static final long TOKEN_REFRESH_BUFFER_SECONDS = 30;
	public static final long DEFAULT_TOKEN_LIFETIME_SECONDS = 3600;

	private JwtUtils() {}

	public static AccessToken exchange(TokenRequest tokenRequest) {
		return exchange(tokenRequest, 0);
	}

	@SneakyThrows
	public static AccessToken exchange(TokenRequest tokenRequest, int timeoutSeconds) {

		ValidationUtils.requireNonNull(tokenRequest, "tokenRequest is required");
		ValidationUtils.requireNonNegative(timeoutSeconds, "timeoutSeconds must be non-negative");

		var httpRequest = tokenRequest.toHTTPRequest();

		if (timeoutSeconds > 0) {
			var timeoutMillis = (int) TimeUnit.SECONDS.toMillis(timeoutSeconds);
			httpRequest.setConnectTimeout(timeoutMillis);
			httpRequest.setReadTimeout(timeoutMillis);
		}

		var tokenResponse = TokenResponse.parse(httpRequest.send());

		ValidationUtils.requireTrue(tokenResponse.indicatesSuccess(), () -> new IOException("OAuth token request failed: " + tokenResponse.toErrorResponse().getErrorObject()));

		return tokenResponse.toSuccessResponse().getTokens().getAccessToken();
	}

	public static class CachedTokenHolder {

		private Instant tokenExpiryTime;
		private AccessToken cachedAccessToken;
		private final ReentrantLock lock = new ReentrantLock();

		public AccessToken getToken(Supplier<TokenRequest> tokenRequestFactory) {
			return getToken(0, tokenRequestFactory);
		}

		@SneakyThrows
		public AccessToken getToken(int timeoutSeconds, Supplier<TokenRequest> tokenRequestFactory) {

			ValidationUtils.requireNonNull(tokenRequestFactory, "tokenRequestFactory is required");

			lock.lock();

			try {

				// return cached

				if (ValidationUtils.isNotNull(cachedAccessToken) && ValidationUtils.isNotNull(tokenExpiryTime) && Instant.now().isBefore(tokenExpiryTime)) {
					return cachedAccessToken;
				}

				// fetch new

				cachedAccessToken = exchange(tokenRequestFactory.get(), timeoutSeconds);

				var lifetimeSeconds = cachedAccessToken.getLifetime() > 0 ? cachedAccessToken.getLifetime() : DEFAULT_TOKEN_LIFETIME_SECONDS;

				tokenExpiryTime = Instant.now().plusSeconds(lifetimeSeconds - TOKEN_REFRESH_BUFFER_SECONDS);

				return cachedAccessToken;

			} finally {
				lock.unlock();
			}
		}
	}
}
