package com.ecommerce.user_service.auth.exception;

public class InvalidClientCredentialsException extends RuntimeException {

	public InvalidClientCredentialsException() {
		super("Invalid client credentials");
	}
}
