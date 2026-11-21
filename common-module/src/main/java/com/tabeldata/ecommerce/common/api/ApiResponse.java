package com.tabeldata.ecommerce.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        OffsetDateTime timestamp,
        Map<String, String> errors
) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, OffsetDateTime.now(), null);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok("Success", data);
    }

    public static ApiResponse<Void> okMessage(String message) {
        return new ApiResponse<>(true, message, null, OffsetDateTime.now(), null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, OffsetDateTime.now(), null);
    }

    public static <T> ApiResponse<T> error(String message, Map<String, String> errors) {
        return new ApiResponse<>(false, message, null, OffsetDateTime.now(), errors);
    }
}
