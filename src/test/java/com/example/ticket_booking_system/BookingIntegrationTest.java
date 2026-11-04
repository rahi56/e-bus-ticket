
package com.example.ticket_booking_system;

import com.example.ticket_booking_system.dto.BookingRequest;
import com.example.ticket_booking_system.entity.Booking;
import com.example.ticket_booking_system.entity.Bus;
import com.example.ticket_booking_system.entity.Passenger;
import com.example.ticket_booking_system.entity.Trip;
import com.example.ticket_booking_system.repository.BookingRepository;
import com.example.ticket_booking_system.repository.BusRepository;
import com.example.ticket_booking_system.repository.PassengerRepository;
import com.example.ticket_booking_system.repository.TripRepository;
import com.example.ticket_booking_system.service.BookingService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class BookingIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private BusRepository busRepository;

    @Test
    @Transactional
    void shouldCreateAndPersistBooking() {
        // 1. Create a bus
        Bus bus = new Bus();
        bus.setBusNumber("TEST-101");
        bus.setTotalSeats(40);
        bus = busRepository.save(bus);

        // 2. Create a passenger
        Passenger passenger = new Passenger();
        passenger.setFirstName("Test");
        passenger.setLastName("Passenger");
        passenger.setEmail("test@example.com");
        passenger.setPhone("01712345678");
        passenger = passengerRepository.save(passenger);

        // 3. Create a trip for the bus
        Trip trip = new Trip();
        trip.setBusId(bus.getId());
        trip.setOrigin("Dhaka");
        trip.setDestination("Tangail");
        trip = tripRepository.save(trip);

        // 4. Create a booking using the real service
        BookingRequest request = new BookingRequest();
        request.setPassengerId(passenger.getId());
        request.setTripId(trip.getId());
        request.setSeatNumber("A1");

        Booking created = bookingService.createBooking(request);

        // 5. Verify the booking
        assertNotNull(created.getId());
        assertEquals("A1", created.getSeatNumber());
        assertEquals("CONFIRMED", created.getStatus());

        // 6. Verify it exists in the database
        Booking saved = bookingRepository.findById(created.getId())
                .orElseThrow();

        assertEquals(passenger.getId(), saved.getPassenger().getId());
        assertEquals(trip.getId(), saved.getTrip().getId());
    }

    @Test
    @Transactional
    void shouldRejectDuplicateSeatBooking() {
        // Create a bus
        Bus bus = new Bus();
        bus.setBusNumber("TEST-102");
        bus.setTotalSeats(40);
        bus = busRepository.save(bus);

        // Create a passenger
        Passenger passenger = new Passenger();
        passenger.setFirstName("Test");
        passenger.setLastName("Passenger");
        passenger.setEmail("duplicate@example.com");
        passenger.setPhone("01712345679");
        passenger = passengerRepository.save(passenger);

        // Create a trip
        Trip trip = new Trip();
        trip.setBusId(bus.getId());
        trip.setOrigin("Dhaka");
        trip.setDestination("Tangail");
        trip = tripRepository.save(trip);

        // First booking
        BookingRequest request = new BookingRequest();
        request.setPassengerId(passenger.getId());
        request.setTripId(trip.getId());
        request.setSeatNumber("A1");

        bookingService.createBooking(request);

        // Second booking for the same seat and trip
        org.springframework.web.server.ResponseStatusException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> bookingService.createBooking(request)
                );

        assertEquals(
                org.springframework.http.HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        // Only one booking should exist for that trip and seat
        assertEquals(
                true,
                bookingRepository.existsByTrip_IdAndSeatNumberAndStatus(
                        trip.getId(),
                        "A1",
                        "CONFIRMED"
                )
        );
    }


    @Test
    @Transactional
    void shouldAllowSeatReuseAfterCancellation() {
        // Create a bus
        Bus bus = new Bus();
        bus.setBusNumber("TEST-103");
        bus.setTotalSeats(40);
        bus = busRepository.save(bus);

        // Create first passenger
        Passenger firstPassenger = new Passenger();
        firstPassenger.setFirstName("First");
        firstPassenger.setLastName("Passenger");
        firstPassenger.setEmail("first@example.com");
        firstPassenger.setPhone("01712345680");
        firstPassenger = passengerRepository.save(firstPassenger);

        // Create second passenger
        Passenger secondPassenger = new Passenger();
        secondPassenger.setFirstName("Second");
        secondPassenger.setLastName("Passenger");
        secondPassenger.setEmail("second@example.com");
        secondPassenger.setPhone("01712345681");
        secondPassenger = passengerRepository.save(secondPassenger);

        // Create trip
        Trip trip = new Trip();
        trip.setBusId(bus.getId());
        trip.setOrigin("Dhaka");
        trip.setDestination("Tangail");
        trip = tripRepository.save(trip);

        // First passenger books A1
        BookingRequest firstRequest = new BookingRequest();
        firstRequest.setPassengerId(firstPassenger.getId());
        firstRequest.setTripId(trip.getId());
        firstRequest.setSeatNumber("A1");

        Booking firstBooking = bookingService.createBooking(firstRequest);

        // Cancel the first booking
        Booking cancelled = bookingService.cancelBooking(firstBooking.getId());

        assertEquals("CANCELLED", cancelled.getStatus());

        // Second passenger books the same seat
        BookingRequest secondRequest = new BookingRequest();
        secondRequest.setPassengerId(secondPassenger.getId());
        secondRequest.setTripId(trip.getId());
        secondRequest.setSeatNumber("A1");

        Booking secondBooking = bookingService.createBooking(secondRequest);

        assertNotNull(secondBooking.getId());
        assertEquals("A1", secondBooking.getSeatNumber());
        assertEquals("CONFIRMED", secondBooking.getStatus());

        // The cancelled booking remains in the database
        assertEquals(
                "CANCELLED",
                bookingRepository.findById(firstBooking.getId())
                        .orElseThrow()
                        .getStatus()
        );
    }


}
