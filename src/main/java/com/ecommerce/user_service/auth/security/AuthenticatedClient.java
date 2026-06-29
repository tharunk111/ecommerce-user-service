package com.ecommerce.user_service.auth.security;

import java.util.List;

public record AuthenticatedClient(String clientId, List<String> authorities) {
}
