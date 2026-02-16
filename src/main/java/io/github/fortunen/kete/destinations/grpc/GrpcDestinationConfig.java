package io.github.fortunen.kete.destinations.grpc;

import java.time.Duration;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.grpc.MethodDescriptor;
import io.grpc.okhttp.OkHttpChannelBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class GrpcDestinationConfig extends DestinationConfig {

	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String METHOD = "method";
	public static final int DEFAULT_PORT = 50051;
	public static final String SERVICE = "service";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String USE_PLAINTEXT = "use-plaintext";
	public static final String TIMEOUT_SECONDS = "timeout-seconds";

	private int port;
	private String host;
	private String method;
	private String service;
	private Duration timeout;
	private int timeoutSeconds;
	private boolean usePlaintext;
	private String fullMethodName;
	private OkHttpChannelBuilder channelBuilder;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// host

		host = ValidationUtils.requireNonBlank(configuration.getString(HOST, "").trim(), HOST + " is required");

		// port

		port = configuration.getInt(PORT, DEFAULT_PORT);

		ValidationUtils.requireValidPort(port, PORT);

		// service

		service = ValidationUtils.requireNonBlank(configuration.getString(SERVICE, "").trim(), SERVICE + " is required");

		// method

		method = ValidationUtils.requireNonBlank(configuration.getString(METHOD, "").trim(), METHOD + " is required");

		// timeout-seconds

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");

		// timeout

		timeout = Duration.ofSeconds(timeoutSeconds);

		// use-plaintext

		usePlaintext = configuration.getBoolean(USE_PLAINTEXT, false);

		// precomputed

		fullMethodName = MethodDescriptor.generateFullMethodName(service, method);

		// channel builder

		channelBuilder = OkHttpChannelBuilder.forAddress(host, port);

		if (usePlaintext) {
			channelBuilder.usePlaintext();
		} else if (tls.isEnabled()) {
			channelBuilder.sslSocketFactory(tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory());
		}
	}
}
