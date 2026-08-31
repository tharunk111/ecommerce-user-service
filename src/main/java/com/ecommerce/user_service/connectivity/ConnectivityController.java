package com.ecommerce.user_service.connectivity;

import com.ecommerce.user_service.connectivity.dto.ConnectivityResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/connectivity")
public class ConnectivityController {

	private final ConnectivityService connectivityService;

	public ConnectivityController(ConnectivityService connectivityService) {
		this.connectivityService = connectivityService;
	}

	@GetMapping("/order-service")
	public ResponseEntity<ConnectivityResponse> checkOrderService() {
		ConnectivityResponse response = connectivityService.checkOrderService();
		return ResponseEntity
				.status(response.reachable() ? HttpStatus.OK : HttpStatus.BAD_GATEWAY)
				.body(response);
	}
}
