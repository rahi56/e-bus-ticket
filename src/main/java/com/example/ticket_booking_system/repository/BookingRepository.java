package com.example.ticket_booking_system.repository;

import com.example.ticket_booking_system.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking,Long> {


    boolean existsByTrip_IdAndSeatNumberAndStatus(
            Long tripId,
            String seatNumber,
            String status
    );

    boolean existsByTrip_IdAndSeatNumberAndStatusAndIdNot(
            Long tripId,
            String seatNumber,
            String status,
            Long id
    );

}
