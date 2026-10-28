package com.rbu.parking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rbu.parking.dto.request.BookingRequestDto;
import com.rbu.parking.dto.request.CheckoutRequestDto;
import com.rbu.parking.model.ParkingSlot;
import com.rbu.parking.model.PaymentStatus;
import com.rbu.parking.model.SlotStatus;
import com.rbu.parking.model.SlotType;
import com.rbu.parking.repository.ParkingSlotRepository;
import com.rbu.parking.repository.VehicleBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("End-to-End RESTful API & Business Logic Integration Tests")
class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private ParkingSlotRepository slotRepository;

    @Autowired
    private VehicleBookingRepository bookingRepository;

    private ParkingSlot testSlot;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        slotRepository.deleteAll();

        testSlot = slotRepository.save(ParkingSlot.builder()
                .slotNumber("T-101")
                .slotType(SlotType.REGULAR)
                .status(SlotStatus.AVAILABLE)
                .floor(1)
                .version(0L)
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/slots/available - Should return real-time available slots")
    void testGetAvailableSlots() throws Exception {
        mockMvc.perform(get("/api/v1/slots/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].slotNumber").value("T-101"))
                .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("POST /api/v1/bookings/entry - Should allocate available slot upon vehicle entry (201 Created)")
    void testVehicleEntrySuccess() throws Exception {
        BookingRequestDto request = BookingRequestDto.builder()
                .vehicleNumber("MH-12-DE-1234")
                .entryTime(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/v1/bookings/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vehicleNumber").value("MH-12-DE-1234"))
                .andExpect(jsonPath("$.data.slot.slotNumber").value("T-101"))
                .andExpect(jsonPath("$.data.paymentStatus").value("PENDING"));

        // Slot must now be OCCUPIED
        ParkingSlot updatedSlot = slotRepository.findById(testSlot.getId()).orElseThrow();
        assertEquals(SlotStatus.OCCUPIED, updatedSlot.getStatus());
    }

    @Test
    @DisplayName("POST /api/v1/bookings/entry - Should return 400 Bad Request on blank vehicle number")
    void testVehicleEntryValidationFailure() throws Exception {
        BookingRequestDto request = BookingRequestDto.builder()
                .vehicleNumber("")
                .build();

        mockMvc.perform(post("/api/v1/bookings/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.errors", not(empty())));
    }

    @Test
    @DisplayName("POST /api/v1/bookings/entry - Should return 409 Conflict if vehicle is already parked")
    void testVehicleAlreadyParkedConflict() throws Exception {
        BookingRequestDto request = BookingRequestDto.builder()
                .vehicleNumber("KA-01-MJ-5555")
                .entryTime(LocalDateTime.now())
                .build();

        // First entry
        mockMvc.perform(post("/api/v1/bookings/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Duplicate entry attempt
        mockMvc.perform(post("/api/v1/bookings/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("already actively parked")));
    }

    @Test
    @DisplayName("GET /api/v1/bookings/active - Should fetch all active records")
    void testGetActiveBookings() throws Exception {
        BookingRequestDto request = BookingRequestDto.builder()
                .vehicleNumber("DL-04-CA-9999")
                .entryTime(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/v1/bookings/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/bookings/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].vehicleNumber").value("DL-04-CA-9999"))
                .andExpect(jsonPath("$.data[0].paymentStatus").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/v1/bookings/{id}/fare - Should calculate real-time accrued fare accurately (₹100 for 3h 5m)")
    void testRealTimeFareCalculation() throws Exception {
        LocalDateTime entryTime = LocalDateTime.now().minusHours(4).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime evaluationTime = entryTime.plusHours(3).plusMinutes(5); // 3h 5m -> 2 blocks -> ₹100

        BookingRequestDto request = BookingRequestDto.builder()
                .vehicleNumber("MH-02-XY-8888")
                .entryTime(entryTime)
                .build();

        String responseJson = mockMvc.perform(post("/api/v1/bookings/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long bookingId = objectMapper.readTree(responseJson).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/bookings/" + bookingId + "/fare")
                        .param("asOf", evaluationTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.elapsedMinutes").value(185))
                .andExpect(jsonPath("$.data.billingBlocks").value(2))
                .andExpect(jsonPath("$.data.accruedFare").value(100.00))
                .andExpect(jsonPath("$.data.currency").value("INR"));
    }

    @Test
    @DisplayName("POST /api/v1/bookings/{id}/checkout - Should process exit, calculate bill, and free up slot")
    void testCheckoutAndBillingSuccess() throws Exception {
        LocalDateTime entryTime = LocalDateTime.now().minusHours(4).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime exitTime = entryTime.plusHours(2).plusMinutes(15); // 2h 15m -> 1 block -> ₹50

        BookingRequestDto bookingRequest = BookingRequestDto.builder()
                .vehicleNumber("UP-16-ZZ-1111")
                .entryTime(entryTime)
                .build();

        String entryResponse = mockMvc.perform(post("/api/v1/bookings/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long bookingId = objectMapper.readTree(entryResponse).path("data").path("id").asLong();

        CheckoutRequestDto checkoutRequest = CheckoutRequestDto.builder()
                .exitTime(exitTime)
                .build();

        mockMvc.perform(post("/api/v1/bookings/" + bookingId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.durationMinutes").value(135))
                .andExpect(jsonPath("$.data.totalFare").value(50.00));

        // Slot must now be freed back to AVAILABLE
        ParkingSlot freedSlot = slotRepository.findById(testSlot.getId()).orElseThrow();
        assertEquals(SlotStatus.AVAILABLE, freedSlot.getStatus());

        // Attempting double checkout must return 409 Conflict
        mockMvc.perform(post("/api/v1/bookings/" + bookingId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /api/v1/analytics/revenue - Should display aggregate revenue generated on a specified date")
    void testRevenueReporting() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDateTime entryTime = today.atTime(8, 0, 0);
        LocalDateTime exitTime = entryTime.plusHours(3).plusMinutes(10); // 3h 10m -> ₹100

        BookingRequestDto bookingRequest = BookingRequestDto.builder()
                .vehicleNumber("TS-09-AA-3333")
                .entryTime(entryTime)
                .build();

        String entryResponse = mockMvc.perform(post("/api/v1/bookings/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long bookingId = objectMapper.readTree(entryResponse).path("data").path("id").asLong();

        CheckoutRequestDto checkoutRequest = CheckoutRequestDto.builder()
                .exitTime(exitTime)
                .build();

        mockMvc.perform(post("/api/v1/bookings/" + bookingId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isOk());

        // Fetch revenue for date
        mockMvc.perform(get("/api/v1/analytics/revenue")
                        .param("date", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportDate").value(today.toString()))
                .andExpect(jsonPath("$.data.totalCompletedBookings").value(1))
                .andExpect(jsonPath("$.data.totalRevenue").value(100.00))
                .andExpect(jsonPath("$.data.currency").value("INR"));
    }
}
