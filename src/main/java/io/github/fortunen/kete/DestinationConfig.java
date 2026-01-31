package io.github.fortunen.kete;

import org.apache.commons.configuration2.MapConfiguration;
import org.keycloak.models.KeycloakSession;

import io.github.fortunen.kete.utils.ConfigurationUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@NoArgsConstructor(force = true)
@ToString(exclude = "keycloakSession")
@EqualsAndHashCode(callSuper = false, exclude = "keycloakSession")
public abstract class DestinationConfig {

	public static final String TLS = "tls";
	public static final int DEFAULT_MIN_POOL_SIZE = 5;
	public static final int DEFAULT_MAX_POOL_SIZE = 20;
	public static final String MIN_POOL_SIZE = "min-pool-size";
	public static final String MAX_POOL_SIZE = "max-pool-size";
	public static final String MESSAGE_HEADERS_ENABLED = "message-headers-enabled";

	protected TlsMaterial tls;
	protected String keycloakRealm;
	protected String destinationKind;
	protected MapConfiguration configuration;
	protected int minPoolSize = DEFAULT_MIN_POOL_SIZE;
	protected int maxPoolSize = DEFAULT_MAX_POOL_SIZE;

	@Setter
	protected KeycloakSession keycloakSession;

	protected abstract void doInitialize();

	public final void initialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// pool sizes (optional)

		minPoolSize = configuration.getInt(MIN_POOL_SIZE, DEFAULT_MIN_POOL_SIZE);
		maxPoolSize = configuration.getInt(MAX_POOL_SIZE, DEFAULT_MAX_POOL_SIZE);

		ValidationUtils.requireGreaterThan(minPoolSize, 0, MIN_POOL_SIZE + " must be greater than 0");
		ValidationUtils.requireGreaterThan(maxPoolSize, 0, MAX_POOL_SIZE + " must be greater than 0");
		ValidationUtils.requireTrue(maxPoolSize >= minPoolSize, MAX_POOL_SIZE + " must be >= " + MIN_POOL_SIZE);

		// tls

		tls = TlsMaterial.builder()
			.withConfiguration(ConfigurationUtils.getSubSet(configuration, TLS))
			.build();

		// initialize subclass

		doInitialize();
	}
}
