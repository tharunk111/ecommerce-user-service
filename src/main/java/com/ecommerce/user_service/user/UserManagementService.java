package com.ecommerce.user_service.user;

import com.ecommerce.user_service.auth.dto.UserResponse;
import com.ecommerce.user_service.auth.exception.DuplicateResourceException;
import com.ecommerce.user_service.user.dto.SupportAgentRequest;
import java.util.List;
import java.util.Locale;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagementService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserManagementService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@PreAuthorize("hasAuthority('ROLE_SERVICE_CLIENT')")
	@Transactional
	public UserResponse createSupportAgent(SupportAgentRequest request) {
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
		user.setRole(UserRole.ROLE_SUPPORT_AGENT);

		return UserResponse.from(userRepository.save(user));
	}

	@PreAuthorize("hasAuthority('ROLE_SUPPORT_AGENT')")
	@Transactional(readOnly = true)
	public List<UserResponse> getAllCustomers() {
		return userRepository.findAllByRoleOrderByCreatedAtDesc(UserRole.ROLE_CUSTOMER).stream()
				.map(UserResponse::from)
				.toList();
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
