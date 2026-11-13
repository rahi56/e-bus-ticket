
package com.example.ticket_booking_system.service;

import com.example.ticket_booking_system.dto.BookingRequest;
import com.example.ticket_booking_system.entity.Booking;
import com.example.ticket_booking_system.entity.BookingStatus;
import com.example.ticket_booking_system.entity.Bus;
import com.example.ticket_booking_system.entity.Passenger;
import com.example.ticket_booking_system.entity.Trip;
import com.example.ticket_booking_system.exception.PassengerNotFoundException;
import com.example.ticket_booking_system.repository.BookingRepository;
import com.example.ticket_booking_system.repository.BusRepository;
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
    private final BusRepository busRepository;

    public BookingService(
            BookingRepository bookingRepository,
            PassengerRepository passengerRepository,
            TripRepository tripRepository,
            BusRepository busRepository) {

        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.tripRepository = tripRepository;
        this.busRepository = busRepository;
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

        Trip trip = tripRepository
                .findByIdWithLock(request.getTripId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trip not found with id: " + request.getTripId()
                ));

        Bus bus = busRepository.findById(trip.getBusId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bus not found with id: " + trip.getBusId()
                ));

        int seatNumber;

        try {
            seatNumber = Integer.parseInt(request.getSeatNumber());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Seat number must be a valid number"
            );
        }

        if (seatNumber < 1 || seatNumber > bus.getTotalSeats()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid seat number. Available seats are between 1 and "
                            + bus.getTotalSeats()
            );
        }

        String normalizedSeatNumber = String.valueOf(seatNumber);

        if (bookingRepository.existsByTrip_IdAndSeatNumberAndStatus(
                trip.getId(),
                normalizedSeatNumber,
                BookingStatus.CONFIRMED)) {

            log.warn("Booking rejected: seat {} is already booked for trip ID {}",
                    normalizedSeatNumber,
                    trip.getId());

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This seat is already booked for this trip"
            );
        }

        Booking booking = new Booking();
        booking.setPassenger(passenger);
        booking.setTrip(trip);
        booking.setSeatNumber(normalizedSeatNumber);
        booking.setStatus(BookingStatus.CONFIRMED);

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

        Booking booking = bookingRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found with id: " + id
                ));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot update a cancelled booking"
            );
        }

        // Identify the original and requested trips.
        Long originalTripId = booking.getTrip().getId();
        Long requestedTripId = request.getTripId();

        // Lock both trips in ascending ID order.
        Trip originalTrip;
        Trip requestedTrip;

        if (originalTripId.equals(requestedTripId)) {

            originalTrip = tripRepository.findByIdWithLock(originalTripId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Trip not found with id: " + originalTripId
                    ));

            requestedTrip = originalTrip;

        } else if (originalTripId < requestedTripId) {

            originalTrip = tripRepository.findByIdWithLock(originalTripId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Trip not found with id: " + originalTripId
                    ));

            requestedTrip = tripRepository.findByIdWithLock(requestedTripId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Trip not found with id: " + requestedTripId
                    ));

        } else {

            requestedTrip = tripRepository.findByIdWithLock(requestedTripId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Trip not found with id: " + requestedTripId
                    ));

            originalTrip = tripRepository.findByIdWithLock(originalTripId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Trip not found with id: " + originalTripId
                    ));
        }

        Passenger passenger = passengerRepository
                .findById(request.getPassengerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Passenger not found with id: "
                                + request.getPassengerId()
                ));

        // Get the bus for the requested trip.
        Bus bus = busRepository.findById(requestedTrip.getBusId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bus not found with id: " + requestedTrip.getBusId()
                ));

        // Validate the requested seat.
        int seatNumber;

        try {
            seatNumber = Integer.parseInt(request.getSeatNumber());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Seat number must be a valid number"
            );
        }

        if (seatNumber < 1 || seatNumber > bus.getTotalSeats()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid seat number. Available seats are between 1 and "
                            + bus.getTotalSeats()
            );
        }

        String normalizedSeatNumber = String.valueOf(seatNumber);

        // Check whether another confirmed booking owns this seat.
        if (bookingRepository.existsByTrip_IdAndSeatNumberAndStatusAndIdNot(
                requestedTrip.getId(),
                normalizedSeatNumber,
                BookingStatus.CONFIRMED,
                id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This seat is already booked for this trip"
            );
        }

        booking.setPassenger(passenger);
        booking.setTrip(requestedTrip);
        booking.setSeatNumber(normalizedSeatNumber);

        return bookingRepository.save(booking);
    }

    // CANCEL BOOKING
    @Transactional
    public Booking cancelBooking(Long id) {

        Booking booking = bookingRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found with id: " + id
                ));

        Long tripId = booking.getTrip().getId();

        tripRepository.findByIdWithLock(tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trip not found with id: " + tripId
                ));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            log.warn("Booking ID: {} is already cancelled", id);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Booking is already cancelled"
            );
        }

        log.info("Cancelling booking ID: {}", id);

        booking.setStatus(BookingStatus.CANCELLED);

        Booking cancelledBooking = bookingRepository.save(booking);

        log.info("Booking ID: {} cancelled successfully", id);

        return cancelledBooking;
    }
}
