package com.ecommerce.common.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.ApiResponseUtil;
import com.ecommerce.user.Exception.RefreshTokenException;
import com.ecommerce.user.Exception.SessionExpiredException;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    //DTO 유효성 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        log.error("Validation failed for fields: {} | Details: {}", errors.keySet(), ex.getMessage());
        return ResponseEntity.status(httpStatus)
                             .body(ApiResponseUtil.createResponse(httpStatus.value(), errors,"Validation failed. Please correct the highlighted fields."));
    }

    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleSessionExpiredException(SessionExpiredException ex) {
        log.warn("Session expired for user. Reason: {}", ex.getMessage());
        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(httpStatus)
                .body(ApiResponseUtil.createResponse(httpStatus.value(), "session has expired."));
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenException(RefreshTokenException ex) {
        log.error(ex.getMessage());
        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(httpStatus)
                .body(ApiResponseUtil.createResponse(httpStatus.value(), ex.getMessage()));
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("An error occurred: {}", ex.getMessage(), ex);
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(httpStatus)
                .body(ApiResponseUtil.createResponse(httpStatus.value(), "Internal Servel Error"));
    }
}

