package io.github.fortunen.kete.integrationtests.websocketdestination;

import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.SSLContext;

import org.apache.commons.configuration2.MapConfiguration;
import org.java_websocket.SSLSocketChannel2;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.WebSocketServerFactory;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.websocket.WebSocketDestination;
import io.github.fortunen.kete.destinations.websocket.WebSocketDestinationConfig;

public class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];

	protected WebSocketDestination destination;
	protected WebSocketDestinationConfig config;
	protected TestWebSocketServer wsServer;
	protected LinkedBlockingQueue<String> receivedMessages;

	@BeforeEach
	void setUp() {
		destination = new WebSocketDestination();
		config = new WebSocketDestinationConfig();
		receivedMessages = new LinkedBlockingQueue<>();
	}

	@AfterEach
	void tearDown() throws Exception {
		if (wsServer != null) {
			try {
				wsServer.stop(1000);
			} catch (Exception e) {
				// ignore
			}
			wsServer = null;
		}
		if (destination != null) {
			try {
				destination.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	protected void configureDestination(MapConfiguration mapConfig) {
		mapConfig.setProperty(Constants.KIND, "websocket");
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected void startWebSocketServer() throws Exception {
		wsServer = new TestWebSocketServer(0, receivedMessages);
		wsServer.start();

		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> wsServer.getPort() > 0);
	}

	protected void startWithServerOnlyTLS(TlsMaterial tls) throws Exception {
		startWebSocketWithTls(tls, false);
	}

	protected void startWithClientAndServerTLS(TlsMaterial tls) throws Exception {
		startWebSocketWithTls(tls, true);
	}

	private void startWebSocketWithTls(TlsMaterial tls, boolean requireClientAuth) throws Exception {
		
		var sslContext = createServerSSLContext(tls, requireClientAuth);
		
		wsServer = new TestWebSocketServer(0, receivedMessages);
		
		if (requireClientAuth) {
			wsServer.setWebSocketFactory(new MtlsSSLWebSocketServerFactory(sslContext));
		} else {
			wsServer.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
		}

		wsServer.start();

		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> wsServer.getPort() > 0);

		waitForWebSocketTlsReady(tls);
	}

	private SSLContext createServerSSLContext(TlsMaterial tls, boolean requireClientAuth) throws Exception {
		
		if (requireClientAuth) {
			return tls.getServerKeyStoreSSLContext();
		}

		var serverKeyManagerFactory = javax.net.ssl.KeyManagerFactory.getInstance(javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm());
		
		serverKeyManagerFactory.init(tls.getServerKeyStore(), tls.getKeyPassword() != null ? tls.getKeyPassword().toCharArray() : null);

		var sslContext = SSLContext.getInstance("TLS");
		
		sslContext.init(serverKeyManagerFactory.getKeyManagers(), null, null);
		
		return sslContext;
	}

	protected int getWebSocketPort() {
		return wsServer.getPort();
	}

	protected int getWebSocketTlsPort() {
		return wsServer.getPort();
	}

	protected String getWebSocketTlsHost() {
		return "localhost";
	}

	private void waitForWebSocketTlsReady(TlsMaterial tls) {
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {
				
				var sslContext = tls.getKeyStoreAndTrustStoreSSLContext();
				var factory = sslContext.getSocketFactory();
				
				try (var socket = factory.createSocket(getWebSocketTlsHost(), getWebSocketTlsPort())) {
					socket.setSoTimeout(2000);
					return socket.isConnected();
				}

			} catch (Exception e) {
				return false;
			}
		});
	}

	protected EventMessage createMessage(String eventId, String eventType, String contentType, byte[] eventBody) {
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "false", "", "");
	}

	protected static class TestWebSocketServer extends WebSocketServer {

		private final LinkedBlockingQueue<String> messages;

		public TestWebSocketServer(int port, LinkedBlockingQueue<String> messages) {
			super(new InetSocketAddress(port));
			this.messages = messages;
			setReuseAddr(true);
		}

		@Override
		public void onOpen(WebSocket conn, ClientHandshake handshake) {
			// Connection opened
		}

		@Override
		public void onClose(WebSocket conn, int code, String reason, boolean remote) {
			// Connection closed
		}

		@Override
		public void onMessage(WebSocket conn, String message) {
			messages.offer(message);
		}

		@Override
		public void onError(WebSocket conn, Exception ex) {
			// Handle error
		}

		@Override
		public void onStart() {
			// Server started
		}
	}

	protected static class MtlsSSLWebSocketServerFactory implements WebSocketServerFactory {

		private final SSLContext sslContext;
		private final ExecutorService exec;

		public MtlsSSLWebSocketServerFactory(SSLContext sslContext) {
			this.sslContext = sslContext;
			this.exec = Executors.newSingleThreadScheduledExecutor();
		}

		@Override
		public ByteChannel wrapChannel(SocketChannel channel, SelectionKey key) throws IOException {
			
			var engine = sslContext.createSSLEngine();
			
			engine.setUseClientMode(false);
			engine.setNeedClientAuth(true);
			
			return new SSLSocketChannel2(channel, engine, exec, key);
		}

		@Override
		public WebSocketImpl createWebSocket(WebSocketAdapter a, Draft d) {
			return new WebSocketImpl(a, d);
		}

		@Override
		public WebSocketImpl createWebSocket(WebSocketAdapter a, java.util.List<Draft> d) {
			return new WebSocketImpl(a, d);
		}

		@Override
		public void close() {
			exec.shutdown();
		}
	}
}
