package com.ecommerce.user_service.auth.exception;

public class AccountNotActiveException extends RuntimeException {

	public AccountNotActiveException() {
		super("Account is not active");
	}
}
