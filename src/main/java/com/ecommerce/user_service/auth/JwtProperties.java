package com.ecommerce.user_service.auth;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

	@NotNull(message = "app.jwt.private-key-location must be configured")
	private Resource privateKeyLocation;

	@NotNull(message = "app.jwt.public-key-location must be configured")
	private Resource publicKeyLocation;

	private String issuer = "user-service";

	@NotNull
	private Duration accessTokenTtl = Duration.ofMinutes(15);

	@NotNull
	private Duration refreshTokenTtl = Duration.ofDays(30);
}
