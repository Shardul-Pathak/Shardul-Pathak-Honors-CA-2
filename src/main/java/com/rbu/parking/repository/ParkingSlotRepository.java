package com.rbu.parking.repository;

import com.rbu.parking.model.ParkingSlot;
import com.rbu.parking.model.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    List<ParkingSlot> findByStatus(SlotStatus status);

    Optional<ParkingSlot> findFirstByStatusOrderByIdAsc(SlotStatus status);

    Optional<ParkingSlot> findBySlotNumber(String slotNumber);

    long countByStatus(SlotStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ParkingSlot s WHERE s.id = :id")
    Optional<ParkingSlot> findByIdWithLock(@Param("id") Long id);
}
