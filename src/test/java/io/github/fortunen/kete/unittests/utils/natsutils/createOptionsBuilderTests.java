package io.github.fortunen.kete.unittests.utils.natsutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.utils.NatsUtils;

public class createOptionsBuilderTests {

	@Test
	public void shouldParseServersTrimmingAndDroppingBlanks() {

		// act

		var servers = NatsUtils.parseServers(" nats://a:4222 , ,nats://b:4222,");

		// assert

		assertThat(servers).containsExactly("nats://a:4222", "nats://b:4222");
	}

	@Test
	public void shouldThrowWhenServersIsBlank() {

		// act

		var thrown = catchThrowable(() -> NatsUtils.parseServers(" "));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("servers is required");
	}

	@Test
	public void shouldBuildOptionsWithTimeoutsNameAndFailFastReconnect() {

		// act

		var options = NatsUtils.createOptionsBuilder(new String[] { "nats://a:4222" }, 5, 30, "kete", TlsMaterial.builder().build()).build();

		// assert

		assertThat(options.getServers()).hasSize(1);
		assertThat(options.getConnectionTimeout()).isEqualTo(Duration.ofSeconds(5));
		assertThat(options.getPingInterval()).isEqualTo(Duration.ofSeconds(30));
		assertThat(options.getConnectionName()).isEqualTo("kete");
		assertThat(options.getMaxReconnect()).isEqualTo(-1);
		assertThat(options.getReconnectBufferSize()).isZero();
		assertThat(options.getSslContext()).isNull();
	}

	@Test
	public void shouldThrowWhenConnectionNameIsBlank() {

		// act

		var thrown = catchThrowable(() -> NatsUtils.createOptionsBuilder(new String[] { "nats://a:4222" }, 5, 30, "", TlsMaterial.builder().build()));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("connectionName is required");
	}
}
