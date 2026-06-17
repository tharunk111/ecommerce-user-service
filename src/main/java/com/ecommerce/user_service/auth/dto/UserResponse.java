package com.ecommerce.user_service.auth.dto;

import com.ecommerce.user_service.user.AccountStatus;
import com.ecommerce.user_service.user.User;
import com.ecommerce.user_service.user.UserRole;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
		UUID id,
		String email,
		String firstName,
		String lastName,
		String phoneNumber,
		String defaultShippingAddress,
		UserRole role,
		AccountStatus status,
		Instant createdAt) {

	public static UserResponse from(User user) {
		return new UserResponse(
				user.getId(),
				user.getEmail(),
				user.getFirstName(),
				user.getLastName(),
				user.getPhoneNumber(),
				user.getDefaultShippingAddress(),
				user.getRole(),
				user.getStatus(),
				user.getCreatedAt());
	}
}
