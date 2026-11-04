
package com.example.ticket_booking_system.controller;

import com.example.ticket_booking_system.dto.TripRequest;
import com.example.ticket_booking_system.entity.Booking;
import com.example.ticket_booking_system.entity.Trip;
import com.example.ticket_booking_system.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Trip createTrip(@RequestBody TripRequest request) {
        return tripService.createTrip(request);
    }
    @GetMapping
    public List<Trip> getAllTrips() {
        return tripService.getAllTrips();
    }
    @GetMapping("/{id}")
    public Trip getTripById(@PathVariable Long id) {
        return tripService.getTripById(id);
    }
    @PutMapping("/{id}")
    public Trip updateTrip(
            @PathVariable Long id,
            @RequestBody TripRequest request
    ) {
        return tripService.updateTrip(id, request);

    }


}
