package com.rbu.parking.dto.response;

import com.rbu.parking.model.PaymentStatus;
import com.rbu.parking.model.VehicleBooking;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Vehicle booking response details")
public class BookingResponseDto {

    @Schema(description = "Booking ID", example = "101")
    private Long id;

    @Schema(description = "Vehicle registration number", example = "MH-12-AB-1234")
    private String vehicleNumber;

    @Schema(description = "Allocated parking slot details")
    private ParkingSlotResponseDto slot;

    @Schema(description = "Entry timestamp", example = "2026-10-28T10:00:00")
    private LocalDateTime entryTime;

    @Schema(description = "Exit timestamp", example = "2026-10-28T12:15:00")
    private LocalDateTime exitTime;

    @Schema(description = "Parking duration in minutes", example = "135")
    private Long durationMinutes;

    @Schema(description = "Total parking fare in INR", example = "50.00")
    private BigDecimal totalFare;

    @Schema(description = "Payment and lifecycle status", example = "PENDING")
    private PaymentStatus paymentStatus;

    @Schema(description = "Booking record creation time", example = "2026-10-28T10:00:00")
    private LocalDateTime createdAt;

    public static BookingResponseDto fromEntity(VehicleBooking booking) {
        if (booking == null) return null;
        return BookingResponseDto.builder()
                .id(booking.getId())
                .vehicleNumber(booking.getVehicleNumber())
                .slot(ParkingSlotResponseDto.fromEntity(booking.getSlot()))
                .entryTime(booking.getEntryTime())
                .exitTime(booking.getExitTime())
                .durationMinutes(booking.getDurationMinutes())
                .totalFare(booking.getTotalFare())
                .paymentStatus(booking.getPaymentStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
