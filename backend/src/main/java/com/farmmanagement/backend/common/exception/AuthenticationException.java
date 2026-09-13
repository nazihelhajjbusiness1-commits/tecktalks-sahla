package com.farmmanagement.backend.common.exception;

public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}