package com.fellowlodge.api.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard API response envelope returned by every endpoint.
 * <pre>
 * {
 *   "timestamp": "...",
 *   "status": 200,
 *   "success": true,
 *   "message": "...",
 *   "data": { ... },
 *   "pagination": { ... }   // present only for paged responses
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String timestamp;
    private int status;
    private boolean success;
    private String message;
    private T data;
    private PageResponse pagination;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now().toString())
                .status(200)
                .success(true)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now().toString())
                .status(200)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now().toString())
                .status(201)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, PageResponse pagination) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now().toString())
                .status(200)
                .success(true)
                .message("Success")
                .data(data)
                .pagination(pagination)
                .build();
    }

    public static <T> ApiResponse<T> deleted(String message) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now().toString())
                .status(200)
                .success(true)
                .message(message)
                .build();
    }
}
