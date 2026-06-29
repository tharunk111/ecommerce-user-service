package com.ecommerce.user_service.auth;

import com.ecommerce.user_service.auth.dto.ClientCredentialsRequest;
import com.ecommerce.user_service.auth.dto.ClientTokenResponse;
import com.ecommerce.user_service.auth.exception.InvalidClientCredentialsException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ClientAuthenticationService {

	private final ClientRegistryProperties clientRegistryProperties;
	private final JwtService jwtService;
	private final PasswordEncoder passwordEncoder;

	public ClientAuthenticationService(
			ClientRegistryProperties clientRegistryProperties,
			JwtService jwtService,
			PasswordEncoder passwordEncoder) {
		this.clientRegistryProperties = clientRegistryProperties;
		this.jwtService = jwtService;
		this.passwordEncoder = passwordEncoder;
	}

	public ClientTokenResponse authenticate(ClientCredentialsRequest request) {
		ClientRegistryProperties.ClientRegistration client = clientRegistryProperties.getClients().stream()
				.filter(candidate -> secureEquals(candidate.getClientId(), request.clientId()))
				.findFirst()
				.orElseThrow(InvalidClientCredentialsException::new);

		if (!secretMatches(request.clientSecret(), client.getClientSecret())) {
			throw new InvalidClientCredentialsException();
		}

		List<String> authorities = client.getAuthorities() == null || client.getAuthorities().isEmpty()
				? List.of("ROLE_SERVICE_CLIENT")
				: client.getAuthorities();
		JwtService.TokenDetails token = jwtService.generateClientCredentialsToken(client.getClientId(), authorities);
		return new ClientTokenResponse("Bearer", token.token(), token.expiresAt());
	}

	private boolean secretMatches(String rawSecret, String configuredSecret) {
		if (configuredSecret == null) {
			return false;
		}
		if (configuredSecret.startsWith("$2a$") || configuredSecret.startsWith("$2b$")
				|| configuredSecret.startsWith("$2y$")) {
			return passwordEncoder.matches(rawSecret, configuredSecret);
		}
		return secureEquals(configuredSecret, rawSecret);
	}

	private boolean secureEquals(String left, String right) {
		if (left == null || right == null) {
			return false;
		}
		return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
	}
}
