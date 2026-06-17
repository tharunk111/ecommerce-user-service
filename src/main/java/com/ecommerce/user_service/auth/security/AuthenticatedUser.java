package com.ecommerce.user_service.auth.security;

import com.ecommerce.user_service.user.UserRole;
import java.util.UUID;

public record AuthenticatedUser(UUID id, String email, UserRole role) {
}
