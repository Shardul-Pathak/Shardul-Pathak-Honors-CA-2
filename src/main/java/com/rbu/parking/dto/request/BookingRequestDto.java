package com.rbu.parking.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for parking a vehicle and booking a slot")
public class BookingRequestDto {

    @NotBlank(message = "Vehicle number is required")
    @Pattern(regexp = "^[A-Za-z0-9-]{4,15}$", message = "Vehicle number must be 4 to 15 alphanumeric characters (e.g. MH-12-AB-1234)")
    @Schema(description = "Vehicle registration number", example = "MH-12-AB-1234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String vehicleNumber;

    @Positive(message = "Slot ID must be a positive number")
    @Schema(description = "Optional specific slot ID. If omitted, the system automatically allocates the first available slot.", example = "1")
    private Long slotId;

    @PastOrPresent(message = "Entry time cannot be in the future")
    @Schema(description = "Optional entry timestamp. Defaults to current system time if omitted.", example = "2026-10-28T10:00:00")
    private LocalDateTime entryTime;
}
