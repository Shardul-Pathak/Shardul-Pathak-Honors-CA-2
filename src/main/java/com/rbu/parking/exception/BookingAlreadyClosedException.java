package com.rbu.parking.exception;

public class BookingAlreadyClosedException extends RuntimeException {
    public BookingAlreadyClosedException(String message) {
        super(message);
    }
}
