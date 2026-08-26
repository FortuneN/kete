package io.github.fortunen.kete.unittests.utils.redisutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.utils.RedisUtils;
import io.lettuce.core.ClientOptions;

public class createClientMaterialTests {

	private static TlsMaterial tlsDisabled() {
		return TlsMaterial.builder().build();
	}

	private static RedisUtils.Settings settings(String host, int port, String username, String password) {
		return new RedisUtils.Settings(host, port, 0, false, username, password, "kete", 7, 3);
	}

	@Test
	public void shouldBuildStandaloneUriWithTimeoutAndAuthentication() {

		// act

		var material = RedisUtils.createStandalone(settings("redis.example", 6380, "user", "secret"), tlsDisabled());

		// assert

		assertThat(material.redisUri().getHost()).isEqualTo("redis.example");
		assertThat(material.redisUri().getPort()).isEqualTo(6380);
		assertThat(material.redisUri().getClientName()).isEqualTo("kete");
		assertThat(material.redisUri().getTimeout()).isEqualTo(Duration.ofSeconds(7));
		var credentials = material.redisUri().getCredentialsProvider().resolveCredentials().block();
		assertThat(credentials.getUsername()).isEqualTo("user");
		assertThat(credentials.getPassword()).containsExactly("secret".toCharArray());
		assertThat(material.redisUri().isSsl()).isFalse();
		assertThat(material.clientOptions().getDisconnectedBehavior()).isEqualTo(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS);
		assertThat(material.clientOptions().getSocketOptions().getConnectTimeout()).isEqualTo(Duration.ofSeconds(3));
		assertThat(material.clusterNodeUris()).isNull();
		assertThat(material.clusterClientOptions()).isNull();
	}

	@Test
	public void shouldKeepDefaultConnectTimeoutWhenConnectionTimeoutIsZero() {

		// arrange

		var settings = new RedisUtils.Settings("redis.example", 6379, 2, true, "", "", "kete", 10, 0);

		// act

		var material = RedisUtils.createStandalone(settings, tlsDisabled());

		// assert

		assertThat(material.redisUri().getDatabase()).isEqualTo(2);
		assertThat(material.redisUri().getCredentialsProvider().resolveCredentials().block().hasPassword()).isFalse();
		assertThat(material.clientOptions().getSocketOptions().getConnectTimeout()).isEqualTo(Duration.ofSeconds(10));
	}

	@Test
	public void shouldRequireHostForStandalone() {

		// act

		var thrown = catchThrowable(() -> RedisUtils.createStandalone(settings("", 6379, "", ""), tlsDisabled()));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("host is required for standalone mode");
	}

	@Test
	public void shouldBuildSentinelUriFromNodeList() {

		// act

		var material = RedisUtils.createSentinel(settings("", 6379, "", "secret"), "mymaster", "s1.example:26380, s2.example, s3.example:26381", tlsDisabled());

		// assert

		assertThat(material.redisUri().getSentinelMasterId()).isEqualTo("mymaster");
		assertThat(material.redisUri().getSentinels()).hasSize(3);
		assertThat(material.redisUri().getSentinels().get(0).getHost()).isEqualTo("s1.example");
		assertThat(material.redisUri().getSentinels().get(0).getPort()).isEqualTo(26380);
		assertThat(material.redisUri().getSentinels().get(1).getPort()).isEqualTo(RedisUtils.DEFAULT_SENTINEL_PORT);
		assertThat(material.redisUri().getSentinels().get(2).getPort()).isEqualTo(26381);
		assertThat(material.redisUri().getCredentialsProvider().resolveCredentials().block().getPassword()).containsExactly("secret".toCharArray());
		assertThat(material.clientOptions()).isNotNull();
	}

	@Test
	public void shouldBuildClusterNodeUrisWithDefaultPort() {

		// act

		var material = RedisUtils.createCluster(settings("", 7000, "", ""), "n1.example, n2.example:7001", tlsDisabled());

		// assert

		assertThat(material.clusterNodeUris()).hasSize(2);
		assertThat(material.clusterNodeUris().get(0).getHost()).isEqualTo("n1.example");
		assertThat(material.clusterNodeUris().get(0).getPort()).isEqualTo(7000);
		assertThat(material.clusterNodeUris().get(1).getPort()).isEqualTo(7001);
		assertThat(material.clusterNodeUris().get(0).getTimeout()).isEqualTo(Duration.ofSeconds(7));
		assertThat(material.clusterClientOptions().getDisconnectedBehavior()).isEqualTo(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS);
		assertThat(material.redisUri()).isNull();
		assertThat(material.clientOptions()).isNull();
	}

	@Test
	public void shouldNotCreateSslOptionsWhenTlsIsDisabled() {

		// act & assert

		assertThat(RedisUtils.createSslOptions(tlsDisabled())).isNull();
	}

	@Test
	public void shouldRejectNegativeTimeouts() {

		// act

		var thrown = catchThrowable(() -> new RedisUtils.Settings("h", 6379, 0, false, "", "", "kete", -1, 0));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("commandTimeoutSeconds must be non-negative");
	}
}
