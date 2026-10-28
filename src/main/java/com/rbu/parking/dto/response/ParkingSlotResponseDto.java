package com.rbu.parking.dto.response;

import com.rbu.parking.model.ParkingSlot;
import com.rbu.parking.model.SlotStatus;
import com.rbu.parking.model.SlotType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Parking slot response payload")
public class ParkingSlotResponseDto {

    @Schema(description = "Slot ID", example = "1")
    private Long id;

    @Schema(description = "Slot unique code", example = "G-01")
    private String slotNumber;

    @Schema(description = "Slot type", example = "REGULAR")
    private SlotType slotType;

    @Schema(description = "Current slot status", example = "AVAILABLE")
    private SlotStatus status;

    @Schema(description = "Floor number", example = "1")
    private Integer floor;

    @Schema(description = "Last update timestamp", example = "2026-10-28T10:00:00")
    private LocalDateTime updatedAt;

    public static ParkingSlotResponseDto fromEntity(ParkingSlot slot) {
        if (slot == null) return null;
        return ParkingSlotResponseDto.builder()
                .id(slot.getId())
                .slotNumber(slot.getSlotNumber())
                .slotType(slot.getSlotType())
                .status(slot.getStatus())
                .floor(slot.getFloor())
                .updatedAt(slot.getUpdatedAt())
                .build();
    }
}
