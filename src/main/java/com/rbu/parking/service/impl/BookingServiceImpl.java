package com.rbu.parking.service.impl;

import com.rbu.parking.dto.request.BookingRequestDto;
import com.rbu.parking.dto.request.CheckoutRequestDto;
import com.rbu.parking.dto.response.BookingResponseDto;
import com.rbu.parking.dto.response.FareCalculationResponseDto;
import com.rbu.parking.exception.*;
import com.rbu.parking.model.ParkingSlot;
import com.rbu.parking.model.PaymentStatus;
import com.rbu.parking.model.SlotStatus;
import com.rbu.parking.model.VehicleBooking;
import com.rbu.parking.repository.ParkingSlotRepository;
import com.rbu.parking.repository.VehicleBookingRepository;
import com.rbu.parking.service.BookingService;
import com.rbu.parking.service.engine.FeeCalculationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final ParkingSlotRepository slotRepository;
    private final VehicleBookingRepository bookingRepository;
    private final FeeCalculationEngine feeCalculationEngine;

    @Override
    @Transactional
    public BookingResponseDto bookSlot(BookingRequestDto request) {
        String normalizedVehicle = request.getVehicleNumber().trim().toUpperCase();

        // Defensive check: Verify vehicle is not already parked in an active session
        if (bookingRepository.existsByVehicleNumberAndPaymentStatus(normalizedVehicle, PaymentStatus.PENDING)) {
            log.warn("Vehicle {} already has an active parking session", normalizedVehicle);
            throw new VehicleAlreadyParkedException(
                    "Vehicle [" + normalizedVehicle + "] is already actively parked. Complete exit before creating a new booking."
            );
        }

        // Determine Slot: Requested slot or next available
        ParkingSlot slot;
        if (request.getSlotId() != null) {
            slot = slotRepository.findByIdWithLock(request.getSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with ID: " + request.getSlotId()));

            if (slot.getStatus() != SlotStatus.AVAILABLE) {
                log.warn("Requested slot {} is currently {}", slot.getSlotNumber(), slot.getStatus());
                throw new SlotUnavailableException(
                        "Slot [" + slot.getSlotNumber() + "] is currently " + slot.getStatus() + ". Please choose an available slot."
                );
            }
        } else {
            slot = slotRepository.findFirstByStatusOrderByIdAsc(SlotStatus.AVAILABLE)
                    .orElseThrow(() -> new SlotUnavailableException("No parking slots are currently available. The parking facility is full."));
        }

        // Determine and validate entry time
        LocalDateTime entryTime = request.getEntryTime() != null ? request.getEntryTime() : LocalDateTime.now();
        if (entryTime.isAfter(LocalDateTime.now())) {
            throw new InvalidBookingTimeException("Entry timestamp cannot be in the future: " + entryTime);
        }

        // Allocate slot
        slot.setStatus(SlotStatus.OCCUPIED);
        slotRepository.save(slot);

        // Create booking
        VehicleBooking booking = VehicleBooking.builder()
                .vehicleNumber(normalizedVehicle)
                .slot(slot)
                .entryTime(entryTime)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        VehicleBooking savedBooking = bookingRepository.save(booking);
        log.info("Allocated slot {} to vehicle {} (Booking ID: {})", slot.getSlotNumber(), normalizedVehicle, savedBooking.getId());

        return BookingResponseDto.fromEntity(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getActiveBookings() {
        return bookingRepository.findByPaymentStatus(PaymentStatus.PENDING).stream()
                .map(BookingResponseDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FareCalculationResponseDto calculateRealTimeFare(Long bookingId, LocalDateTime asOfTime) {
        VehicleBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking record not found with ID: " + bookingId));

        LocalDateTime evaluationTime = asOfTime != null ? asOfTime : LocalDateTime.now();

        if (evaluationTime.isBefore(booking.getEntryTime())) {
            throw new InvalidBookingTimeException(
                    String.format("Evaluation time (%s) cannot be earlier than booking entry time (%s)",
                            evaluationTime, booking.getEntryTime())
            );
        }

        long elapsedMinutes = feeCalculationEngine.calculateDurationMinutes(booking.getEntryTime(), evaluationTime);
        long blocks = feeCalculationEngine.calculateBillingBlocks(elapsedMinutes);
        BigDecimal accruedFare = feeCalculationEngine.calculateFare(booking.getEntryTime(), evaluationTime);

        return FareCalculationResponseDto.builder()
                .bookingId(booking.getId())
                .vehicleNumber(booking.getVehicleNumber())
                .slotNumber(booking.getSlot().getSlotNumber())
                .entryTime(booking.getEntryTime())
                .calculationTime(evaluationTime)
                .elapsedMinutes(elapsedMinutes)
                .billingBlocks(blocks)
                .accruedFare(accruedFare)
                .currency("INR")
                .pricingPolicy("Flat ₹50 per 3 hours (or fraction thereof)")
                .build();
    }

    @Override
    @Transactional
    public BookingResponseDto checkout(Long bookingId, CheckoutRequestDto request) {
        VehicleBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking record not found with ID: " + bookingId));

        // Defensive check: Ensure booking is still active
        if (booking.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new BookingAlreadyClosedException("Booking ID [" + bookingId + "] has already completed checkout and payment.");
        }

        LocalDateTime exitTime = (request != null && request.getExitTime() != null)
                ? request.getExitTime()
                : LocalDateTime.now();

        if (exitTime.isBefore(booking.getEntryTime())) {
            throw new InvalidBookingTimeException(
                    String.format("Exit timestamp (%s) cannot be before entry timestamp (%s)",
                            exitTime, booking.getEntryTime())
            );
        }

        // Calculate billable duration and final fare
        long durationMinutes = feeCalculationEngine.calculateDurationMinutes(booking.getEntryTime(), exitTime);
        BigDecimal totalFare = feeCalculationEngine.calculateFare(booking.getEntryTime(), exitTime);

        // Update booking
        booking.setExitTime(exitTime);
        booking.setDurationMinutes(durationMinutes);
        booking.setTotalFare(totalFare);
        booking.setPaymentStatus(PaymentStatus.COMPLETED);

        // Free up slot
        ParkingSlot slot = booking.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(slot);

        VehicleBooking completedBooking = bookingRepository.save(booking);
        log.info("Completed checkout for booking ID {}. Vehicle: {}, Duration: {} mins, Total Fare: ₹{}",
                bookingId, booking.getVehicleNumber(), durationMinutes, totalFare);

        return BookingResponseDto.fromEntity(completedBooking);
    }
}
