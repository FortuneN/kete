package io.github.fortunen.kete;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
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
import lombok.extern.slf4j.Slf4j;
import okhttp3.tls.HeldCertificate;

@Data
@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
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
	private String caCertificatePemFilePath;
	private SSLContext keyStoreAndTrustStoreSSLContext;

	// trust stuff

	private KeyStore trustStore;
	private String trustStoreType;
	private String trustStoreBase64;
	private String trustStorePassword;
	private String trustStoreFilePath;
	private String trustManagerAlgorithm;
	private SSLContext trustStoreSSLContext;

	// keystore stuff

	private KeyStore keyStore;
	private String keyPassword;
	private String keyStoreType;
	private String keyStoreBase64;
	private String keyStorePassword;
	private String keyStoreFilePath;
	private String keyManagerAlgorithm;
	private SSLContext keyStoreSSLContext;
	private KeyStore keyStoreAndTrustStore;

	// client stuff

	private String clientPrivateKeyPemFilePath;
	private String clientCertificatePemFilePath;
	private String clientPrivateKeyPkcs1PemFilePath;

	// server stuff

	private KeyStore serverKeyStore;
	private String serverKeyStoreFilePath;
	private String serverPrivateKeyPemFilePath;
	private SSLContext serverKeyStoreSSLContext;
	private String serverCertificatePemFilePath;
	private String serverPrivateKeyPkcs1PemFilePath;

	// builder

	public static TlsMaterialBuilder builder() {
		return new TlsMaterialBuilder();
	}

	public static class TlsMaterialBuilder {

		private boolean writeFiles;
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

		public TlsMaterialBuilder withWriteFiles(boolean writeFiles) {
			this.writeFiles = writeFiles;
			return this;
		}

		@SneakyThrows
		public TlsMaterialBuilder withConfiguration(MapConfiguration configuration) {

			ValidationUtils.requireNonNull(configuration, "configuration is required");

			material.enabled = configuration.getBoolean(ENABLED, false);
			material.version = configuration.getString(TLS_VERSION, DEFAULT_TLS_VERSION).trim();

			var trustStoreConfiguration = ConfigurationUtils.getSubSet(configuration, TRUST_STORE);

			if (!trustStoreConfiguration.isEmpty()) {

				material.trustStorePassword = trustStoreConfiguration.getString(PASSWORD, "").trim();
				material.trustStoreType = trustStoreConfiguration.getString(TYPE, TRUST_STORE_DEFAULT_TYPE).trim();
				material.trustManagerAlgorithm = trustStoreConfiguration.getString(TRUST_MANAGER_ALGORITHM, KEY_MANAGER_DEFAULT_ALGORITHM).trim();

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
				material.serverKeyStore.setKeyEntry("server-key", caSignedServerCertificate.keyPair().getPrivate(), material.keyPassword != null ? material.keyPassword.toCharArray() : null, new Certificate[] { caSignedServerCertificate.certificate(), selfSignedCaCertificate.certificate() });

				material.keyStore.setKeyEntry("client-key", caSignedClientCertificate.keyPair().getPrivate(), material.keyPassword != null ? material.keyPassword.toCharArray() : null, new Certificate[] { caSignedClientCertificate.certificate(), selfSignedCaCertificate.certificate() });

				if (writeFiles) {
					material.caCertificatePemFilePath = FileUtils.writeToTempFile(selfSignedCaCertificate.certificatePem(), "-ca.pem");
					material.serverCertificatePemFilePath = FileUtils.writeToTempFile(caSignedServerCertificate.certificatePem(), "-server.pem");
					material.clientCertificatePemFilePath = FileUtils.writeToTempFile(caSignedClientCertificate.certificatePem(), "-client.pem");
					material.serverPrivateKeyPemFilePath = FileUtils.writeToTempFile(caSignedServerCertificate.privateKeyPkcs8Pem(), "-server-key.pem");
					material.clientPrivateKeyPemFilePath = FileUtils.writeToTempFile(caSignedClientCertificate.privateKeyPkcs8Pem(), "-client-key.pem");
					material.serverPrivateKeyPkcs1PemFilePath = FileUtils.writeToTempFile(CertificateUtils.convertPkcs8ToPkcs1Pem(caSignedServerCertificate.keyPair().getPrivate()), "-server-key-pkcs1.pem");
					material.clientPrivateKeyPkcs1PemFilePath = FileUtils.writeToTempFile(CertificateUtils.convertPkcs8ToPkcs1Pem(caSignedClientCertificate.keyPair().getPrivate()), "-client-key-pkcs1.pem");
				}
			}

			// trust stuff

			try (var stream = new ByteArrayOutputStream()) {
				material.trustStore.store(stream, ValidationUtils.isNotNull(material.trustStorePassword) ? material.trustStorePassword.toCharArray() : null);
				material.trustStoreBase64 = Base64Utils.encode(stream.toByteArray());
			}

			if (writeFiles) {

				var trustStoreFile = FileUtils.createTempFile(null);

				try (var trustStoreStream = new FileOutputStream(trustStoreFile)) {
					material.trustStore.store(trustStoreStream, ValidationUtils.isNotNull(material.trustStorePassword) ? material.trustStorePassword.toCharArray() : null);
				}

				material.trustStoreFilePath = trustStoreFile.getAbsolutePath();
			}

			material.trustManagerAlgorithm = ValidationUtils.requireNonBlankElse(material.trustManagerAlgorithm, TRUST_MANAGER_DEFAULT_ALGORITHM).trim();

			var trustManagerFactory = TrustManagerFactory.getInstance(material.trustManagerAlgorithm);

			trustManagerFactory.init(material.trustStore);

			// keystore stuff

			try (var keyStoreStream = new ByteArrayOutputStream()) {
				material.keyStore.store(keyStoreStream, ValidationUtils.isNotNull(material.keyStorePassword) ? material.keyStorePassword.toCharArray() : null);
				material.keyStoreBase64 = Base64Utils.encode(keyStoreStream.toByteArray());
			}

			if (writeFiles && material.keyStore.size() > 0) {

				var keyStoreFile = FileUtils.createTempFile(null);

				try (var keyStoreStream = new FileOutputStream(keyStoreFile)) {
					material.keyStore.store(keyStoreStream, ValidationUtils.isNotNull(material.keyStorePassword) ? material.keyStorePassword.toCharArray() : null);
				}

				material.keyStoreFilePath = keyStoreFile.getAbsolutePath();
			}

			if (writeFiles && material.serverKeyStore != null && material.serverKeyStore.size() > 0) {

				var serverKeyStoreFile = FileUtils.createTempFile("-server.jks");

				try (var serverKeyStoreStream = new FileOutputStream(serverKeyStoreFile)) {
					material.serverKeyStore.store(serverKeyStoreStream, ValidationUtils.isNotNull(material.keyStorePassword) ? material.keyStorePassword.toCharArray() : null);
				}

				material.serverKeyStoreFilePath = serverKeyStoreFile.getAbsolutePath();
			}

			material.keyManagerAlgorithm = ValidationUtils.requireNonBlankElse(material.keyManagerAlgorithm, KEY_MANAGER_DEFAULT_ALGORITHM).trim();

			var keyManagerFactoryInitializedSuccessfully = false;
			var keyManagerFactory = KeyManagerFactory.getInstance(material.keyManagerAlgorithm);

			for (var password : new String[] { material.keyPassword, material.keyStorePassword, null, "", "changeit", "secret" }) {
				try {

					keyManagerFactory.init(material.keyStore, ValidationUtils.isNotNull(password) ? password.toCharArray() : null);
					material.keyPassword = password;
					keyManagerFactoryInitializedSuccessfully = true;

					break;

				} catch (Exception e) {
					// try next password
				}
			}

			if (!keyManagerFactoryInitializedSuccessfully) {

				keyManagerFactory.init(material.keyStore, ValidationUtils.isNotBlank(material.keyPassword) ? material.keyPassword.toCharArray() : null);
			}

			// ssl contexts

			material.trustStoreSSLContext = SSLContext.getInstance(material.version);
			material.trustStoreSSLContext.init(null, trustManagerFactory.getTrustManagers(), null);

			material.keyStoreSSLContext = SSLContext.getInstance(material.version);
			material.keyStoreSSLContext.init(keyManagerFactory.getKeyManagers(), null, null);

			material.keyStoreAndTrustStoreSSLContext = SSLContext.getInstance(material.version);
			material.keyStoreAndTrustStoreSSLContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

			// server stuff

			if (material.serverKeyStore != null && material.serverKeyStore.size() > 0) {

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
				material.serverKeyStoreSSLContext.init(serverKeyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
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

	// kafka

	@SneakyThrows
	public void updateKafkaConfiguration(Properties configuration) {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		if (!enabled) {
			return;
		}

		var securityProtocol = ValidationUtils.requireNonBlank(configuration.getProperty("security.protocol"), ".destination.security.protocol is required").trim();
		ValidationUtils.requireTrue(securityProtocol.equals("SSL") || securityProtocol.equals("SASL_SSL"), ".destination.security.protocol must be 'SSL' or 'SASL_SSL' when TLS is enabled");

		if (ValidationUtils.isNotBlank(version)) {
			configuration.put("ssl.protocol", version);
		}

		if (ValidationUtils.isNotBlank(trustStoreType)) {
			configuration.put("ssl.truststore.type", trustStoreType);
		}

		if (ValidationUtils.isNotBlank(trustStoreFilePath)) {
			configuration.put("ssl.truststore.location", trustStoreFilePath);
		}

		if (ValidationUtils.isNotBlank(trustStorePassword)) {
			configuration.put("ssl.truststore.password", trustStorePassword);
		}

		if (ValidationUtils.isNotBlank(trustManagerAlgorithm)) {
			configuration.put("ssl.trustmanager.algorithm", trustManagerAlgorithm);
		}

		if (ValidationUtils.isNotBlank(keyStoreType)) {
			configuration.put("ssl.keystore.type", keyStoreType);
		}

		if (ValidationUtils.isNotBlank(keyStoreFilePath)) {
			configuration.put("ssl.keystore.location", keyStoreFilePath);
		}

		if (ValidationUtils.isNotBlank(keyStorePassword)) {
			configuration.put("ssl.keystore.password", keyStorePassword);
		}

		if (ValidationUtils.isNotBlank(keyPassword)) {
			configuration.put("ssl.key.password", keyPassword);
		}

		if (ValidationUtils.isNotBlank(keyManagerAlgorithm)) {
			configuration.put("ssl.keymanager.algorithm", keyManagerAlgorithm);
		}
	}
}
