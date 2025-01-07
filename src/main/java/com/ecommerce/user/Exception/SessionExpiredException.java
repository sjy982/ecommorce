package com.ecommerce.user.Exception;

public class SessionExpiredException extends RuntimeException{
    public SessionExpiredException(String message) {
        super(message);
    }

    public SessionExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
