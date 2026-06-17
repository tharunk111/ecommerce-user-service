package com.ecommerce.user_service.auth;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

	private Resource privateKeyLocation;
	private Resource publicKeyLocation;
	private String issuer = "user-service";
	private Duration accessTokenTtl = Duration.ofMinutes(15);
	private Duration refreshTokenTtl = Duration.ofDays(30);
}
