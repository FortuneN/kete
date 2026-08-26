package io.github.fortunen.kete.destinations.mqtt5;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.MqttUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"password"})
public class Mqtt5DestinationConfig extends DestinationConfig {

	public static final int DEFAULT_CONNECTION_TIMEOUT_SECONDS = 10;
	public static final int DEFAULT_KEEP_ALIVE_INTERVAL_SECONDS = 60;

	private int qos;
	private int port;
	private String url;
	private String host;
	private String topic;
	private String scheme;
	private String username;
	private String password;
	private int maxInflight;
	private boolean retained;
	private boolean cleanSession;
	private String transportType;
	private String clientIdPrefix;
	private boolean isTopicTemplated;
	private int keepAliveIntervalSeconds;
	private int connectionTimeoutSeconds;
	private MqttConnectionOptions connectOptions;
	private List<UserProperty> staticUserProperties;
	private AtomicInteger clientIdCounter = new AtomicInteger(0);

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// settings shared by MQTT 3 and MQTT 5

		var settings = MqttUtils.parseSettings(configuration, tls);

		host = settings.host();
		transportType = settings.transportType();
		scheme = settings.scheme();
		port = settings.port();
		url = settings.url();
		topic = settings.topic();
		qos = settings.qos();
		retained = settings.retained();
		clientIdPrefix = settings.clientIdPrefix();
		cleanSession = settings.cleanSession();
		connectionTimeoutSeconds = settings.connectionTimeoutSeconds();
		keepAliveIntervalSeconds = settings.keepAliveIntervalSeconds();
		maxInflight = settings.maxInflight();
		username = settings.username();
		password = settings.password();

		// precomputed fields

		isTopicTemplated = TemplateUtils.containsTemplate(topic);

		staticUserProperties = new ArrayList<>();

		for (var entry : customHeadersEntrySet) {
			staticUserProperties.add(new UserProperty(entry.getKey(), entry.getValue()));
		}

		// connectOptions

		connectOptions = new MqttConnectionOptions();
		connectOptions.setReceiveMaximum(maxInflight);
		connectOptions.setConnectionTimeout(connectionTimeoutSeconds);
		connectOptions.setAutomaticReconnect(true);

		if (settings.hasCleanSession()) {
			connectOptions.setCleanStart(cleanSession);
		}

		if (settings.hasKeepAliveInterval()) {
			connectOptions.setKeepAliveInterval(keepAliveIntervalSeconds);
		}

		if (ValidationUtils.isNotBlank(username)) {
			connectOptions.setUserName(username);
		}

		if (ValidationUtils.isNotBlank(password)) {
			connectOptions.setPassword(password.getBytes(StandardCharsets.UTF_8));
		}

		if (tls.isEnabled()) {
			connectOptions.setSocketFactory(tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory());
			connectOptions.setHttpsHostnameVerificationEnabled(tls.isVerifyHostname());
		}
	}
}
