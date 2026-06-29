package com.ecommerce.user_service.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ClientCredentialsRequest(
		@NotBlank String clientId,
		@NotBlank String clientSecret) {
}
