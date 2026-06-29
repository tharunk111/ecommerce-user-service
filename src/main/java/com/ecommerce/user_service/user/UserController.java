package com.ecommerce.user_service.user;

import com.ecommerce.user_service.auth.dto.UserResponse;
import com.ecommerce.user_service.user.dto.SupportAgentRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserManagementService userManagementService;

	public UserController(UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}

	@PostMapping("/support-agents")
	public ResponseEntity<UserResponse> createSupportAgent(@Valid @RequestBody SupportAgentRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userManagementService.createSupportAgent(request));
	}

	@GetMapping("/customers")
	public List<UserResponse> getAllCustomers() {
		return userManagementService.getAllCustomers();
	}
}
