package com.example.ticket_booking_system.service;


public class FareCalculator {

    public double calculateFare(double baseFare, int passengers) {
        return baseFare * passengers;
    }

}