package com.ecommerce.user_service.connectivity;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Feign client for order-service, resolved via Eureka service discovery.
 */
@FeignClient(name = "order-service")
public interface OrderServiceClient {

	@GetMapping(path = "/api/v1/orders/test/")
	String test();
}
