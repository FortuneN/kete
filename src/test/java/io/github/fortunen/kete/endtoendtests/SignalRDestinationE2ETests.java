package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

class SignalRDestinationE2ETests extends EndToEndTestBase {

	private static final int SERVER_PORT = 5000;

	private static final String PROGRAM_CS = """
		using Microsoft.AspNetCore.SignalR;
		using System.Collections.Concurrent;

		var messages = new ConcurrentBag<string>();

		var builder = WebApplication.CreateBuilder();
		builder.Services.AddSignalR();
		builder.Services.AddSingleton(messages);

		var app = builder.Build();

		app.MapHub<EventHub>("/hub");
		app.MapGet("/messages", (ConcurrentBag<string> msgs) => msgs.ToArray());

		app.Run();

		public class EventHub : Hub
		{
		    private readonly ConcurrentBag<string> _messages;
		    public EventHub(ConcurrentBag<string> messages) => _messages = messages;
		    public void SendEvent(string data) => _messages.Add(data);
		}
		""";

	private static final String CSPROJ = """
		<Project Sdk="Microsoft.NET.Sdk.Web">
		  <PropertyGroup>
		    <TargetFramework>net8.0</TargetFramework>
		    <ImplicitUsings>enable</ImplicitUsings>
		  </PropertyGroup>
		</Project>
		""";

	private GenericContainer<?> signalrServer;

	@AfterEach
	void tearDown() {
		if (signalrServer != null) {
			try { signalrServer.stop(); } catch (Exception e) { /* ignore */ }
			signalrServer = null;
		}
		cleanupNetwork();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldForwardLoginEventToSignalRHub() throws Exception {

		// arrange — start SignalR server on Docker network

		signalrServer = new GenericContainer<>(DockerImageName.parse("mcr.microsoft.com/dotnet/sdk:8.0"))
			.withNetwork(createNetwork())
			.withNetworkAliases("signalr-server")
			.withExposedPorts(SERVER_PORT)
			.withEnv("ASPNETCORE_URLS", "http://0.0.0.0:" + SERVER_PORT)
			.withEnv("DOTNET_NOLOGO", "true")
			.withCopyToContainer(Transferable.of(PROGRAM_CS, 0777), "/app/Program.cs")
			.withCopyToContainer(Transferable.of(CSPROJ, 0777), "/app/SignalRServer.csproj")
			.withCommand("sh", "-c", "cd /app && dotnet run");
		signalrServer.start();

		waitForHttpReady(signalrServer, SERVER_PORT, "/messages");

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.signalr-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.signalr-test.destination.kind", "signalr");
		envVars.put("kete.routes.signalr-test.destination.url", "http://signalr-server:" + SERVER_PORT + "/hub");
		envVars.put("kete.routes.signalr-test.destination.hub-method", "SendEvent");
		envVars.put("kete.routes.signalr-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert — query the SignalR server directly to verify the hub invocation

				var client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(5)).build();
				var baseUrl = "http://" + "127.0.0.1" + ":" + signalrServer.getMappedPort(SERVER_PORT);

				await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).ignoreExceptions().until(() -> {
					var request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/messages")).timeout(Duration.ofSeconds(5)).GET().build();
					var response = client.send(request, HttpResponse.BodyHandlers.ofString());
					return response.statusCode() == 200 && !response.body().equals("[]");
				});

				var request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/messages")).timeout(Duration.ofSeconds(5)).GET().build();
				var response = client.send(request, HttpResponse.BodyHandlers.ofString());

				assertThat(response.statusCode()).isEqualTo(200);
				assertThat(response.body()).satisfiesAnyOf(
					b -> assertThat(b).contains("type"),
					b -> assertThat(b).contains("operationType")
				);
				assertThat(response.body()).contains(TEST_REALM);

				// cleanup
				cleanupTestRealm(adminClient);
			}
		}
	}
}
