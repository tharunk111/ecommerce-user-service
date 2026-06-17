package com.ecommerce.user_service.auth;

import com.ecommerce.user_service.user.UserRole;
import java.time.Instant;
import java.util.UUID;

public record JwtClaims(
		UUID userId,
		String email,
		UserRole role,
		Instant expiresAt) {
}
