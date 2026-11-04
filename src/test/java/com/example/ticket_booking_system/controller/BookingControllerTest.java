package com.example.ticket_booking_system.controller;

import com.example.ticket_booking_system.entity.Booking;
import com.example.ticket_booking_system.entity.Passenger;
import com.example.ticket_booking_system.entity.Trip;
import com.example.ticket_booking_system.service.BookingService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    void shouldReturnBookingById() throws Exception {
        // Arrange: create passenger
        Passenger passenger = new Passenger();
        passenger.setId(1L);

        // Arrange: create trip
        Trip trip = new Trip();
        trip.setId(2L);

        // Arrange: create booking with its relationships
        Booking booking = new Booking();
        booking.setId(10L);
        booking.setPassenger(passenger);
        booking.setTrip(trip);
        booking.setSeatNumber("A1");
        booking.setStatus("CONFIRMED");

        when(bookingService.getBookingById(10L))
                .thenReturn(booking);

        // Act + Assert: simulate HTTP GET and check the response
        mockMvc.perform(get("/api/bookings/10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.passengerId").value(1))
                .andExpect(jsonPath("$.tripId").value(2))
                .andExpect(jsonPath("$.seatNumber").value("A1"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldReturn404WhenBookingDoesNotExist() throws Exception {
        when(bookingService.getBookingById(999L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found"
                ));

        mockMvc.perform(get("/api/bookings/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateBookingSuccessfully() throws Exception {
        Passenger passenger = new Passenger();
        passenger.setId(1L);

        Trip trip = new Trip();
        trip.setId(2L);

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setPassenger(passenger);
        booking.setTrip(trip);
        booking.setSeatNumber("A1");
        booking.setStatus("CONFIRMED");

        when(bookingService.createBooking(
                org.mockito.ArgumentMatchers.any(
                        com.example.ticket_booking_system.dto.BookingRequest.class
                )
        )).thenReturn(booking);

        String requestJson = """
                {
                    "passengerId": 1,
                    "tripId": 2,
                    "seatNumber": "A1"
                }
                """;

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.passengerId").value(1))
                .andExpect(jsonPath("$.tripId").value(2))
                .andExpect(jsonPath("$.seatNumber").value("A1"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldRejectBookingWithMissingPassengerId() throws Exception {
        String requestJson = """
                {
                    "tripId": 2,
                    "seatNumber": "A1"
                }
                """;

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCancelBookingSuccessfully() throws Exception {
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

        when(bookingService.cancelBooking(10L))
                .thenReturn(booking);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .patch("/api/bookings/10/cancel")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.seatNumber").value("A1"));
    }

    @Test
    void shouldUpdateBookingSuccessfully() throws Exception {
        Passenger passenger = new Passenger();
        passenger.setId(1L);

        Trip trip = new Trip();
        trip.setId(2L);

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setPassenger(passenger);
        booking.setTrip(trip);
        booking.setSeatNumber("B2");
        booking.setStatus("CONFIRMED");

        when(bookingService.updateBooking(
                org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.any(
                        com.example.ticket_booking_system.dto.BookingRequest.class
                )
        )).thenReturn(booking);

        String requestJson = """
            {
                "passengerId": 1,
                "tripId": 2,
                "seatNumber": "B2"
            }
            """;

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .put("/api/bookings/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.seatNumber").value("B2"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
    @Test
    void shouldReturnAllBookings() throws Exception {
        Passenger passenger = new Passenger();
        passenger.setId(1L);

        Trip trip = new Trip();
        trip.setId(2L);

        Booking booking1 = new Booking();
        booking1.setId(10L);
        booking1.setPassenger(passenger);
        booking1.setTrip(trip);
        booking1.setSeatNumber("A1");
        booking1.setStatus("CONFIRMED");

        Booking booking2 = new Booking();
        booking2.setId(11L);
        booking2.setPassenger(passenger);
        booking2.setTrip(trip);
        booking2.setSeatNumber("A2");
        booking2.setStatus("CONFIRMED");

        when(bookingService.getAllBookings())
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/bookings")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].seatNumber").value("A1"))
                .andExpect(jsonPath("$[1].id").value(11))
                .andExpect(jsonPath("$[1].seatNumber").value("A2"));
    }
}