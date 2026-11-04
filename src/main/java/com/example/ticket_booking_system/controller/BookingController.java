
package com.example.ticket_booking_system.controller;

import com.example.ticket_booking_system.dto.BookingRequest;
import com.example.ticket_booking_system.dto.BookingResponse;
import com.example.ticket_booking_system.entity.Booking;
import com.example.ticket_booking_system.service.BookingService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // CREATE BOOKING
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
            @Valid @RequestBody BookingRequest request) {

        Booking booking = bookingService.createBooking(request);
        return BookingResponse.from(booking);
    }

    // GET ALL BOOKINGS
    @GetMapping
    public List<BookingResponse> getAllBookings() {

        return bookingService.getAllBookings()
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    // GET BOOKING BY ID
    @GetMapping("/{id}")
    public BookingResponse getBookingById(@PathVariable Long id) {

        Booking booking = bookingService.getBookingById(id);
        return BookingResponse.from(booking);
    }

    // UPDATE BOOKING
    @PutMapping("/{id}")
    public BookingResponse updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingRequest request) {

        Booking booking = bookingService.updateBooking(id, request);
        return BookingResponse.from(booking);
    }

    // CANCEL BOOKING
    @PatchMapping("/{id}/cancel")
    public BookingResponse cancelBooking(@PathVariable Long id) {

        Booking booking = bookingService.cancelBooking(id);
        return BookingResponse.from(booking);
    }
}
