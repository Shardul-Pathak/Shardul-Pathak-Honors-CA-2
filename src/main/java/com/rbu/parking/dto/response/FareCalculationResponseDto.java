package com.rbu.parking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Real-time fare calculation response accrued at any given time")
public class FareCalculationResponseDto {

    @Schema(description = "Booking ID", example = "101")
    private Long bookingId;

    @Schema(description = "Vehicle registration number", example = "MH-12-AB-1234")
    private String vehicleNumber;

    @Schema(description = "Slot number currently allocated", example = "G-01")
    private String slotNumber;

    @Schema(description = "Vehicle entry time", example = "2026-10-28T10:00:00")
    private LocalDateTime entryTime;

    @Schema(description = "Current calculation evaluation time", example = "2026-10-28T13:05:00")
    private LocalDateTime calculationTime;

    @Schema(description = "Duration elapsed so far in minutes", example = "185")
    private Long elapsedMinutes;

    @Schema(description = "Number of 3-hour billing blocks billed", example = "2")
    private Long billingBlocks;

    @Schema(description = "Total fare accrued so far", example = "100.00")
    private BigDecimal accruedFare;

    @Schema(description = "Currency", example = "INR")
    private String currency;

    @Schema(description = "Pricing policy description", example = "Flat ₹50 per 3 hours (or fraction thereof)")
    private String pricingPolicy;
}
