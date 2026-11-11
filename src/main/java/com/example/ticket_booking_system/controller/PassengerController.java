
package com.example.ticket_booking_system.controller;

import com.example.ticket_booking_system.entity.Passenger;
import com.example.ticket_booking_system.service.PassengerService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
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
            @RequestBody com.example.ticket_booking_system.dto.PassengerRequest request) {

        return passengerService.updatePassenger(id, request);
    }

    // Delete passenger
    @DeleteMapping("/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void deletePassenger(@PathVariable Long id) {
        passengerService.deletePassenger(id);
    }
}
