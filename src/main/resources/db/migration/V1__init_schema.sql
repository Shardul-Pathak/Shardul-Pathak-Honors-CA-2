-- V1__init_schema.sql
-- Create initial schema for Smart Parking Management System

CREATE TABLE IF NOT EXISTS parking_slots (
    id BIGSERIAL PRIMARY KEY,
    slot_number VARCHAR(50) NOT NULL UNIQUE,
    slot_type VARCHAR(30) NOT NULL DEFAULT 'REGULAR',
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    floor INT NOT NULL DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vehicle_bookings (
    id BIGSERIAL PRIMARY KEY,
    vehicle_number VARCHAR(30) NOT NULL,
    slot_id BIGINT NOT NULL REFERENCES parking_slots(id) ON DELETE RESTRICT,
    entry_time TIMESTAMP NOT NULL,
    exit_time TIMESTAMP,
    duration_minutes BIGINT,
    total_fare NUMERIC(10, 2),
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_parking_slots_status ON parking_slots(status);
CREATE INDEX IF NOT EXISTS idx_vehicle_bookings_vehicle_number ON vehicle_bookings(vehicle_number);
CREATE INDEX IF NOT EXISTS idx_vehicle_bookings_payment_status ON vehicle_bookings(payment_status);
CREATE INDEX IF NOT EXISTS idx_vehicle_bookings_slot_id ON vehicle_bookings(slot_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_bookings_exit_time ON vehicle_bookings(exit_time);
