package com.sawli.shortlinkx.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Consistent envelope structure returned by all API endpoints.
 *
 * @param <T> the type of data returned in the response payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Helper to create a success API response with data and message.
     *
     * @param data    the response payload
     * @param message custom success message
     * @param <T>     data type
     * @return response envelope
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Helper to create an error API response.
     *
     * @param message error description message
     * @param <T>     data type
     * @return response envelope
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
