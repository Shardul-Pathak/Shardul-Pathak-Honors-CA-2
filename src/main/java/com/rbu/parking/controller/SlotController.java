package com.rbu.parking.controller;

import com.rbu.parking.dto.response.ApiResponseDto;
import com.rbu.parking.dto.response.ErrorResponseDto;
import com.rbu.parking.dto.response.ParkingSlotResponseDto;
import com.rbu.parking.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/slots")
@RequiredArgsConstructor
@Tag(name = "Slot Management", description = "Endpoints for inspecting real-time parking slot availability and configuration")
public class SlotController {

    private final SlotService slotService;

    @GetMapping("/available")
    @Operation(
            summary = "Check and retrieve available parking slots",
            description = "Retrieves a real-time list of all parking slots currently marked as AVAILABLE."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved available parking slots",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ParkingSlotResponseDto.class)))
    )
    public ResponseEntity<ApiResponseDto<List<ParkingSlotResponseDto>>> getAvailableSlots() {
        List<ParkingSlotResponseDto> availableSlots = slotService.getAvailableSlots();
        return ResponseEntity.ok(ApiResponseDto.success("Available slots retrieved successfully", availableSlots));
    }

    @GetMapping
    @Operation(
            summary = "List all parking slots",
            description = "Retrieves all parking slots regardless of current occupancy status."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved all slots",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ParkingSlotResponseDto.class)))
    )
    public ResponseEntity<ApiResponseDto<List<ParkingSlotResponseDto>>> getAllSlots() {
        List<ParkingSlotResponseDto> allSlots = slotService.getAllSlots();
        return ResponseEntity.ok(ApiResponseDto.success("All parking slots retrieved successfully", allSlots));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get parking slot by ID",
            description = "Retrieves details of a specific parking slot by its database ID."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved slot details",
            content = @Content(schema = @Schema(implementation = ParkingSlotResponseDto.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Parking slot not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    )
    public ResponseEntity<ApiResponseDto<ParkingSlotResponseDto>> getSlotById(@PathVariable Long id) {
        ParkingSlotResponseDto slot = slotService.getSlotById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Parking slot retrieved successfully", slot));
    }
}
