package com.ecommerce.auth.exception;

import org.springframework.http.HttpStatus;

import io.jsonwebtoken.JwtException;

public class TokenInvalidException extends JwtException {
    public TokenInvalidException(String message) {
        super(message);
    }

    public TokenInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
