package com.ecommerce.user_service.auth;

import com.ecommerce.user_service.auth.dto.AuthResponse;
import com.ecommerce.user_service.auth.dto.LoginRequest;
import com.ecommerce.user_service.auth.dto.SignupRequest;
import com.ecommerce.user_service.auth.dto.UserResponse;
import com.ecommerce.user_service.auth.exception.AccountNotActiveException;
import com.ecommerce.user_service.auth.exception.DuplicateResourceException;
import com.ecommerce.user_service.auth.exception.InvalidCredentialsException;
import com.ecommerce.user_service.user.AccountStatus;
import com.ecommerce.user_service.user.User;
import com.ecommerce.user_service.user.UserRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			RefreshTokenService refreshTokenService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
	}

	@Transactional
	public AuthResponse signup(SignupRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new DuplicateResourceException("Email is already registered");
		}

		User user = new User();
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.password()));
		user.setFirstName(request.firstName().trim());
		user.setLastName(request.lastName().trim());
		user.setPhoneNumber(blankToNull(request.phoneNumber()));
		user.setDefaultShippingAddress(blankToNull(request.defaultShippingAddress()));

		return issueTokens(userRepository.save(user));
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
				.orElseThrow(InvalidCredentialsException::new);
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new InvalidCredentialsException();
		}
		if (user.getStatus() != AccountStatus.ACTIVE) {
			throw new AccountNotActiveException();
		}

		return issueTokens(user);
	}

	private AuthResponse issueTokens(User user) {
		JwtService.TokenDetails accessToken = jwtService.generateAccessToken(user);
		RefreshTokenService.GeneratedRefreshToken refreshToken = refreshTokenService.generate();
		user.setRefreshTokenHash(refreshToken.tokenHash());
		user.setRefreshTokenExpiresAt(refreshToken.expiresAt());
		userRepository.save(user);

		return new AuthResponse(
				"Bearer",
				accessToken.token(),
				accessToken.expiresAt(),
				refreshToken.token(),
				refreshToken.expiresAt(),
				UserResponse.from(user));
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
