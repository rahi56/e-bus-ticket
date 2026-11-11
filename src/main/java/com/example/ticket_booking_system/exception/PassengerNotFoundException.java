package com.example.ticket_booking_system.exception;

public class PassengerNotFoundException extends RuntimeException {

    public PassengerNotFoundException(String message) {
        super(message);
    }
}