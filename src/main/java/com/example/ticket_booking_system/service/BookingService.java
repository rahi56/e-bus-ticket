
package com.example.ticket_booking_system.service;

import com.example.ticket_booking_system.dto.BookingRequest;
import com.example.ticket_booking_system.entity.Booking;
import com.example.ticket_booking_system.entity.Passenger;
import com.example.ticket_booking_system.entity.Trip;
import com.example.ticket_booking_system.exception.PassengerNotFoundException;
import com.example.ticket_booking_system.repository.BookingRepository;
import com.example.ticket_booking_system.repository.PassengerRepository;
import com.example.ticket_booking_system.repository.TripRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BookingService {
    private static final Logger log =
            LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final TripRepository tripRepository;

    public BookingService(
            BookingRepository bookingRepository,
            PassengerRepository passengerRepository,
            TripRepository tripRepository) {

        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.tripRepository = tripRepository;
    }

    // CREATE BOOKING
    @Transactional
    public Booking createBooking(BookingRequest request) {

        log.info("Creating booking for passenger ID: {}, trip ID: {}, seat: {}",
                request.getPassengerId(),
                request.getTripId(),
                request.getSeatNumber());

        Passenger passenger = passengerRepository
                .findById(request.getPassengerId())
                .orElseThrow(() -> new PassengerNotFoundException(
                        "Passenger not found with id: " + request.getPassengerId()
                ));

        // Lock the trip row until this transaction completes.
        Trip trip = tripRepository
                .findByIdWithLock(request.getTripId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trip not found with id: " + request.getTripId()
                ));

        // Reject an already-confirmed seat.
        if (bookingRepository.existsByTrip_IdAndSeatNumberAndStatus(
                trip.getId(),
                request.getSeatNumber(),
                "CONFIRMED")) {
            log.warn("Booking rejected: seat {} is already booked for trip ID {}",
                    request.getSeatNumber(),
                    trip.getId());

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This seat is already booked for this trip"
            );
        }

        Booking booking = new Booking();
        booking.setPassenger(passenger);
        booking.setTrip(trip);
        booking.setSeatNumber(request.getSeatNumber());
        booking.setStatus("CONFIRMED");

        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking created successfully. Booking ID: {}, Seat: {}",
                savedBooking.getId(),
                savedBooking.getSeatNumber());

        return savedBooking;
    }

    // GET ALL BOOKINGS
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // GET BOOKING BY ID
    @Transactional(readOnly = true)
    public Booking getBookingById(Long id) {

        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found with id: " + id
                ));
    }

    // UPDATE BOOKING
    @Transactional
    public Booking updateBooking(Long id, BookingRequest request) {

        Booking booking = getBookingById(id);

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot update a cancelled booking"
            );
        }

        Passenger passenger = passengerRepository
                .findById(request.getPassengerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Passenger not found with id: "
                                + request.getPassengerId()
                ));

        // Lock the requested trip row.
        Trip trip = tripRepository
                .findByIdWithLock(request.getTripId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trip not found with id: " + request.getTripId()
                ));

        // Exclude this booking when checking for duplicate seats.
        if (bookingRepository
                .existsByTrip_IdAndSeatNumberAndStatusAndIdNot(
                        trip.getId(),
                        request.getSeatNumber(),
                        "CONFIRMED",
                        id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This seat is already booked for this trip"
            );
        }

        booking.setPassenger(passenger);
        booking.setTrip(trip);
        booking.setSeatNumber(request.getSeatNumber());

        return bookingRepository.save(booking);
    }

    // CANCEL BOOKING
    @Transactional
    public Booking cancelBooking(Long id) {

        Booking booking = getBookingById(id);

        Long tripId = booking.getTrip().getId();

        // Lock the trip row before changing booking status.
        tripRepository.findByIdWithLock(tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trip not found with id: " + tripId
                ));

        if ("CANCELLED".equals(booking.getStatus())) {
            log.warn("Booking ID: {} is already cancelled", id);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Booking is already cancelled"
            );
        }
        log.info("Cancelling booking ID: {}", id);

        booking.setStatus("CANCELLED");

        Booking cancelledBooking = bookingRepository.save(booking);

        log.info("Booking ID: {} cancelled successfully", id);

        return cancelledBooking;
    }
}
