# Smart Parking Management System RESTful API

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Database](https://img.shields.io/badge/Database-PostgreSQL%2016-blue.svg)](https://www.postgresql.org/)
[![Documentation](https://img.shields.io/badge/Swagger%20UI-OpenAPI%203.0-purple.svg)](http://localhost:8080/swagger-ui/index.html)
[![Build & Tests](https://img.shields.io/badge/Tests-31%20Passed-success.svg)](#running-tests)

A production-grade RESTful backend service demonstrating **Clean Architecture**, **Defensive Programming**, **Jakarta Bean Validation**, and automated **Flyway Database Migrations**.

---

## Clean Architecture & Design Patterns

The codebase is organized into strict architectural boundaries:

```
src/main/java/com/rbu/parking
├── config/              # OpenAPI / Swagger 3.0 & Bean configurations
├── controller/          # RESTful Web Controllers (SlotController, BookingController, AnalyticsController)
├── dto/
│   ├── request/         # Request payloads validated with Jakarta Bean Validation (@Valid, @Pattern, etc.)
│   └── response/        # Decoupled API response transfer objects (ApiResponseDto, BookingResponseDto, etc.)
├── exception/           # Custom domain exceptions & GlobalExceptionHandler (@RestControllerAdvice)
├── model/               # JPA Entities with versioning & Enums (ParkingSlot, VehicleBooking, SlotStatus)
├── repository/          # Spring Data JPA Repositories with custom JPQL queries & locking
└── service/             # Business service interfaces and implementations
    ├── engine/          # FeeCalculationEngine (Defensive pricing engine for ₹50 / 3-hour blocks)
    └── impl/            # SlotServiceImpl, BookingServiceImpl, AnalyticsServiceImpl
```

---

## Key Architectural & Defensive Guarantees

1. **Defensive Concurrency & Locking:**
   - Optimistic concurrency control via `@Version` on `ParkingSlot` entity prevents double-booking race conditions when multiple vehicles request slots simultaneously.
   - Slot allocation is wrapped in transactional boundaries (`@Transactional`) ensuring atomic state updates.
2. **Duplicate Entry Defense:**
   - Active parking checks ensure a vehicle cannot check into multiple slots simultaneously (returns `409 Conflict`).
3. **Double-Checkout Prevention:**
   - Checked-out bookings cannot be checked out again (returns `409 Conflict`).
4. **Defensive Timestamp Validation:**
   - Exit times cannot precede entry times (returns `400 Bad Request`).
   - Timestamps in the future are rejected via `@PastOrPresent`.
5. **Jakarta Bean Validation:**
   - Strict pattern matching on vehicle license registration numbers (`^[A-Za-z0-9-]{4,15}$`), non-null constraints, and positive slot IDs.
6. **Standardized Error Handling:**
   - Centralized `@RestControllerAdvice` formats all errors uniformly (`timestamp`, `status`, `error`, `message`, `path`, `errors`).

---

## Pricing & Fare Calculation Engine

### Pricing Rule:
> **Flat charge of ₹50 per 3 hours (or fraction thereof).**

| Duration Elapsed | Billable 3-Hour Blocks | Calculated Fare |
| :--- | :--- | :--- |
| **0 minutes / instant** | 1 block | **₹50.00** |
| **1 hour 15 mins** | 1 block | **₹50.00** |
| **2 hours 15 mins** *(Prompt Example)* | 1 block | **₹50.00** |
| **3 hours 0 mins (exact boundary)** | 1 block | **₹50.00** |
| **3 hours 1 min** | 2 blocks | **₹100.00** |
| **3 hours 5 mins** *(Prompt Example)* | 2 blocks | **₹100.00** |
| **6 hours 0 mins (exact boundary)** | 2 blocks | **₹100.00** |
| **6 hours 1 min** | 3 blocks | **₹150.00** |
| **24 hours (full day)** | 8 blocks | **₹400.00** |

---

## RESTful API Endpoints

### 1. Slot Management
| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/slots/available` | Check and retrieve real-time available slots | `200 OK` |
| `GET` | `/api/v1/slots` | List all slots (available, occupied, maintenance) | `200 OK` |
| `GET` | `/api/v1/slots/{id}` | Get slot details by ID | `200 OK` / `404 Not Found` |

### 2. Booking & Billing Lifecycle
| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/bookings/entry` | Allocate slot upon vehicle entry (Auto or by `slotId`) | `201 Created` / `400` / `409` |
| `GET` | `/api/v1/bookings/active` | Fetch all currently active/occupied slots | `200 OK` |
| `GET` | `/api/v1/bookings/{bookingId}/fare` | Real-time fare calculation accrued at any given time | `200 OK` / `404` |
| `POST` | `/api/v1/bookings/{bookingId}/checkout` | Process vehicle exit, calculate bill, free up slot | `200 OK` / `400` / `409` |

### 3. Revenue & Analytics
| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/analytics/revenue?date=YYYY-MM-DD` | Aggregate revenue generated on specified date | `200 OK` |

---

## Database & PostgreSQL Integration

The service connects to PostgreSQL running in Docker (via container `prisma-postgres` on mapped host port `5433`):

* **Database Name:** `parking_db`
* **Username:** `postgres`
* **Password:** `postgres`
* **Port:** `5433`
* **Migration Tool:** Flyway (`src/main/resources/db/migration/`)
  - `V1__init_schema.sql`: Table DDL, relational foreign keys, performance indices.
  - `V2__seed_slots.sql`: Seeds 10 initial parking slots across 2 floors (`G-01` to `G-05`, `F2-01` to `F2-05`).

---

## Running Unit & Integration Tests

Execute the full suite of unit tests and integration tests via Maven wrapper:

```bash
./mvnw clean test
```

### Test Coverage Highlights:
- **`FeeCalculationEngineTest`**: 22 unit tests validating instantaneous exit, exact 3h/6h boundaries, rollover cases (e.g. 3h 5m -> ₹100, 2h 15m -> ₹50), multiple days, and defensive checks (null input, negative duration).
- **`BookingIntegrationTest`**: 9 end-to-end MockMvc integration tests testing full HTTP lifecycle, duplicate parking prevention, bean validation rejections, real-time fare calculation, checkout, and daily revenue reporting.

---

## Running the Application Locally

Ensure the Docker container is running:
```bash
docker start prisma-postgres
```

Start the Spring Boot application:
```bash
./mvnw spring-boot:run
```

Once running:
- **Interactive Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON Spec:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Postman Collection Verification

A pre-configured Postman collection is included in the project root:
- File: `parking-management-api.postman_collection.json`
- Import directly into Postman to test all endpoints, variable chaining (e.g. `activeBookingId`), and error handling scenarios.
