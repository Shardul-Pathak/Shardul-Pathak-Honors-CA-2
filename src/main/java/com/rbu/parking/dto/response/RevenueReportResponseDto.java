package com.rbu.parking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Daily aggregate revenue reporting response")
public class RevenueReportResponseDto {

    @Schema(description = "Reporting calendar date", example = "2026-10-28")
    private LocalDate reportDate;

    @Schema(description = "Total number of completed vehicle bookings on this date", example = "42")
    private Long totalCompletedBookings;

    @Schema(description = "Total aggregate revenue generated", example = "3550.00")
    private BigDecimal totalRevenue;

    @Schema(description = "Currency", example = "INR")
    private String currency;

    @Schema(description = "Report generation timestamp", example = "2026-10-28T18:00:00")
    private LocalDateTime generatedAt;
}
