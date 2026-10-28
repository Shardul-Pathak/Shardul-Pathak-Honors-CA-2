package com.rbu.parking.controller;

import com.rbu.parking.dto.request.BookingRequestDto;
import com.rbu.parking.dto.request.CheckoutRequestDto;
import com.rbu.parking.dto.response.ApiResponseDto;
import com.rbu.parking.dto.response.BookingResponseDto;
import com.rbu.parking.dto.response.ErrorResponseDto;
import com.rbu.parking.dto.response.FareCalculationResponseDto;
import com.rbu.parking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking & Billing Lifecycle", description = "Endpoints for vehicle entry, active occupancies, real-time fare calculation, and exit checkout")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/entry")
    @Operation(
            summary = "Allocate slot upon vehicle entry",
            description = "Allocates an available parking slot to the incoming vehicle, creates an active booking session, and marks slot as OCCUPIED."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Slot allocated successfully",
            content = @Content(schema = @Schema(implementation = BookingResponseDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Validation failure on vehicle number or timestamps",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    )
    @ApiResponse(
            responseCode = "409",
            description = "Conflict: Vehicle is already actively parked or requested slot is full/occupied",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    )
    public ResponseEntity<ApiResponseDto<BookingResponseDto>> bookSlot(
            @Valid @RequestBody BookingRequestDto request
    ) {
        BookingResponseDto booking = bookingService.bookSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Slot allocated successfully upon vehicle entry", booking));
    }

    @GetMapping("/active")
    @Operation(
            summary = "Fetch all active parking records",
            description = "Retrieves all current active parking sessions (currently occupied slots with PENDING payment status)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Active parking records retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BookingResponseDto.class)))
    )
    public ResponseEntity<ApiResponseDto<List<BookingResponseDto>>> getActiveBookings() {
        List<BookingResponseDto> activeBookings = bookingService.getActiveBookings();
        return ResponseEntity.ok(ApiResponseDto.success("Active parking bookings retrieved successfully", activeBookings));
    }

    @GetMapping("/{bookingId}/fare")
    @Operation(
            summary = "Real-time fare calculation",
            description = "Calculates the total parking fee accrued at any given time based on ₹50 per 3 hours (or fraction thereof)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Real-time fare calculated successfully",
            content = @Content(schema = @Schema(implementation = FareCalculationResponseDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Evaluation timestamp is invalid or earlier than entry time",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Booking ID not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    )
    public ResponseEntity<ApiResponseDto<FareCalculationResponseDto>> calculateFare(
            @PathVariable Long bookingId,
            @Parameter(description = "Optional evaluation timestamp (ISO-8601). Defaults to current system time if omitted.", example = "2026-10-28T13:05:00")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime asOf
    ) {
        FareCalculationResponseDto fare = bookingService.calculateRealTimeFare(bookingId, asOf);
        return ResponseEntity.ok(ApiResponseDto.success("Real-time fare calculated successfully", fare));
    }

    @PostMapping("/{bookingId}/checkout")
    @Operation(
            summary = "Process vehicle exit and billing",
            description = "Finalizes the booking session, calculates the total billable fee, marks payment as COMPLETED, and frees up the allocated parking slot."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Checkout processed and slot freed up successfully",
            content = @Content(schema = @Schema(implementation = BookingResponseDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Exit time is before entry time or in the future",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Booking ID not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    )
    @ApiResponse(
            responseCode = "409",
            description = "Booking has already been checked out and completed",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    )
    public ResponseEntity<ApiResponseDto<BookingResponseDto>> checkout(
            @PathVariable Long bookingId,
            @Valid @RequestBody(required = false) CheckoutRequestDto request
    ) {
        BookingResponseDto checkedOutBooking = bookingService.checkout(bookingId, request);
        return ResponseEntity.ok(ApiResponseDto.success("Vehicle checked out and slot freed up successfully", checkedOutBooking));
    }
}
