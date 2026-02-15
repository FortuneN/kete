package io.github.fortunen.kete.destinations.zeromq;

import java.nio.file.Files;
import java.nio.file.Path;

import org.zeromq.SocketType;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.ConfigurationUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class ZeroMQDestinationConfig extends DestinationConfig {

	public static final String PUSH = "PUSH";
	public static final String BIND = "BIND";
	public static final String CURVE = "curve";
	public static final String LINGER = "linger";
	public static final int DEFAULT_LINGER = 1000;
	public static final String CONNECT = "CONNECT";
	public static final String PUBLISH = "PUBLISH";
	public static final String ENVELOPE = "envelope";
	public static final String ENDPOINT = "endpoint";
	public static final String SOCKET_TYPE = "socket-type";
	public static final String DEFAULT_SOCKET_TYPE = PUBLISH;
	public static final String DEFAULT_CONNECTION_MODE = CONNECT;
	public static final String CONNECTION_MODE = "connection-mode";
	public static final String SEND_HIGH_WATER_MARK = "send-high-water-mark";

	private int linger;
	private String endpoint;
	private String envelope;
	private String socketType;
	private boolean hasEnvelope;
	private boolean curveEnabled;
	private byte[] curveServerKey;
	private byte[] curvePublicKey;
	private byte[] curveSecretKey;
	private String connectionMode;
	private int sendHighWaterMark;
	private SocketType socketTypeValue;
	private boolean isEnvelopeTemplated;
	private boolean hasSendHighWaterMark;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		// endpoint

		endpoint = ValidationUtils.requireNonBlank(configuration.getString(ENDPOINT, "").trim(), ENDPOINT + " is required");

		// socket type

		socketType = configuration.getString(SOCKET_TYPE, DEFAULT_SOCKET_TYPE).trim().toUpperCase();

		ValidationUtils.requireTrue(socketType.equals(PUBLISH) || socketType.equals(PUSH), SOCKET_TYPE + " must be one of : " + PUBLISH + " or " + PUSH);

		if (socketType.equals(PUBLISH)) {
			socketTypeValue = SocketType.PUB;
		} else {
			socketTypeValue = SocketType.PUSH;
		}

		// connection mode

		connectionMode = configuration.getString(CONNECTION_MODE, DEFAULT_CONNECTION_MODE).trim().toUpperCase();

		ValidationUtils.requireTrue(connectionMode.equals(BIND) || connectionMode.equals(CONNECT), CONNECTION_MODE + " must be one of : " + BIND + " or " + CONNECT);

		// linger (milliseconds to wait for pending messages on close, -1 = infinite, 0 = discard immediately)

		linger = configuration.getInt(LINGER, DEFAULT_LINGER);

		ValidationUtils.requireGreaterThan(linger, -2, LINGER + " must be -1 or greater");

		// send-high-water-mark (0 = unlimited, maps to ZMQ_SNDHWM)

		if (configuration.containsKey(SEND_HIGH_WATER_MARK)) {
			sendHighWaterMark = ValidationUtils.requireNonNegative(configuration.getInt(SEND_HIGH_WATER_MARK, 1000), SEND_HIGH_WATER_MARK + " must be non-negative");
			hasSendHighWaterMark = true;
		}

		// authentication-type

		if (hasAuthenticationType) {
			switch (authenticationType) {
				case "curve" -> {
					curveEnabled = true;
					var curveConfig = ConfigurationUtils.getSubSet(configuration, CURVE);
					var loaderKind = ValidationUtils.requireNonBlank(curveConfig.getString("loader.kind", "").trim(), "curve.loader.kind is required when authentication-type is 'curve'");

					switch (loaderKind) {
						case "z85-inline" -> {
							var serverKeyZ85 = ValidationUtils.requireNonBlank(curveConfig.getString("server-key", "").trim(), "curve.server-key is required");
							var publicKeyZ85 = ValidationUtils.requireNonBlank(curveConfig.getString("public-key", "").trim(), "curve.public-key is required");
							var secretKeyZ85 = ValidationUtils.requireNonBlank(curveConfig.getString("secret-key", "").trim(), "curve.secret-key is required");
							curveServerKey = z85Decode(serverKeyZ85);
							curvePublicKey = z85Decode(publicKeyZ85);
							curveSecretKey = z85Decode(secretKeyZ85);
						}
						case "z85-file-path" -> {
							var serverKeyPath = ValidationUtils.requireNonBlank(curveConfig.getString("server-key", "").trim(), "curve.server-key is required");
							var publicKeyPath = ValidationUtils.requireNonBlank(curveConfig.getString("public-key", "").trim(), "curve.public-key is required");
							var secretKeyPath = ValidationUtils.requireNonBlank(curveConfig.getString("secret-key", "").trim(), "curve.secret-key is required");
							curveServerKey = z85Decode(Files.readString(Path.of(serverKeyPath)).trim());
							curvePublicKey = z85Decode(Files.readString(Path.of(publicKeyPath)).trim());
							curveSecretKey = z85Decode(Files.readString(Path.of(secretKeyPath)).trim());
						}
						case "binary-file-path" -> {
							var serverKeyPath = ValidationUtils.requireNonBlank(curveConfig.getString("server-key", "").trim(), "curve.server-key is required");
							var publicKeyPath = ValidationUtils.requireNonBlank(curveConfig.getString("public-key", "").trim(), "curve.public-key is required");
							var secretKeyPath = ValidationUtils.requireNonBlank(curveConfig.getString("secret-key", "").trim(), "curve.secret-key is required");
							curveServerKey = Files.readAllBytes(Path.of(serverKeyPath));
							curvePublicKey = Files.readAllBytes(Path.of(publicKeyPath));
							curveSecretKey = Files.readAllBytes(Path.of(secretKeyPath));
							ValidationUtils.requireTrue(curveServerKey.length == 32, "curve.server-key must be 32 bytes");
							ValidationUtils.requireTrue(curvePublicKey.length == 32, "curve.public-key must be 32 bytes");
							ValidationUtils.requireTrue(curveSecretKey.length == 32, "curve.secret-key must be 32 bytes");
						}
						default -> throw new IllegalStateException("curve.loader.kind must be one of: z85-inline, z85-file-path, binary-file-path");
					}
				}
				default -> throw new IllegalStateException(AUTHENTICATION_TYPE + " must be one of: curve");
			}
		}

		// envelope (optional, PUBLISH only)

		var envelopeStr = configuration.getString(ENVELOPE, "").trim();

		if (ValidationUtils.isNotBlank(envelopeStr)) {
			ValidationUtils.requireTrue(socketType.equals(PUBLISH), ENVELOPE + " is only valid for socket-type: " + PUBLISH);
			envelope = envelopeStr;
			hasEnvelope = true;
			isEnvelopeTemplated = TemplateUtils.containsTemplate(envelope);
		}
	}

	static byte[] z85Decode(String encoded) {
		ValidationUtils.requireTrue(encoded.length() == 40, "Z85-encoded key must be exactly 40 characters, got " + encoded.length());
		var decoder = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.-:+=^!/*?&<>()[]{}@%$#";
		var decoded = new byte[32];
		for (var i = 0; i < 8; i++) {
			long value = 0;
			for (var j = 0; j < 5; j++) {
				var index = decoder.indexOf(encoded.charAt(i * 5 + j));
				ValidationUtils.requireTrue(index >= 0, "Invalid Z85 character: " + encoded.charAt(i * 5 + j));
				value = value * 85 + index;
			}
			decoded[i * 4] = (byte) ((value >> 24) & 0xFF);
			decoded[i * 4 + 1] = (byte) ((value >> 16) & 0xFF);
			decoded[i * 4 + 2] = (byte) ((value >> 8) & 0xFF);
			decoded[i * 4 + 3] = (byte) (value & 0xFF);
		}
		return decoded;
	}
}