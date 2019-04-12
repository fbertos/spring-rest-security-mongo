package org.fbertos.authorization.config;

import org.springframework.security.core.AuthenticationException;

public class InvalidJwtAuthenticationException extends AuthenticationException {
	private static final long serialVersionUID = 3860776090697360362L;

	public InvalidJwtAuthenticationException(String e) {
        super(e);
    }
}
