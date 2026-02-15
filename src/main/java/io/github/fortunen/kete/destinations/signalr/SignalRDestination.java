package io.github.fortunen.kete.destinations.signalr;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import com.microsoft.signalr.HubConnection;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Component(name = "signalr")
public class SignalRDestination extends Destination<SignalRDestinationConfig> {

	private String hubMethod;
	private int timeoutSeconds;
	private HubConnection hubConnection;
	private boolean isHubMethodTemplated;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		hubMethod = config.getHubMethod();
		timeoutSeconds = config.getTimeoutSeconds();
		isHubMethodTemplated = config.isHubMethodTemplated();
		hubConnection = config.getHubConnectionBuilder().build();

		// verify connection

		hubConnection.start().blockingAwait(timeoutSeconds, TimeUnit.SECONDS);
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		var actualHubMethod = isHubMethodTemplated ? TemplateUtils.substitute(hubMethod, message) : hubMethod;

		var body = new String(message.eventBody(), StandardCharsets.UTF_8);

		hubConnection.send(actualHubMethod, body);
	}

	@Override
	public void close() {
		if (ValidationUtils.isNotNull(hubConnection)) {
			try {
				hubConnection.stop().blockingAwait(timeoutSeconds, TimeUnit.SECONDS);
			} catch (Exception ignored) {
				// best-effort cleanup
			}
		}
	}
}
