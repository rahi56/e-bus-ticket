
package com.example.ticket_booking_system.controller;

import com.example.ticket_booking_system.dto.PassengerRequest;
import com.example.ticket_booking_system.entity.Passenger;
import com.example.ticket_booking_system.service.PassengerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    // Create passenger
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Passenger createPassenger(
            @Valid @RequestBody PassengerRequest request) {
        return passengerService.createPassenger(request);
    }

    // Get all passengers
    @GetMapping
    public List<Passenger> getAllPassengers() {
        return passengerService.getAllPassengers();
    }

    // Get passenger by ID
    @GetMapping("/{id}")
    public Passenger getPassengerById(@PathVariable Long id) {
        return passengerService.getPassengerById(id);
    }

    // Update passenger
    @PutMapping("/{id}")
    public Passenger updatePassenger(
            @PathVariable Long id,
            @Valid @RequestBody PassengerRequest request) {
        return passengerService.updatePassenger(id, request);
    }

    // Delete passenger
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePassenger(@PathVariable Long id) {
        passengerService.deletePassenger(id);
    }
}
