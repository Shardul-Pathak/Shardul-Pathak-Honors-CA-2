package com.rbu.parking.controller;

import com.rbu.parking.dto.response.ApiResponseDto;
import com.rbu.parking.dto.response.RevenueReportResponseDto;
import com.rbu.parking.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Revenue & Analytics", description = "Endpoints for aggregate financial reporting and parking utilization metrics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/revenue")
    @Operation(
            summary = "Aggregate revenue report for a specified date",
            description = "Calculates and displays total aggregate revenue and number of completed vehicle bookings for the specified calendar date."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Revenue report generated successfully",
            content = @Content(schema = @Schema(implementation = RevenueReportResponseDto.class))
    )
    public ResponseEntity<ApiResponseDto<RevenueReportResponseDto>> getRevenueReport(
            @Parameter(description = "Reporting calendar date in ISO format (YYYY-MM-DD). Defaults to today if omitted.", example = "2026-10-28")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        RevenueReportResponseDto report = analyticsService.getRevenueReport(date);
        return ResponseEntity.ok(ApiResponseDto.success("Daily revenue report generated successfully", report));
    }
}
