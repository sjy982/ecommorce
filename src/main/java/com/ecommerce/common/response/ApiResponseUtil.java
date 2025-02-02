package com.ecommerce.common.response;

import java.time.LocalDateTime;

public class ApiResponseUtil {
    public static <T> ApiResponse<T> createResponse(int status, T data, String message) {
        return new ApiResponse<>(
                LocalDateTime.now(),
                status,
                message,
                data
        );
    }

    public static ApiResponse<Void> createResponse(int status, String message) {
        return new ApiResponse<>(
                LocalDateTime.now(),
                status,
                message,
                null
        );
    }
}
