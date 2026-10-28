package com.rbu.parking.service.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Fee Calculation Engine Unit Tests")
class FeeCalculationEngineTest {

    private FeeCalculationEngine feeEngine;

    @BeforeEach
    void setUp() {
        feeEngine = new FeeCalculationEngine();
    }

    @Test
    @DisplayName("Should charge base ₹50 for 0 minutes (instantaneous entry/exit)")
    void testFeeForZeroMinutes() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 28, 10, 0, 0);
        BigDecimal fare = feeEngine.calculateFare(now, now);
        assertEquals(new BigDecimal("50.00"), fare);
    }

    @Test
    @DisplayName("Should charge ₹50 for 1 hour duration (under 3 hours)")
    void testFeeForOneHour() {
        LocalDateTime entry = LocalDateTime.of(2026, 10, 28, 10, 0, 0);
        LocalDateTime exit = entry.plusHours(1);
        BigDecimal fare = feeEngine.calculateFare(entry, exit);
        assertEquals(new BigDecimal("50.00"), fare);
    }

    @Test
    @DisplayName("Example Case 1: 2 hours 15 mins should cost ₹50")
    void testFeeForTwoHoursFifteenMinutes_ExampleCase() {
        LocalDateTime entry = LocalDateTime.of(2026, 10, 28, 10, 0, 0);
        LocalDateTime exit = entry.plusHours(2).plusMinutes(15);
        BigDecimal fare = feeEngine.calculateFare(entry, exit);
        assertEquals(new BigDecimal("50.00"), fare);
    }

    @Test
    @DisplayName("Exact boundary: Exactly 3 hours (180 mins) should cost ₹50")
    void testFeeForExactThreeHours() {
        LocalDateTime entry = LocalDateTime.of(2026, 10, 28, 10, 0, 0);
        LocalDateTime exit = entry.plusHours(3);
        BigDecimal fare = feeEngine.calculateFare(entry, exit);
        assertEquals(new BigDecimal("50.00"), fare);
    }

    @Test
    @DisplayName("Boundary rollover: 3 hours 1 minute should cost ₹100 (enters 2nd block)")
    void testFeeForThreeHoursOneMinute() {
        LocalDateTime entry = LocalDateTime.of(2026, 10, 28, 10, 0, 0);
        LocalDateTime exit = entry.plusHours(3).plusMinutes(1);
        BigDecimal fare = feeEngine.calculateFare(entry, exit);
        assertEquals(new BigDecimal("100.00"), fare);
    }

    @Test
    @DisplayName("Example Case 2: 3 hours 5 mins should cost ₹100")
    void testFeeForThreeHoursFiveMinutes_ExampleCase() {
        LocalDateTime entry = LocalDateTime.of(2026, 10, 28, 10, 0, 0);
        LocalDateTime exit = entry.plusHours(3).plusMinutes(5);
        BigDecimal fare = feeEngine.calculateFare(entry, exit);
        assertEquals(new BigDecimal("100.00"), fare);
    }

    @Test
    @DisplayName("Exact boundary: Exactly 6 hours (360 mins) should cost ₹100")
    void testFeeForExactSixHours() {
        LocalDateTime entry = LocalDateTime.of(2026, 10, 28, 10, 0, 0);
        LocalDateTime exit = entry.plusHours(6);
        BigDecimal fare = feeEngine.calculateFare(entry, exit);
        assertEquals(new BigDecimal("100.00"), fare);
    }

    @Test
    @DisplayName("Boundary rollover: 6 hours 1 minute should cost ₹150")
    void testFeeForSixHoursOneMinute() {
        LocalDateTime entry = LocalDateTime.of(2026, 10, 28, 10, 0, 0);
        LocalDateTime exit = entry.plusHours(6).plusMinutes(1);
        BigDecimal fare = feeEngine.calculateFare(entry, exit);
        assertEquals(new BigDecimal("150.00"), fare);
    }

    @Test
    @DisplayName("Full 24-hour day stay (8 blocks) should cost ₹400")
    void testFeeForFullDay() {
        LocalDateTime entry = LocalDateTime.of(2026, 10, 28, 8, 0, 0);
        LocalDateTime exit = entry.plusHours(24);
        BigDecimal fare = feeEngine.calculateFare(entry, exit);
        assertEquals(new BigDecimal("400.00"), fare);
    }

    @ParameterizedTest(name = "Duration {0} mins should yield fare ₹{1}")
    @CsvSource({
            "1, 50.00",
            "30, 50.00",
            "179, 50.00",
            "180, 50.00",
            "181, 100.00",
            "359, 100.00",
            "360, 100.00",
            "361, 150.00",
            "540, 150.00",
            "541, 200.00"
    })
    void testParameterizedDurationMinutes(long minutes, String expectedFare) {
        LocalDateTime entry = LocalDateTime.of(2026, 10, 28, 12, 0, 0);
        LocalDateTime exit = entry.plusMinutes(minutes);
        BigDecimal fare = feeEngine.calculateFare(entry, exit);
        assertEquals(new BigDecimal(expectedFare), fare);
    }

    @Test
    @DisplayName("Defensive check: Exit before entry should throw IllegalArgumentException")
    void testDefensive_ExitBeforeEntry_ThrowsException() {
        LocalDateTime entry = LocalDateTime.of(2026, 10, 28, 14, 0, 0);
        LocalDateTime exit = entry.minusMinutes(30);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> feeEngine.calculateFare(entry, exit)
        );
        assertTrue(ex.getMessage().contains("cannot be earlier than entry time"));
    }

    @Test
    @DisplayName("Defensive check: Null entryTime should throw IllegalArgumentException")
    void testDefensive_NullEntryTime_ThrowsException() {
        LocalDateTime exit = LocalDateTime.of(2026, 10, 28, 14, 0, 0);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> feeEngine.calculateFare(null, exit)
        );
        assertEquals("Entry time must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("Defensive check: Null exitTime should throw IllegalArgumentException")
    void testDefensive_NullExitTime_ThrowsException() {
        LocalDateTime entry = LocalDateTime.of(2026, 10, 28, 14, 0, 0);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> feeEngine.calculateFare(entry, null)
        );
        assertEquals("Exit time must not be null", ex.getMessage());
    }
}
