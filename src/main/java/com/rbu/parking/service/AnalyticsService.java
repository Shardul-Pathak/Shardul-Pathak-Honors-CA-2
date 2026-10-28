package com.rbu.parking.service;

import com.rbu.parking.dto.response.RevenueReportResponseDto;

import java.time.LocalDate;

public interface AnalyticsService {
    RevenueReportResponseDto getRevenueReport(LocalDate date);
}
