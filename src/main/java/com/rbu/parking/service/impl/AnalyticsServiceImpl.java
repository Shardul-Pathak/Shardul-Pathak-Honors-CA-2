package com.rbu.parking.service.impl;

import com.rbu.parking.dto.response.RevenueReportResponseDto;
import com.rbu.parking.model.PaymentStatus;
import com.rbu.parking.repository.VehicleBookingRepository;
import com.rbu.parking.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final VehicleBookingRepository bookingRepository;

    @Override
    public RevenueReportResponseDto getRevenueReport(LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        BigDecimal revenue = bookingRepository.calculateTotalRevenueForDateRange(
                startOfDay,
                endOfDay,
                PaymentStatus.COMPLETED
        );

        long count = bookingRepository.countCompletedBookingsForDateRange(
                startOfDay,
                endOfDay,
                PaymentStatus.COMPLETED
        );

        log.info("Aggregated revenue report for date {}: count={}, total=₹{}", targetDate, count, revenue);

        return RevenueReportResponseDto.builder()
                .reportDate(targetDate)
                .totalCompletedBookings(count)
                .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
                .currency("INR")
                .generatedAt(LocalDateTime.now())
                .build();
    }
}
