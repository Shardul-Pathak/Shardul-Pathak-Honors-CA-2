package com.rbu.parking.service.engine;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Defensive pricing engine implementing the rule:
 * Flat charge of ₹50 per 3 hours (or fraction thereof).
 *
 * Example:
 *  - 2 hours 15 mins = ₹50
 *  - 3 hours 5 mins = ₹100
 */
@Component
public class FeeCalculationEngine {

    public static final BigDecimal RATE_PER_BLOCK = new BigDecimal("50.00");
    public static final long BLOCK_DURATION_MINUTES = 180L; // 3 hours in minutes

    /**
     * Calculates the total parking fare between entryTime and exitTime.
     *
     * @param entryTime vehicle entry timestamp (must not be null)
     * @param exitTime  vehicle exit timestamp (must not be null and not before entryTime)
     * @return calculated fare in BigDecimal
     * @throws IllegalArgumentException if timestamps are null or exitTime is before entryTime
     */
    public BigDecimal calculateFare(LocalDateTime entryTime, LocalDateTime exitTime) {
        validateTimestamps(entryTime, exitTime);

        long totalMinutes = calculateDurationMinutes(entryTime, exitTime);
        long blocks = calculateBillingBlocks(totalMinutes);

        return RATE_PER_BLOCK.multiply(BigDecimal.valueOf(blocks));
    }

    /**
     * Calculates duration in total minutes between entry and exit.
     */
    public long calculateDurationMinutes(LocalDateTime entryTime, LocalDateTime exitTime) {
        validateTimestamps(entryTime, exitTime);
        return Duration.between(entryTime, exitTime).toMinutes();
    }

    /**
     * Calculates the number of 3-hour billing blocks (ceiling division).
     * Any fraction of a 3-hour window counts as a full 3-hour block (minimum 1 block).
     */
    public long calculateBillingBlocks(long durationMinutes) {
        if (durationMinutes <= 0) {
            // Instantaneous exit or 0 minutes still constitutes entry into the first block
            return 1L;
        }
        // Ceiling division: (durationMinutes + BLOCK_DURATION_MINUTES - 1) / BLOCK_DURATION_MINUTES
        return (durationMinutes + BLOCK_DURATION_MINUTES - 1) / BLOCK_DURATION_MINUTES;
    }

    private void validateTimestamps(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null) {
            throw new IllegalArgumentException("Entry time must not be null");
        }
        if (exitTime == null) {
            throw new IllegalArgumentException("Exit time must not be null");
        }
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException(
                    String.format("Exit time (%s) cannot be earlier than entry time (%s)", exitTime, entryTime)
            );
        }
    }
}
