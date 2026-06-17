package com.ecommerce.user_service.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
		@NotBlank @Email @Size(max = 150) String email,
		@NotBlank @Size(min = 8, max = 72) String password,
		@NotBlank @Size(max = 75) String firstName,
		@NotBlank @Size(max = 75) String lastName,
		@Pattern(regexp = "^$|^[+]?[0-9]{7,15}$", message = "must be a valid phone number") String phoneNumber,
		@Size(max = 500) String defaultShippingAddress) {
}
