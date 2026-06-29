package com.ecommerce.user_service.auth.dto;

import java.time.Instant;

public record ClientTokenResponse(
		String tokenType,
		String accessToken,
		Instant accessTokenExpiresAt) {
}
