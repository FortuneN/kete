package io.github.fortunen.kete;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.lang3.StringUtils;

import io.github.fortunen.kete.utils.Base64Utils;
import io.github.fortunen.kete.utils.CertificateUtils;
import io.github.fortunen.kete.utils.ConfigurationUtils;
import io.github.fortunen.kete.utils.FileUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.tls.HeldCertificate;

@Data
@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@ToString(exclude = {"keyPassword", "keyStorePassword", "trustStorePassword", "keyStoreBytes", "keyStoreBase64", "clientPrivateKeyPemBytes", "clientPrivateKeyPemBase64", "clientPrivateKeyPkcs1PemBytes", "clientPrivateKeyPkcs1PemBase64", "serverKeyStoreBytes", "serverKeyStoreBase64", "serverPrivateKeyPemBytes", "serverPrivateKeyPemBase64", "serverPrivateKeyPkcs1PemBytes", "serverPrivateKeyPkcs1PemBase64"})
public class TlsMaterial {

	public static final String TYPE = "type";
	public static final String LOADER = "loader";
	public static final String ENABLED = "enabled";
	public static final String PASSWORD = "password";
	public static final String TLS_VERSION = "version";
	public static final String DEFAULT_TLS_VERSION = "TLS";
	public static final String KEY_PASSWORD = "key-password";

	public static final String KEY_STORE = "key-store";
	public static final String KEY_MANAGER_ALGORITHM = "key-manager-algorithm";
	public static final String KEY_STORE_DEFAULT_TYPE = KeyStore.getDefaultType();
	public static final String KEY_MANAGER_DEFAULT_ALGORITHM = KeyManagerFactory.getDefaultAlgorithm();

	public static final String TRUST_STORE = "trust-store";
	public static final String TRUST_MANAGER_ALGORITHM = "trust-manager-algorithm";
	public static final String TRUST_STORE_DEFAULT_TYPE = KeyStore.getDefaultType();
	public static final String TRUST_MANAGER_DEFAULT_ALGORITHM = TrustManagerFactory.getDefaultAlgorithm();

	public static final X509Certificate[] SYSTEM_TRUSTED_CERTIFICATES;

	static {

		var systemTrustedCertificates = (X509Certificate[]) null;

		try {

			var trustManagerFactory = TrustManagerFactory.getInstance(TRUST_MANAGER_DEFAULT_ALGORITHM);

			trustManagerFactory.init((KeyStore) null);

			for (var trustManager : trustManagerFactory.getTrustManagers()) {
				if (trustManager instanceof X509TrustManager x509) {
					systemTrustedCertificates = x509.getAcceptedIssuers();
					break;
				}
			}

		} catch (Exception exception) {

			log.warn("Failed to load system trusted certificates", exception);

		}

		if (ValidationUtils.isNull(systemTrustedCertificates)) {
			systemTrustedCertificates = new X509Certificate[0];
		}

		SYSTEM_TRUSTED_CERTIFICATES = systemTrustedCertificates;
	}

	private String version;
	private boolean enabled;
	private boolean verifyHostname;
	private byte[] caCertificatePemBytes;
	private String caCertificatePemBase64;
	private SSLContext keyStoreAndTrustStoreSSLContext;

	// trust stuff

	private KeyStore trustStore;
	private String trustStoreType;
	private byte[] trustStoreBytes;
	private String trustStoreBase64;
	private String trustStorePassword;
	private String trustManagerAlgorithm;
	private SSLContext trustStoreSSLContext;
	private TrustManagerFactory trustManagerFactory;

	// keystore stuff

	private KeyStore keyStore;
	private String keyPassword;
	private String keyStoreType;
	private byte[] keyStoreBytes;
	private String keyStoreBase64;
	private String keyStorePassword;
	private String keyManagerAlgorithm;
	private boolean keyStoreConfigured;
	private SSLContext keyStoreSSLContext;
	private KeyStore keyStoreAndTrustStore;
	private KeyManagerFactory keyManagerFactory;

	// client stuff

	private byte[] clientCertificatePemBytes;
	private String clientCertificatePemBase64;
	private byte[] clientPrivateKeyPemBytes;
	private String clientPrivateKeyPemBase64;
	private byte[] clientPrivateKeyPkcs1PemBytes;
	private String clientPrivateKeyPkcs1PemBase64;

	// server stuff

	private KeyStore serverKeyStore;
	private byte[] serverKeyStoreBytes;
	private String serverKeyStoreBase64;
	private byte[] serverPrivateKeyPemBytes;
	private String serverPrivateKeyPemBase64;
	private byte[] serverCertificatePemBytes;
	private String serverCertificatePemBase64;
	private SSLContext serverKeyStoreSSLContext;
	private byte[] serverPrivateKeyPkcs1PemBytes;
	private String serverPrivateKeyPkcs1PemBase64;

	// lazy file paths (written on first access from bytes)

	private String keyStoreFilePath;
	private String trustStoreFilePath;
	private String serverKeyStoreFilePath;
	private String caCertificatePemFilePath;
	private String serverPrivateKeyPemFilePath;
	private String clientPrivateKeyPemFilePath;
	private String serverCertificatePemFilePath;
	private String clientCertificatePemFilePath;
	private String serverPrivateKeyPkcs1PemFilePath;
	private String clientPrivateKeyPkcs1PemFilePath;

	// =========================================================================
	// Lazy Getters - computed on first access, cached for subsequent calls
	// =========================================================================

	private String lazyBase64(String cached, byte[] source) {
		return cached != null ? cached : source != null ? Base64Utils.encode(source) : null;
	}

	@SneakyThrows
	private String lazyFilePath(String cached, byte[] source, String extension) {
		if (cached != null || source == null) return cached;
		var file = FileUtils.createTempFile(extension);
		try (var stream = new FileOutputStream(file)) { stream.write(source); }
		return file.getAbsolutePath();
	}

	public String getKeyStoreBase64() { return keyStoreBase64 = lazyBase64(keyStoreBase64, keyStoreBytes); }
	public String getTrustStoreBase64() { return trustStoreBase64 = lazyBase64(trustStoreBase64, trustStoreBytes); }
	public String getServerKeyStoreBase64() { return serverKeyStoreBase64 = lazyBase64(serverKeyStoreBase64, serverKeyStoreBytes); }
	public String getCaCertificatePemBase64() { return caCertificatePemBase64 = lazyBase64(caCertificatePemBase64, caCertificatePemBytes); }
	public String getServerPrivateKeyPemBase64() { return serverPrivateKeyPemBase64 = lazyBase64(serverPrivateKeyPemBase64, serverPrivateKeyPemBytes); }
	public String getClientPrivateKeyPemBase64() { return clientPrivateKeyPemBase64 = lazyBase64(clientPrivateKeyPemBase64, clientPrivateKeyPemBytes); }
	public String getServerCertificatePemBase64() { return serverCertificatePemBase64 = lazyBase64(serverCertificatePemBase64, serverCertificatePemBytes); }
	public String getClientCertificatePemBase64() { return clientCertificatePemBase64 = lazyBase64(clientCertificatePemBase64, clientCertificatePemBytes); }
	public String getServerPrivateKeyPkcs1PemBase64() { return serverPrivateKeyPkcs1PemBase64 = lazyBase64(serverPrivateKeyPkcs1PemBase64, serverPrivateKeyPkcs1PemBytes); }
	public String getClientPrivateKeyPkcs1PemBase64() { return clientPrivateKeyPkcs1PemBase64 = lazyBase64(clientPrivateKeyPkcs1PemBase64, clientPrivateKeyPkcs1PemBytes); }

	public String getKeyStoreFilePath() { return keyStoreFilePath = lazyFilePath(keyStoreFilePath, keyStoreBytes, ".jks"); }
	public String getTrustStoreFilePath() { return trustStoreFilePath = lazyFilePath(trustStoreFilePath, trustStoreBytes, ".jks"); }
	public String getServerKeyStoreFilePath() { return serverKeyStoreFilePath = lazyFilePath(serverKeyStoreFilePath, serverKeyStoreBytes, ".jks"); }
	public String getCaCertificatePemFilePath() { return caCertificatePemFilePath = lazyFilePath(caCertificatePemFilePath, caCertificatePemBytes, ".pem"); }
	public String getServerPrivateKeyPemFilePath() { return serverPrivateKeyPemFilePath = lazyFilePath(serverPrivateKeyPemFilePath, serverPrivateKeyPemBytes, ".pem"); }
	public String getClientPrivateKeyPemFilePath() { return clientPrivateKeyPemFilePath = lazyFilePath(clientPrivateKeyPemFilePath, clientPrivateKeyPemBytes, ".pem"); }
	public String getServerCertificatePemFilePath() { return serverCertificatePemFilePath = lazyFilePath(serverCertificatePemFilePath, serverCertificatePemBytes, ".pem"); }
	public String getClientCertificatePemFilePath() { return clientCertificatePemFilePath = lazyFilePath(clientCertificatePemFilePath, clientCertificatePemBytes, ".pem"); }
	public String getServerPrivateKeyPkcs1PemFilePath() { return serverPrivateKeyPkcs1PemFilePath = lazyFilePath(serverPrivateKeyPkcs1PemFilePath, serverPrivateKeyPkcs1PemBytes, ".pem"); }
	public String getClientPrivateKeyPkcs1PemFilePath() { return clientPrivateKeyPkcs1PemFilePath = lazyFilePath(clientPrivateKeyPkcs1PemFilePath, clientPrivateKeyPkcs1PemBytes, ".pem"); }

	public SSLSocketFactory getHostnameVerifyingSocketFactory() {
		var delegate = keyStoreAndTrustStoreSSLContext.getSocketFactory();
		return new SSLSocketFactory() {
			@Override public String[] getDefaultCipherSuites() { return delegate.getDefaultCipherSuites(); }
			@Override public String[] getSupportedCipherSuites() { return delegate.getSupportedCipherSuites(); }
			@Override public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException { return withVerification(delegate.createSocket(s, host, port, autoClose)); }
			@Override public Socket createSocket(String host, int port) throws IOException { return withVerification(delegate.createSocket(host, port)); }
			@Override public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException { return withVerification(delegate.createSocket(host, port, localHost, localPort)); }
			@Override public Socket createSocket(InetAddress host, int port) throws IOException { return withVerification(delegate.createSocket(host, port)); }
			@Override public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException { return withVerification(delegate.createSocket(address, port, localAddress, localPort)); }
			private Socket withVerification(Socket socket) {
				if (socket instanceof SSLSocket ssl) { var params = ssl.getSSLParameters(); params.setEndpointIdentificationAlgorithm("HTTPS"); ssl.setSSLParameters(params); }
				return socket;
			}
		};
	}

	public SSLContext getHostnameVerifyingSSLContext() {
		var original = keyStoreAndTrustStoreSSLContext;
		var wrappedFactory = getHostnameVerifyingSocketFactory();
		return new SSLContext(new SSLContextSpi() {
			@Override protected void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom sr) {}
			@Override protected SSLSocketFactory engineGetSocketFactory() { return wrappedFactory; }
			@Override protected SSLServerSocketFactory engineGetServerSocketFactory() { return original.getServerSocketFactory(); }
			@Override protected SSLEngine engineCreateSSLEngine() { return original.createSSLEngine(); }
			@Override protected SSLEngine engineCreateSSLEngine(String host, int port) { return original.createSSLEngine(host, port); }
			@Override protected SSLSessionContext engineGetClientSessionContext() { return original.getClientSessionContext(); }
			@Override protected SSLSessionContext engineGetServerSessionContext() { return original.getServerSessionContext(); }
		}, original.getProvider(), original.getProtocol()) {};
	}

	// builder

	public static TlsMaterialBuilder builder() {
		return new TlsMaterialBuilder();
	}

	public static class TlsMaterialBuilder {

		private TlsMaterial material = new TlsMaterial();
		private Set<String> serverHostNames = new HashSet<>();

		public TlsMaterialBuilder withEnabled(boolean enabled) {
			this.material.enabled = enabled;
			return this;
		}

		public TlsMaterialBuilder withVersion(String version) {
			this.material.version = version;
			return this;
		}

		public TlsMaterialBuilder withTrustStoreType(String trustStoreType) {
			this.material.trustStoreType = trustStoreType;
			return this;
		}

		public TlsMaterialBuilder withTrustStorePassword(String trustStorePassword) {
			this.material.trustStorePassword = trustStorePassword;
			return this;
		}

		public TlsMaterialBuilder withTrustManagerAlgorithm(String trustManagerAlgorithm) {
			this.material.trustManagerAlgorithm = trustManagerAlgorithm;
			return this;
		}

		public TlsMaterialBuilder withKeyStoreType(String keyStoreType) {
			this.material.keyStoreType = keyStoreType;
			return this;
		}

		public TlsMaterialBuilder withKeyStorePassword(String keyStorePassword) {
			this.material.keyStorePassword = keyStorePassword;
			return this;
		}

		public TlsMaterialBuilder withKeyPassword(String keyPassword) {
			this.material.keyPassword = keyPassword;
			return this;
		}

		public TlsMaterialBuilder withKeyManagerAlgorithm(String keyManagerAlgorithm) {
			this.material.keyManagerAlgorithm = keyManagerAlgorithm;
			return this;
		}

		public TlsMaterialBuilder withServerHostNames(String[] serverHostNames) {
			this.serverHostNames = ValidationUtils.isNotNull(serverHostNames) ? Arrays.stream(serverHostNames).filter(ValidationUtils::isNotBlank).map(StringUtils::trim).collect(Collectors.toSet()) : new HashSet<>();
			return this;
		}

		@SneakyThrows
		public TlsMaterialBuilder withConfiguration(MapConfiguration configuration) {

			ValidationUtils.requireNonNull(configuration, "configuration is required");

			material.enabled = configuration.getBoolean(ENABLED, false);
			material.version = configuration.getString(TLS_VERSION, DEFAULT_TLS_VERSION).trim();
			material.verifyHostname = configuration.getBoolean("verify-hostname", false);

			var trustStoreConfiguration = ConfigurationUtils.getSubSet(configuration, TRUST_STORE);

			if (!trustStoreConfiguration.isEmpty()) {

				material.trustStorePassword = trustStoreConfiguration.getString(PASSWORD, "").trim();
				material.trustStoreType = trustStoreConfiguration.getString(TYPE, TRUST_STORE_DEFAULT_TYPE).trim();
				material.trustManagerAlgorithm = trustStoreConfiguration.getString(TRUST_MANAGER_ALGORITHM, TRUST_MANAGER_DEFAULT_ALGORITHM).trim();
				material.trustStore = KeyStore.getInstance(material.trustStoreType);

				var trustStoreLoaderConfiguration = ConfigurationUtils.getSubSet(trustStoreConfiguration, LOADER);

				if (!trustStoreLoaderConfiguration.isEmpty()) {

					var trustStoreLoadedSuccessfully = false;
					var trustStoreLoader = CertificateUtils.createCertificateLoader(trustStoreLoaderConfiguration);

					for (var password : new String[] { material.trustStorePassword, null, "", "changeit", "secret" }) {
						try {

							trustStoreLoader.loadKeyStore(material.trustStore, ValidationUtils.isNotNull(password) ? password.toCharArray() : null);
							material.trustStorePassword = password;
							trustStoreLoadedSuccessfully = true;
							break;

						} catch (Exception e) {
							// try next password
						}
					}

					if (!trustStoreLoadedSuccessfully) {
						trustStoreLoader.loadKeyStore(material.trustStore, ValidationUtils.isNotNull(material.trustStorePassword) ? material.trustStorePassword.toCharArray() : null);
					}

				} else {
					material.trustStore.load(null, null);
				}
			}

			var keyStoreConfiguration = ConfigurationUtils.getSubSet(configuration, KEY_STORE);

			if (!keyStoreConfiguration.isEmpty()) {

				material.keyStoreConfigured = true;
				material.keyStoreType = keyStoreConfiguration.getString(TYPE, KEY_STORE_DEFAULT_TYPE).trim();
				material.keyPassword = keyStoreConfiguration.getString(KEY_PASSWORD, "").trim();
				material.keyStorePassword = keyStoreConfiguration.getString(PASSWORD, "").trim();
				material.keyManagerAlgorithm = keyStoreConfiguration.getString(KEY_MANAGER_ALGORITHM, KEY_MANAGER_DEFAULT_ALGORITHM).trim();
				material.keyStore = KeyStore.getInstance(material.keyStoreType);

				var certificateLoaderConfiguration = ConfigurationUtils.getSubSet(keyStoreConfiguration, LOADER);

				if (!certificateLoaderConfiguration.isEmpty()) {

					var certificateLoader = CertificateUtils.createCertificateLoader(certificateLoaderConfiguration);

					var keyStoreLoadedSuccessfully = false;

					for (var password : new String[] { material.keyStorePassword, null, "", "changeit", "secret" }) {
						try {

							certificateLoader.loadKeyStore(material.keyStore, ValidationUtils.isNotNull(password) ? password.toCharArray() : null);
							material.keyStorePassword = password;
							keyStoreLoadedSuccessfully = true;
							break;

						} catch (Exception e) {
							// try next password
						}
					}

					if (!keyStoreLoadedSuccessfully) {
						certificateLoader.loadKeyStore(material.keyStore, ValidationUtils.isNotNull(material.keyStorePassword) ? material.keyStorePassword.toCharArray() : null);
					}

				} else {
					material.keyStore.load(null, null);
				}
			}

			return this;
		}

		@SneakyThrows
		public TlsMaterial build() {

			if (!material.enabled) {
				return material;
			}

			// init

			material.version = ValidationUtils.requireNonBlankElse(material.version, DEFAULT_TLS_VERSION).trim();

			if (ValidationUtils.isNull(material.keyStore)) {
				material.keyStoreType = ValidationUtils.requireNonBlankElse(material.keyStoreType, KEY_STORE_DEFAULT_TYPE).trim();
				material.keyStore = KeyStore.getInstance(material.keyStoreType);
				material.keyStore.load(null, null);
			}

			if (ValidationUtils.isNull(material.trustStore)) {
				material.trustStoreType = ValidationUtils.requireNonBlankElse(material.trustStoreType, TRUST_STORE_DEFAULT_TYPE).trim();
				material.trustStore = KeyStore.getInstance(material.trustStoreType);
				material.trustStore.load(null, null);
			}

			for (var i = 0; i < SYSTEM_TRUSTED_CERTIFICATES.length; i++) {
				material.trustStore.setCertificateEntry("system-trusted-certificate-" + i, SYSTEM_TRUSTED_CERTIFICATES[i]);
			}

			// certificates

			if (ValidationUtils.isNotNull(serverHostNames) && !serverHostNames.isEmpty()) {

				var selfSignedCaCertificate = new HeldCertificate.Builder().rsa2048().commonName("Test CA").certificateAuthority(0).build();
				var caSignedServerCertificateBuilder = new HeldCertificate.Builder().rsa2048().commonName("Test Server").signedBy(selfSignedCaCertificate);

				for (var serverHostName : serverHostNames) {
					caSignedServerCertificateBuilder.addSubjectAlternativeName(serverHostName);
				}

				var caSignedServerCertificate = caSignedServerCertificateBuilder.build();
				var caSignedClientCertificate = new HeldCertificate.Builder().rsa2048().commonName("Test Client").signedBy(selfSignedCaCertificate).build();

				material.trustStore.setCertificateEntry("ca-certificate", selfSignedCaCertificate.certificate());
				material.trustStore.setCertificateEntry("server-certificate", caSignedServerCertificate.certificate());
				material.trustStore.setCertificateEntry("client-certificate", caSignedClientCertificate.certificate());

				material.serverKeyStore = KeyStore.getInstance(material.keyStoreType);
				material.serverKeyStore.load(null, null);
				material.serverKeyStore.setKeyEntry("server-key", caSignedServerCertificate.keyPair().getPrivate(), ValidationUtils.isNotNull(material.keyPassword) ? material.keyPassword.toCharArray() : null, new Certificate[] { caSignedServerCertificate.certificate(), selfSignedCaCertificate.certificate() });

				material.keyStore.setKeyEntry("client-key", caSignedClientCertificate.keyPair().getPrivate(), ValidationUtils.isNotNull(material.keyPassword) ? material.keyPassword.toCharArray() : null, new Certificate[] { caSignedClientCertificate.certificate(), selfSignedCaCertificate.certificate() });

				material.caCertificatePemBytes = selfSignedCaCertificate.certificatePem().getBytes(StandardCharsets.UTF_8);
				material.serverCertificatePemBytes = caSignedServerCertificate.certificatePem().getBytes(StandardCharsets.UTF_8);
				material.serverPrivateKeyPemBytes = caSignedServerCertificate.privateKeyPkcs8Pem().getBytes(StandardCharsets.UTF_8);
				material.serverPrivateKeyPkcs1PemBytes = CertificateUtils.convertPkcs8ToPkcs1Pem(caSignedServerCertificate.keyPair().getPrivate()).getBytes(StandardCharsets.UTF_8);

				material.clientCertificatePemBytes = caSignedClientCertificate.certificatePem().getBytes(StandardCharsets.UTF_8);
				material.clientPrivateKeyPemBytes = caSignedClientCertificate.privateKeyPkcs8Pem().getBytes(StandardCharsets.UTF_8);
				material.clientPrivateKeyPkcs1PemBytes = CertificateUtils.convertPkcs8ToPkcs1Pem(caSignedClientCertificate.keyPair().getPrivate()).getBytes(StandardCharsets.UTF_8);
			}

			// trust stuff

			try (var stream = new ByteArrayOutputStream()) {
				material.trustStore.store(stream, ValidationUtils.isNotNull(material.trustStorePassword) ? material.trustStorePassword.toCharArray() : null);
				material.trustStoreBytes = stream.toByteArray();
			}

			material.trustManagerAlgorithm = ValidationUtils.requireNonBlankElse(material.trustManagerAlgorithm, TRUST_MANAGER_DEFAULT_ALGORITHM).trim();
			material.trustManagerFactory = TrustManagerFactory.getInstance(material.trustManagerAlgorithm);
			material.trustManagerFactory.init(material.trustStore);

			// keystore stuff

			try (var keyStoreStream = new ByteArrayOutputStream()) {
				material.keyStore.store(keyStoreStream, ValidationUtils.isNotNull(material.keyStorePassword) ? material.keyStorePassword.toCharArray() : null);
				material.keyStoreBytes = keyStoreStream.toByteArray();
			}

			if (ValidationUtils.isNotNull(material.serverKeyStore) && material.serverKeyStore.size() > 0) {
				try (var serverKeyStoreStream = new ByteArrayOutputStream()) {
					material.serverKeyStore.store(serverKeyStoreStream, ValidationUtils.isNotNull(material.keyStorePassword) ? material.keyStorePassword.toCharArray() : null);
					material.serverKeyStoreBytes = serverKeyStoreStream.toByteArray();
				}
			}

			material.keyManagerAlgorithm = ValidationUtils.requireNonBlankElse(material.keyManagerAlgorithm, KEY_MANAGER_DEFAULT_ALGORITHM).trim();

			var keyManagerFactoryInitializedSuccessfully = false;
			material.keyManagerFactory = KeyManagerFactory.getInstance(material.keyManagerAlgorithm);

			for (var password : new String[] { material.keyPassword, material.keyStorePassword, null, "", "changeit", "secret" }) {
				try {

					material.keyManagerFactory.init(material.keyStore, ValidationUtils.isNotNull(password) ? password.toCharArray() : null);
					material.keyPassword = password;
					keyManagerFactoryInitializedSuccessfully = true;

					break;

				} catch (Exception e) {
					// try next password
				}
			}

			if (!keyManagerFactoryInitializedSuccessfully) {
				material.keyManagerFactory.init(material.keyStore, ValidationUtils.isNotBlank(material.keyPassword) ? material.keyPassword.toCharArray() : null);
			}

			// ssl contexts

			material.trustStoreSSLContext = SSLContext.getInstance(material.version);
			material.trustStoreSSLContext.init(null, material.trustManagerFactory.getTrustManagers(), null);

			material.keyStoreSSLContext = SSLContext.getInstance(material.version);
			material.keyStoreSSLContext.init(material.keyManagerFactory.getKeyManagers(), null, null);

			material.keyStoreAndTrustStoreSSLContext = SSLContext.getInstance(material.version);
			material.keyStoreAndTrustStoreSSLContext.init(material.keyManagerFactory.getKeyManagers(), material.trustManagerFactory.getTrustManagers(), null);

			// server stuff

			if (ValidationUtils.isNotNull(material.serverKeyStore) && material.serverKeyStore.size() > 0) {

				var serverKeyManagerFactoryInitializedSuccessfully = false;
				var serverKeyManagerFactory = KeyManagerFactory.getInstance(material.keyManagerAlgorithm);

				for (var password : new String[] { material.keyPassword, material.keyStorePassword, null, "", "changeit", "secret" }) {
					try {

						serverKeyManagerFactory.init(material.serverKeyStore, ValidationUtils.isNotNull(password) ? password.toCharArray() : null);
						serverKeyManagerFactoryInitializedSuccessfully = true;
						break;

					} catch (Exception e) {
						// try next password
					}
				}

				if (!serverKeyManagerFactoryInitializedSuccessfully) {
					serverKeyManagerFactory.init(material.serverKeyStore, ValidationUtils.isNotBlank(material.keyPassword) ? material.keyPassword.toCharArray() : null);
				}

				material.serverKeyStoreSSLContext = SSLContext.getInstance(material.version);
				material.serverKeyStoreSSLContext.init(serverKeyManagerFactory.getKeyManagers(), material.trustManagerFactory.getTrustManagers(), null);
			}

			material.keyStoreAndTrustStore = KeyStore.getInstance(material.keyStoreType);
			material.keyStoreAndTrustStore.load(null, null);

			for (var alias : Collections.list(material.trustStore.aliases())) {
				if (material.trustStore.isCertificateEntry(alias)) {
					material.keyStoreAndTrustStore.setCertificateEntry(alias, material.trustStore.getCertificate(alias));
				}
			}

			for (var alias : Collections.list(material.keyStore.aliases())) {
				if (material.keyStore.isKeyEntry(alias)) {

					var key = material.keyStore.getKey(alias, ValidationUtils.isNotBlank(material.keyPassword) ? material.keyPassword.toCharArray() : null);
					var chain = material.keyStore.getCertificateChain(alias);

					material.keyStoreAndTrustStore.setKeyEntry(alias, key, ValidationUtils.isNotBlank(material.keyPassword) ? material.keyPassword.toCharArray() : null, chain);

				} else if (material.keyStore.isCertificateEntry(alias)) {

					material.keyStoreAndTrustStore.setCertificateEntry(alias, material.keyStore.getCertificate(alias));

				}
			}

			// return

			return material;
		}
	}
}
