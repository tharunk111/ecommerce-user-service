package com.ecommerce.user_service.auth.dto;

import java.time.Instant;

public record AuthResponse(
		String tokenType,
		String accessToken,
		Instant accessTokenExpiresAt,
		String refreshToken,
		Instant refreshTokenExpiresAt,
		UserResponse user) {
}
