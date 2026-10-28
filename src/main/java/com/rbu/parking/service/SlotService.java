package com.rbu.parking.service;

import com.rbu.parking.dto.response.ParkingSlotResponseDto;

import java.util.List;

public interface SlotService {
    List<ParkingSlotResponseDto> getAvailableSlots();
    List<ParkingSlotResponseDto> getAllSlots();
    ParkingSlotResponseDto getSlotById(Long id);
}
