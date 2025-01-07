package com.ecommerce.common.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private LocalDateTime timestamp;
    private int status;
    private String message;
    private T data;
}
