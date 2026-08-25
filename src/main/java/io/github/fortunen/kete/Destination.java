package io.github.fortunen.kete;

import io.github.fortunen.kete.utils.ContentTypeUtils;
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

	// cheap, non-blocking health probe used by the destination pool's test-on-* checks;
	// implementations must only read client state (no network I/O)

	public abstract boolean isHealthy();

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

	// character data can travel over text-only transports; a content encoding makes the body binary unless a transfer encoding turns it back into text

	protected boolean isTextPayload(String contentType) {

		if (config.getContentEncoding() != null && config.getContentTransferEncoding() == null) {
			return false;
		}

		return ContentTypeUtils.isText(contentType);
	}
}
