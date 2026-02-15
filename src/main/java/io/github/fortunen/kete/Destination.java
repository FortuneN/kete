package io.github.fortunen.kete;

import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(exclude = "config")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = false, exclude = "config")
public abstract class Destination<TConfig extends DestinationConfig> implements AutoCloseable {

	protected TConfig config;

	protected abstract void doInitialize();

	protected abstract void doSend(EventMessage message);

	public final void initialize() {
		ValidationUtils.requireNonNull(config, "config is required");
		doInitialize();
	}

	public final void send(EventMessage message) {
		ValidationUtils.requireNonNull(message, "message is required");
		doSend(message);
	}

	protected byte[] encodePayload(byte[] payload) {

		if (config.getContentEncoding() != null) {
			payload = config.getContentEncoding().encode(payload);
		}

		if (config.getContentTransferEncoding() != null) {
			payload = config.getContentTransferEncoding().encode(payload);
		}

		return payload;
	}
}
