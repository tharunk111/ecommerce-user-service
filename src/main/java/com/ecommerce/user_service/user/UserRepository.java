package com.ecommerce.user_service.user;

import com.ecommerce.user_service.entities.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

	boolean existsByEmailIgnoreCase(String email);

	Optional<User> findByEmailIgnoreCase(String email);

	java.util.List<User> findAllByRoleOrderByCreatedAtDesc(UserRole role);
}
