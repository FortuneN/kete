package io.github.fortunen.kete.integrationtests.signalrdestination;

import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.signalr.SignalRDestination;
import io.github.fortunen.kete.destinations.signalr.SignalRDestinationConfig;

public class TestBase {

	private static final int SERVER_PORT = 5000;
	private static final int EVENTS_PORT = 5001;
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final String DOTNET_IMAGE = "mcr.microsoft.com/dotnet/sdk:8.0";

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
		    public void SendEvent(string data) => _messages.Add(data);
		    public EventHub(ConcurrentBag<string> messages) => _messages = messages;
		}
		""";

	private static final String PROGRAM_TLS_CS = """
		using Microsoft.AspNetCore.SignalR;
		using System.Collections.Concurrent;
		using Microsoft.AspNetCore.Server.Kestrel.Https;
		using System.Security.Cryptography.X509Certificates;

		var messages = new ConcurrentBag<string>();
		var builder = WebApplication.CreateBuilder();
		var caExists = File.Exists("/app/certs/ca.crt");

		builder.Services.AddSignalR();
		builder.Services.AddSingleton(messages);

		builder.WebHost.ConfigureKestrel(serverOptions =>
		{
		    serverOptions.ListenAnyIP(5000, listenOptions =>
		    {
		        var cert = X509Certificate2.CreateFromPemFile("/app/certs/server.crt", "/app/certs/server.key");
		        cert = new X509Certificate2(cert.Export(X509ContentType.Pfx));
		        
				if (caExists)
		        {
		            listenOptions.UseHttps(httpsOptions =>
		            {
		                httpsOptions.ServerCertificate = cert;
		                httpsOptions.CheckCertificateRevocation = false;
		                httpsOptions.ClientCertificateMode = ClientCertificateMode.RequireCertificate;

		                httpsOptions.ClientCertificateValidation = (certificate, chain, errors) =>
		                {
							chain.ChainPolicy.RevocationMode = X509RevocationMode.NoCheck;
		                    chain.ChainPolicy.TrustMode = X509ChainTrustMode.CustomRootTrust;
		                    chain.ChainPolicy.CustomTrustStore.Add(new X509Certificate2("/app/certs/ca.crt"));
		                    return chain.Build(certificate);
		                };
		            });
		        }
		        else
		        {
		            listenOptions.UseHttps(cert);
		        }
		    });

		    serverOptions.ListenAnyIP(5001);
		});

		var app = builder.Build();

		app.MapHub<EventHub>("/hub");
		app.MapGet("/messages", (ConcurrentBag<string> msgs) => msgs.ToArray());

		app.Run();

		public class EventHub : Hub
		{
		    private readonly ConcurrentBag<string> _messages;
		    public void SendEvent(string data) => _messages.Add(data);
		    public EventHub(ConcurrentBag<string> messages) => _messages = messages;
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

	private boolean tlsMode;
	protected SignalRDestination destination;
	protected SignalRDestinationConfig config;
	protected GenericContainer<?> serverContainer;

	@BeforeEach
	void setUp() {
		destination = new SignalRDestination();
		config = new SignalRDestinationConfig();
	}

	@AfterEach
	void tearDown() {
		if (destination != null) {
			try { destination.close(); } catch (Exception e) { /* ignore */ }
		}
		if (serverContainer != null) {
			try { serverContainer.stop(); } catch (Exception e) { /* ignore */ }
			serverContainer = null;
		}
	}

	@SuppressWarnings("resource")
	protected void startServer() {
		tlsMode = false;
		serverContainer = new GenericContainer<>(DockerImageName.parse(DOTNET_IMAGE))
			.withExposedPorts(SERVER_PORT)
			.withEnv("ASPNETCORE_URLS", "http://0.0.0.0:" + SERVER_PORT)
			.withEnv("DOTNET_NOLOGO", "true")
			.withCopyToContainer(Transferable.of(PROGRAM_CS, 0777), "/app/Program.cs")
			.withCopyToContainer(Transferable.of(CSPROJ, 0777), "/app/SignalRServer.csproj")
			.withCommand("sh", "-c", "cd /app && dotnet run");
		serverContainer.start();
		waitForServerReady();
	}

	@SuppressWarnings("resource")
	protected void startServerWithTls(TlsMaterial tls, boolean requireClientAuth) {
		
		tlsMode = true;

		var container = new GenericContainer<>(DockerImageName.parse(DOTNET_IMAGE))
			.withExposedPorts(SERVER_PORT, EVENTS_PORT)
			.withEnv("DOTNET_NOLOGO", "true")
			.withCopyToContainer(Transferable.of(PROGRAM_TLS_CS, 0777), "/app/Program.cs")
			.withCopyToContainer(Transferable.of(CSPROJ, 0777), "/app/SignalRServer.csproj")
			.withCopyToContainer(Transferable.of(tls.getServerCertificatePemBytes(), 0777), "/app/certs/server.crt")
			.withCopyToContainer(Transferable.of(tls.getServerPrivateKeyPemBytes(), 0777), "/app/certs/server.key");

		if (requireClientAuth) {
			container.withCopyToContainer(Transferable.of(tls.getCaCertificatePemBytes(), 0777), "/app/certs/ca.crt");
		}

		serverContainer = container.withCommand("sh", "-c", "cd /app && dotnet run");
		serverContainer.start();

		waitForEventsEndpointReady();
	}

	protected String getServerBaseUrl() {
		return "http://127.0.0.1:" + serverContainer.getMappedPort(SERVER_PORT);
	}

	protected String getServerTlsBaseUrl() {
		return "https://127.0.0.1:" + serverContainer.getMappedPort(SERVER_PORT);
	}

	protected String getServerHubUrl() {
		return getServerBaseUrl() + "/hub";
	}

	protected String getServerTlsHubUrl() {
		return getServerTlsBaseUrl() + "/hub";
	}

	private String getEventsBaseUrl() {
		
		if (tlsMode) {
			return "http://127.0.0.1:" + serverContainer.getMappedPort(EVENTS_PORT);
		}

		return getServerBaseUrl();
	}

	protected void configureDestination() {
		configureDestination(Map.of());
	}

	protected void configureDestination(Map<String, Object> extras) {
		var map = new HashMap<String, Object>();
		map.put(Constants.KIND, "signalr");
		map.put("url", getServerHubUrl());
		map.putAll(extras);
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithTls(TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put(Constants.KIND, "signalr");
		map.put("url", getServerTlsHubUrl());
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithMtls(TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put(Constants.KIND, "signalr");
		map.put("url", getServerTlsHubUrl());
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		map.put("tls.key-store.loader.kind", "jks-file-path");
		map.put("tls.key-store.loader.path", tls.getKeyStoreFilePath());
		map.put("tls.key-store.password", tls.getKeyStorePassword());
		map.put("tls.key-store.key-password", tls.getKeyPassword());
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected List<String> getReceivedMessages() throws Exception {
		var client = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofSeconds(5))
			.build();
		var request = HttpRequest.newBuilder()
			.uri(URI.create(getEventsBaseUrl() + "/messages"))
			.timeout(Duration.ofSeconds(5))
			.GET()
			.build();
		var response = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			return List.of();
		}
		var root = MAPPER.readTree(response.body());
		var messages = new ArrayList<String>();
		for (var node : root) {
			messages.add(node.asText());
		}
		return messages;
	}

	protected String getContainerLogs() {
		return serverContainer != null ? serverContainer.getLogs() : "(no container)";
	}

	protected EventMessage createMessage(String eventId, String eventType, String contentType, byte[] eventBody) {
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "EVENT", "", "");
	}

	private void waitForServerReady() {
		var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {
				var request = HttpRequest.newBuilder()
					.uri(URI.create(getServerBaseUrl() + "/messages"))
					.timeout(Duration.ofSeconds(3))
					.GET()
					.build();
				return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
			} catch (Exception e) {
				return false;
			}
		});
	}

	private void waitForEventsEndpointReady() {
		var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
		var url = "http://127.0.0.1:" + serverContainer.getMappedPort(EVENTS_PORT) + "/messages";
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {
				var request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.timeout(Duration.ofSeconds(3))
					.GET()
					.build();
				return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
			} catch (Exception e) {
				return false;
			}
		});
	}
}
