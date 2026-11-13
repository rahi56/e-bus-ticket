
package com.example.ticket_booking_system.dto;

import com.example.ticket_booking_system.entity.Booking;

public class BookingResponse {

    private Long id;
    private Long passengerId;
    private Long tripId;
    private String seatNumber;
    private String status;

    public BookingResponse(
            Long id,
            Long passengerId,
            Long tripId,
            String seatNumber,
            String status) {

        this.id = id;
        this.passengerId = passengerId;
        this.tripId = tripId;
        this.seatNumber = seatNumber;
        this.status = status;
    }

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getPassenger().getId(),
                booking.getTrip().getId(),
                booking.getSeatNumber(),
                booking.getStatus().name()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public Long getTripId() {
        return tripId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public String getStatus() {
        return status;
    }
}
