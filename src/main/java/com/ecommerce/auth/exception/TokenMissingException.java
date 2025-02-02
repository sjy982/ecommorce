package com.ecommerce.auth.exception;

import org.springframework.http.HttpStatus;

import io.jsonwebtoken.JwtException;

public class TokenMissingException extends JwtException {

    public TokenMissingException(String message) {
        super(message);
    }

    public TokenMissingException(String message, Throwable cause) {
        super(message, cause);
    }
}
