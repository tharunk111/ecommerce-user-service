package com.ecommerce.user_service.auth;

import com.ecommerce.user_service.user.UserRole;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JwtClaims(
		UUID userId,
		String subject,
		String email,
		UserRole role,
		List<String> authorities,
		boolean clientCredentials,
		Instant expiresAt) {
}
