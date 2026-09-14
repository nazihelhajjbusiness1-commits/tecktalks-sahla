package com.farmmanagement.backend.common.response;

import java.util.Map;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Map<String, String> errors;

    public ApiResponse(
            boolean success,
            String message,
            T data,
            Map<String, String> errors
    ) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                true,
                message,
                data,
                null
        );
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(
                false,
                message,
                null,
                null
        );
    }

    public static ApiResponse<Void> validationError(
            String message,
            Map<String, String> errors
    ) {
        return new ApiResponse<>(
                false,
                message,
                null,
                errors
        );
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}