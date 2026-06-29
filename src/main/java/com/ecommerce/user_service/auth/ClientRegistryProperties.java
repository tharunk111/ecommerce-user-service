package com.ecommerce.user_service.auth;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth")
public class ClientRegistryProperties {

	private List<ClientRegistration> clients = new ArrayList<>();

	@Getter
	@Setter
	public static class ClientRegistration {

		private String clientId;
		private String clientSecret;
		private List<String> authorities = List.of("ROLE_SERVICE_CLIENT");
	}
}
