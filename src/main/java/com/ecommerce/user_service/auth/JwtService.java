package com.ecommerce.user_service.auth;

import com.ecommerce.user_service.user.User;
import com.ecommerce.user_service.user.UserRole;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

	private final JwtProperties properties;
	private final ObjectMapper objectMapper;
	private final PrivateKey privateKey;
	private final PublicKey publicKey;

	public JwtService(JwtProperties properties, ObjectMapper objectMapper) throws IOException, GeneralSecurityException {
		this.properties = properties;
		this.objectMapper = objectMapper;
		this.privateKey = loadPrivateKey(properties);
		this.publicKey = loadPublicKey(properties);
	}

	public TokenDetails generateAccessToken(User user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(properties.getAccessTokenTtl());
		Map<String, Object> header = new LinkedHashMap<>();
		header.put("alg", "RS256");
		header.put("typ", "JWT");

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("iss", properties.getIssuer());
		payload.put("sub", user.getEmail());
		payload.put("userId", user.getId().toString());
		payload.put("email", user.getEmail());
		payload.put("role", user.getRole().name());
		payload.put("iat", now.getEpochSecond());
		payload.put("exp", expiresAt.getEpochSecond());

		try {
			String encodedHeader = encodeJson(header);
			String encodedPayload = encodeJson(payload);
			String unsignedToken = encodedHeader + "." + encodedPayload;
			return new TokenDetails(unsignedToken + "." + sign(unsignedToken), expiresAt);
		}
		catch (IOException | GeneralSecurityException ex) {
			throw new IllegalStateException("Unable to generate JWT access token", ex);
		}
	}

	public JwtClaims parseAndValidate(String token) {
		try {
			String[] tokenParts = token.split("\\.");
			if (tokenParts.length != 3 || !verify(tokenParts[0] + "." + tokenParts[1], tokenParts[2])) {
				throw new IllegalArgumentException("Invalid JWT token");
			}

			Map<String, Object> payload = objectMapper.readValue(
					BASE64_URL_DECODER.decode(tokenParts[1]),
					new TypeReference<>() {
					});
			if (!properties.getIssuer().equals(payload.get("iss"))) {
				throw new IllegalArgumentException("Invalid JWT issuer");
			}

			Instant expiresAt = Instant.ofEpochSecond(((Number) payload.get("exp")).longValue());
			if (expiresAt.isBefore(Instant.now())) {
				throw new IllegalArgumentException("JWT token has expired");
			}

			return new JwtClaims(
					UUID.fromString((String) payload.get("userId")),
					(String) payload.get("email"),
					UserRole.valueOf((String) payload.get("role")),
					expiresAt);
		}
		catch (IOException | GeneralSecurityException | RuntimeException ex) {
			throw new IllegalArgumentException("Invalid JWT token", ex);
		}
	}

	private String encodeJson(Map<String, Object> value) throws IOException {
		return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
	}

	private String sign(String unsignedToken) throws GeneralSecurityException {
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey);
		signature.update(unsignedToken.getBytes(StandardCharsets.UTF_8));
		return BASE64_URL_ENCODER.encodeToString(signature.sign());
	}

	private boolean verify(String unsignedToken, String encodedSignature) throws GeneralSecurityException {
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initVerify(publicKey);
		signature.update(unsignedToken.getBytes(StandardCharsets.UTF_8));
		return signature.verify(BASE64_URL_DECODER.decode(encodedSignature));
	}

	private PrivateKey loadPrivateKey(JwtProperties properties) throws IOException, GeneralSecurityException {
		byte[] keyBytes = readPem(properties.getPrivateKeyLocation().getContentAsString(StandardCharsets.UTF_8));
		return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
	}

	private PublicKey loadPublicKey(JwtProperties properties) throws IOException, GeneralSecurityException {
		byte[] keyBytes = readPem(properties.getPublicKeyLocation().getContentAsString(StandardCharsets.UTF_8));
		return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
	}

	private byte[] readPem(String pem) {
		String normalized = pem
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replace("-----BEGIN PUBLIC KEY-----", "")
				.replace("-----END PUBLIC KEY-----", "")
				.replaceAll("\\s", "");
		return Base64.getDecoder().decode(normalized);
	}

	public record TokenDetails(String token, Instant expiresAt) {
	}
}
