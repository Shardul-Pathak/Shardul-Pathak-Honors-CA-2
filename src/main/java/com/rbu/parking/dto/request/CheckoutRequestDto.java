package com.rbu.parking.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for vehicle checkout and billing")
public class CheckoutRequestDto {

    @PastOrPresent(message = "Exit time cannot be in the future")
    @Schema(description = "Optional exit timestamp. Defaults to current system time if omitted.", example = "2026-10-28T13:15:00")
    private LocalDateTime exitTime;
}
