
package com.example.ticket_booking_system;

import com.example.ticket_booking_system.entity.Bus;
import com.example.ticket_booking_system.entity.Passenger;
import com.example.ticket_booking_system.entity.Trip;
import com.example.ticket_booking_system.repository.BookingRepository;
import com.example.ticket_booking_system.repository.BusRepository;
import com.example.ticket_booking_system.repository.PassengerRepository;
import com.example.ticket_booking_system.repository.TripRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void shouldCreateBookingThroughApi() throws Exception {
        // 1. Create a bus
        Bus bus = new Bus();
        bus.setBusNumber("API-TEST-101");
        bus.setTotalSeats(40);
        bus = busRepository.save(bus);

        // 2. Create a passenger
        Passenger passenger = new Passenger();
        passenger.setFirstName("API");
        passenger.setLastName("Tester");
        passenger.setEmail("api.tester@example.com");
        passenger.setPhone("01712345682");
        passenger = passengerRepository.save(passenger);

        // 3. Create a trip
        Trip trip = new Trip();
        trip.setBusId(bus.getId());
        trip.setOrigin("Dhaka");
        trip.setDestination("Tangail");
        trip = tripRepository.save(trip);

        // 4. Send a real HTTP request through the controller
        String requestJson = """
                {
                    "passengerId": %d,
                    "tripId": %d,
                    "seatNumber": "C1"
                }
                """.formatted(passenger.getId(), trip.getId());

        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passengerId")
                        .value(passenger.getId()))
                .andExpect(jsonPath("$.tripId")
                        .value(trip.getId()))
                .andExpect(jsonPath("$.seatNumber").value("C1"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // 5. Verify that the booking was persisted
        assertTrue(
                bookingRepository.existsByTrip_IdAndSeatNumberAndStatus(
                        trip.getId(),
                        "C1",
                        "CONFIRMED"
                )
        );
    }

    @Test
    void shouldReturn409WhenSeatIsAlreadyBooked() throws Exception {
        // Create a bus
        Bus bus = new Bus();
        bus.setBusNumber("API-TEST-102");
        bus.setTotalSeats(40);
        bus = busRepository.save(bus);

        // Create a passenger
        Passenger passenger = new Passenger();
        passenger.setFirstName("Duplicate");
        passenger.setLastName("Tester");
        passenger.setEmail("duplicate.api@example.com");
        passenger.setPhone("01712345683");
        passenger = passengerRepository.save(passenger);

        // Create a trip
        Trip trip = new Trip();
        trip.setBusId(bus.getId());
        trip.setOrigin("Dhaka");
        trip.setDestination("Tangail");
        trip = tripRepository.save(trip);

        String requestJson = """
            {
                "passengerId": %d,
                "tripId": %d,
                "seatNumber": "C2"
            }
            """.formatted(passenger.getId(), trip.getId());

        // First request should succeed
        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isCreated());

        // Second request for the same seat should fail
        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isConflict());

        // The original confirmed booking should still exist
        org.junit.jupiter.api.Assertions.assertTrue(
                bookingRepository.existsByTrip_IdAndSeatNumberAndStatus(
                        trip.getId(),
                        "C2",
                        "CONFIRMED"
                )
        );
    }

    @Test
    void shouldCancelBookingThroughApi() throws Exception {
        // Create a bus
        Bus bus = new Bus();
        bus.setBusNumber("API-TEST-103");
        bus.setTotalSeats(40);
        bus = busRepository.save(bus);

        // Create a passenger
        Passenger passenger = new Passenger();
        passenger.setFirstName("Cancel");
        passenger.setLastName("Tester");
        passenger.setEmail("cancel.api@example.com");
        passenger.setPhone("01712345684");
        passenger = passengerRepository.save(passenger);

        // Create a trip
        Trip trip = new Trip();
        trip.setBusId(bus.getId());
        trip.setOrigin("Dhaka");
        trip.setDestination("Tangail");
        trip = tripRepository.save(trip);

        // Create a booking through the API
        String requestJson = """
            {
                "passengerId": %d,
                "tripId": %d,
                "seatNumber": "C3"
            }
            """.formatted(passenger.getId(), trip.getId());

        String response = mockMvc.perform(
                        post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the booking ID from the JSON response
        long bookingId = com.fasterxml.jackson.databind.json.JsonMapper
                .builder()
                .build()
                .readTree(response)
                .get("id")
                .asLong();

        // Cancel the booking
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .patch("/api/bookings/" + bookingId + "/cancel")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Verify the change was persisted
        org.junit.jupiter.api.Assertions.assertEquals(
                "CANCELLED",
                bookingRepository.findById(bookingId)
                        .orElseThrow()
                        .getStatus()
        );
    }

    @Test
    void shouldAllowSeatReuseAfterCancellationThroughApi() throws Exception {
        Bus bus = new Bus();
        bus.setBusNumber("API-TEST-104");
        bus.setTotalSeats(40);
        bus = busRepository.save(bus);

        Passenger first = new Passenger();
        first.setFirstName("First");
        first.setLastName("Passenger");
        first.setEmail("first.reuse@example.com");
        first.setPhone("01712345685");
        first = passengerRepository.save(first);

        Passenger second = new Passenger();
        second.setFirstName("Second");
        second.setLastName("Passenger");
        second.setEmail("second.reuse@example.com");
        second.setPhone("01712345686");
        second = passengerRepository.save(second);

        Trip trip = new Trip();
        trip.setBusId(bus.getId());
        trip.setOrigin("Dhaka");
        trip.setDestination("Tangail");
        trip = tripRepository.save(trip);

        // First passenger books seat C4
        String firstRequest = """
            {
                "passengerId": %d,
                "tripId": %d,
                "seatNumber": "C4"
            }
            """.formatted(first.getId(), trip.getId());

        String firstResponse = mockMvc.perform(
                        post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(firstRequest)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long firstBookingId = com.fasterxml.jackson.databind.json.JsonMapper
                .builder()
                .build()
                .readTree(firstResponse)
                .get("id")
                .asLong();

        // Cancel the first booking
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .patch("/api/bookings/" + firstBookingId + "/cancel")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Second passenger books the same seat
        String secondRequest = """
            {
                "passengerId": %d,
                "tripId": %d,
                "seatNumber": "C4"
            }
            """.formatted(second.getId(), trip.getId());

        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(secondRequest)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seatNumber").value("C4"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Verify both records retain their correct statuses
        org.junit.jupiter.api.Assertions.assertEquals(
                "CANCELLED",
                bookingRepository.findById(firstBookingId)
                        .orElseThrow()
                        .getStatus()
        );

        org.junit.jupiter.api.Assertions.assertTrue(
                bookingRepository.existsByTrip_IdAndSeatNumberAndStatus(
                        trip.getId(), "C4", "CONFIRMED"
                )
        );
    }


    @Test
    void shouldRejectBookingWithMissingPassengerId() throws Exception {
        String requestJson = """
            {
                "tripId": 1,
                "seatNumber": "D1"
            }
            """;

        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectBookingWithBlankSeatNumber() throws Exception {
        String requestJson = """
            {
                "passengerId": 1,
                "tripId": 1,
                "seatNumber": " "
            }
            """;

        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenPassengerDoesNotExist() throws Exception {
        Bus bus = new Bus();
        bus.setBusNumber("API-TEST-105");
        bus.setTotalSeats(40);
        bus = busRepository.save(bus);

        Trip trip = new Trip();
        trip.setBusId(bus.getId());
        trip.setOrigin("Dhaka");
        trip.setDestination("Tangail");
        trip = tripRepository.save(trip);

        String requestJson = """
            {
                "passengerId": 999999,
                "tripId": %d,
                "seatNumber": "D2"
            }
            """.formatted(trip.getId());

        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenTripDoesNotExist() throws Exception {
        Passenger passenger = new Passenger();
        passenger.setFirstName("Missing");
        passenger.setLastName("Trip");
        passenger.setEmail("missing.trip@example.com");
        passenger.setPhone("01712345687");
        passenger = passengerRepository.save(passenger);

        String requestJson = """
            {
                "passengerId": %d,
                "tripId": 999999,
                "seatNumber": "D3"
            }
            """.formatted(passenger.getId());

        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isNotFound());
    }

}
