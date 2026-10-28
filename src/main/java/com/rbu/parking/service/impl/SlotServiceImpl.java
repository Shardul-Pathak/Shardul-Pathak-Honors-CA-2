package com.rbu.parking.service.impl;

import com.rbu.parking.dto.response.ParkingSlotResponseDto;
import com.rbu.parking.exception.ResourceNotFoundException;
import com.rbu.parking.model.ParkingSlot;
import com.rbu.parking.model.SlotStatus;
import com.rbu.parking.repository.ParkingSlotRepository;
import com.rbu.parking.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlotServiceImpl implements SlotService {

    private final ParkingSlotRepository slotRepository;

    @Override
    public List<ParkingSlotResponseDto> getAvailableSlots() {
        return slotRepository.findByStatus(SlotStatus.AVAILABLE).stream()
                .map(ParkingSlotResponseDto::fromEntity)
                .toList();
    }

    @Override
    public List<ParkingSlotResponseDto> getAllSlots() {
        return slotRepository.findAll().stream()
                .map(ParkingSlotResponseDto::fromEntity)
                .toList();
    }

    @Override
    public ParkingSlotResponseDto getSlotById(Long id) {
        ParkingSlot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with ID: " + id));
        return ParkingSlotResponseDto.fromEntity(slot);
    }
}
