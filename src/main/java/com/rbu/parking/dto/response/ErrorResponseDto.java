package com.rbu.parking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Standardized JSON error message response")
public class ErrorResponseDto {

    @Schema(description = "Error timestamp", example = "2026-10-28T12:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "HTTP error reason phrase", example = "Bad Request")
    private String error;

    @Schema(description = "Primary error message", example = "Validation failed for one or more fields")
    private String message;

    @Schema(description = "Request URI path", example = "/api/v1/bookings/entry")
    private String path;

    @Schema(description = "Detailed field validation errors or violation notes")
    private List<String> errors;
}
