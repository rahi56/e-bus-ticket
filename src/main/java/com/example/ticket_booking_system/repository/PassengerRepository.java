package com.example.ticket_booking_system.repository;

import com.example.ticket_booking_system.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerRepository
        extends JpaRepository<Passenger, Long> {
}
