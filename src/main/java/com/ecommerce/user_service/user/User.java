package com.ecommerce.user_service.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true, length = 150)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false, length = 75)
	private String firstName;

	@Column(nullable = false, length = 75)
	private String lastName;

	@Column(length = 25)
	private String phoneNumber;

	@Column(length = 500)
	private String defaultShippingAddress;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private UserRole role = UserRole.ROLE_CUSTOMER;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AccountStatus status = AccountStatus.ACTIVE;

	@Column(length = 128)
	private String refreshTokenHash;

	private Instant refreshTokenExpiresAt;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}
}
