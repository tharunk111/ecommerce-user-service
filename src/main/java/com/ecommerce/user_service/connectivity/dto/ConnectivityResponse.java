package com.ecommerce.user_service.connectivity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConnectivityResponse(
		String target,
		boolean reachable,
		String response,
		String error) {

	public static ConnectivityResponse reachable(String target, String response) {
		return new ConnectivityResponse(target, true, response, null);
	}

	public static ConnectivityResponse unreachable(String target, String error) {
		return new ConnectivityResponse(target, false, null, error);
	}
}
