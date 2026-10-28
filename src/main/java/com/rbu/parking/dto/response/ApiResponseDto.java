package com.rbu.parking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Standard API response wrapper")
public class ApiResponseDto<T> {

    @Schema(description = "Success indicator", example = "true")
    private boolean success;

    @Schema(description = "Response summary message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Payload data")
    private T data;

    @Schema(description = "Response timestamp", example = "2026-10-28T12:00:00")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponseDto<T> success(String message, T data) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
