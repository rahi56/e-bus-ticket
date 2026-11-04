
package com.example.ticket_booking_system.service;

import com.example.ticket_booking_system.entity.Booking;
import com.example.ticket_booking_system.entity.Passenger;
import com.example.ticket_booking_system.entity.Trip;
import com.example.ticket_booking_system.repository.BookingRepository;
import com.example.ticket_booking_system.repository.PassengerRepository;
import com.example.ticket_booking_system.repository.TripRepository;
import com.example.ticket_booking_system.dto.BookingRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void shouldRejectAlreadyBookedSeat() {

        // Arrange: prepare test data.
        BookingRequest request = new BookingRequest();
        request.setPassengerId(1L);
        request.setTripId(2L);
        request.setSeatNumber("A1");

        Passenger passenger = new Passenger();
        passenger.setId(1L);

        Trip trip = new Trip();
        trip.setId(2L);
        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));

        when(tripRepository.findByIdWithLock(2L))
                .thenReturn(Optional.of(trip));

        when(bookingRepository
                .existsByTrip_IdAndSeatNumberAndStatus(
                        2L, "A1", "CONFIRMED"))
                .thenReturn(true);

        // Act: attempt to book the already-confirmed seat.
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.createBooking(request)
        );

        // Assert: the service returns a conflict error.
        assertEquals(409, exception.getStatusCode().value());

        // Verify that no new booking was saved.
        verify(bookingRepository,
                org.mockito.Mockito.never())
                .save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldCreateBookingWhenSeatIsAvailable() {
        BookingRequest request = new BookingRequest();
        request.setPassengerId(1L);
        request.setTripId(2L);
        request.setSeatNumber("A1");

        Passenger passenger = new Passenger();
        passenger.setId(1L);

        Trip trip = new Trip();
        trip.setId(2L);

        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));

        when(tripRepository.findByIdWithLock(2L))
                .thenReturn(Optional.of(trip));

        when(bookingRepository.existsByTrip_IdAndSeatNumberAndStatus(
                2L, "A1", "CONFIRMED"))
                .thenReturn(false);

        Booking savedBooking = new Booking();
        savedBooking.setId(10L);
        savedBooking.setPassenger(passenger);
        savedBooking.setTrip(trip);
        savedBooking.setSeatNumber("A1");
        savedBooking.setStatus("CONFIRMED");

        when(bookingRepository.save(org.mockito.ArgumentMatchers.any(Booking.class)))
                .thenReturn(savedBooking);

        Booking result = bookingService.createBooking(request);

        assertEquals(10L, result.getId());
        assertEquals("A1", result.getSeatNumber());
        assertEquals("CONFIRMED", result.getStatus());

        verify(bookingRepository).save(org.mockito.ArgumentMatchers.any(Booking.class));
    }

    @Test
    void shouldCancelBookingSuccessfully() {
        // Arrange: create a passenger
        Passenger passenger = new Passenger();
        passenger.setId(1L);

        // Arrange: create a trip
        Trip trip = new Trip();
        trip.setId(2L);

        // Arrange: create a confirmed booking
        Booking booking = new Booking();
        booking.setId(10L);
        booking.setPassenger(passenger);
        booking.setTrip(trip);
        booking.setSeatNumber("A1");
        booking.setStatus("CONFIRMED");

        // Mock finding the booking
        when(bookingRepository.findById(10L))
                .thenReturn(Optional.of(booking));

        // Mock locking and finding the trip
        when(tripRepository.findByIdWithLock(2L))
                .thenReturn(Optional.of(trip));

        // Mock saving the updated booking
        when(bookingRepository.save(booking))
                .thenReturn(booking);

        // Act: cancel the booking
        Booking result = bookingService.cancelBooking(10L);

        // Assert: booking status must change
        assertEquals("CANCELLED", result.getStatus());

        // Verify: updated booking was saved
        verify(bookingRepository).save(booking);
    }

    @Test
    void shouldRejectAlreadyCancelledBooking() {
        Passenger passenger = new Passenger();
        passenger.setId(1L);

        Trip trip = new Trip();
        trip.setId(2L);

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setPassenger(passenger);
        booking.setTrip(trip);
        booking.setSeatNumber("A1");
        booking.setStatus("CANCELLED");

        when(bookingRepository.findById(10L))
                .thenReturn(Optional.of(booking));

        when(tripRepository.findByIdWithLock(2L))
                .thenReturn(Optional.of(trip));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.cancelBooking(10L)
        );

        assertEquals(400, exception.getStatusCode().value());

        verify(bookingRepository, org.mockito.Mockito.never())
                .save(org.mockito.ArgumentMatchers.any(Booking.class));
    }

    @Test
    void shouldThrow404WhenBookingDoesNotExist() {
        // Arrange: booking ID 999 does not exist
        when(bookingRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act + Assert: service should throw 404
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.getBookingById(999L)
        );

        assertEquals(404, exception.getStatusCode().value());
    }
}
