package com.farmmanagement.backend.common.exception;

public class InvalidPricingException extends RuntimeException {

    public InvalidPricingException(String message) {
        super(message);
    }
}
