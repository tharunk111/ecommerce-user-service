package com.ecommerce.user_service.connectivity;

import com.ecommerce.user_service.connectivity.dto.ConnectivityResponse;
import feign.FeignException;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ConnectivityService {

	private static final String ORDER_SERVICE = "order-service";

	private final OrderServiceClient orderServiceClient;

	public ConnectivityService(OrderServiceClient orderServiceClient) {
		this.orderServiceClient = orderServiceClient;
	}

	public ConnectivityResponse checkOrderService() {
		try {
			return ConnectivityResponse.reachable(ORDER_SERVICE, orderServiceClient.test());
		}
		catch (Exception ex) {
			log.warn("Connectivity check against {} failed", ORDER_SERVICE, ex);
			return ConnectivityResponse.unreachable(ORDER_SERVICE, describe(ex));
		}
	}

	private String describe(Exception ex) {
		if (ex instanceof FeignException feignException) {
			return "HTTP " + feignException.status() + " from " + ORDER_SERVICE + ": " + feignException.getMessage();
		}
		return ex.getClass().getSimpleName() + ": " + ex.getMessage();
	}
}
