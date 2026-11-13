
package com.example.ticket_booking_system.repository;

import com.example.ticket_booking_system.entity.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByTrip_IdAndSeatNumberAndStatus(
            Long tripId,
            String seatNumber,
            com.example.ticket_booking_system.entity.BookingStatus status
    );

    boolean existsByTrip_IdAndSeatNumberAndStatusAndIdNot(
            Long tripId,
            String seatNumber,
            com.example.ticket_booking_system.entity.BookingStatus status,
            Long id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithLock(@Param("id") Long id);
}
