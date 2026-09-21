package com.farmmanagement.backend.common.exception;

/**
 * Thrown for business/domain validation failures that should surface as a 400
 * (e.g. a grade that does not belong to the selected product, an invalid price
 * period, or an overlapping active price rule). Handled by
 * {@link GlobalExceptionHandler} so grade and pricing endpoints return the same
 * error shape as the farmer and product endpoints.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
