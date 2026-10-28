package com.rbu.parking.repository;

import com.rbu.parking.model.PaymentStatus;
import com.rbu.parking.model.VehicleBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleBookingRepository extends JpaRepository<VehicleBooking, Long> {

    List<VehicleBooking> findByPaymentStatus(PaymentStatus paymentStatus);

    boolean existsByVehicleNumberAndPaymentStatus(String vehicleNumber, PaymentStatus paymentStatus);

    Optional<VehicleBooking> findFirstByVehicleNumberAndPaymentStatusOrderByIdDesc(
            String vehicleNumber,
            PaymentStatus paymentStatus
    );

    List<VehicleBooking> findByExitTimeBetweenAndPaymentStatus(
            LocalDateTime start,
            LocalDateTime end,
            PaymentStatus paymentStatus
    );

    @Query("SELECT COALESCE(SUM(b.totalFare), 0.00) FROM VehicleBooking b " +
           "WHERE b.paymentStatus = :status " +
           "AND b.exitTime >= :startOfDay AND b.exitTime <= :endOfDay")
    BigDecimal calculateTotalRevenueForDateRange(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("status") PaymentStatus status
    );

    @Query("SELECT COUNT(b) FROM VehicleBooking b " +
           "WHERE b.paymentStatus = :status " +
           "AND b.exitTime >= :startOfDay AND b.exitTime <= :endOfDay")
    long countCompletedBookingsForDateRange(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("status") PaymentStatus status
    );
}
