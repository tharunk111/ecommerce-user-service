package com.ecommerce.user_service.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

	private static final int TOKEN_BYTES = 64;

	private final JwtProperties properties;
	private final SecureRandom secureRandom = new SecureRandom();

	public RefreshTokenService(JwtProperties properties) {
		this.properties = properties;
	}

	public GeneratedRefreshToken generate() {
		byte[] bytes = new byte[TOKEN_BYTES];
		secureRandom.nextBytes(bytes);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		return new GeneratedRefreshToken(token, hash(token), Instant.now().plus(properties.getRefreshTokenTtl()));
	}

	public String hash(String token) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(digest);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is not available", ex);
		}
	}

	public record GeneratedRefreshToken(String token, String tokenHash, Instant expiresAt) {
	}
}
