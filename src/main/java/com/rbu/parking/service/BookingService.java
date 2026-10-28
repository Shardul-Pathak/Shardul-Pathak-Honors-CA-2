package com.rbu.parking.service;

import com.rbu.parking.dto.request.BookingRequestDto;
import com.rbu.parking.dto.request.CheckoutRequestDto;
import com.rbu.parking.dto.response.BookingResponseDto;
import com.rbu.parking.dto.response.FareCalculationResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    BookingResponseDto bookSlot(BookingRequestDto request);

    List<BookingResponseDto> getActiveBookings();

    FareCalculationResponseDto calculateRealTimeFare(Long bookingId, LocalDateTime asOfTime);

    BookingResponseDto checkout(Long bookingId, CheckoutRequestDto request);
}
