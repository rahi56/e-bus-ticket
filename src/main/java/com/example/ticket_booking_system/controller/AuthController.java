
package com.example.ticket_booking_system.controller;

import com.example.ticket_booking_system.dto.AuthResponse;
import com.example.ticket_booking_system.dto.LoginRequest;
import com.example.ticket_booking_system.dto.PassengerResponse;
import com.example.ticket_booking_system.dto.RegisterRequest;
import com.example.ticket_booking_system.entity.Passenger;
import com.example.ticket_booking_system.service.JwtService;
import com.example.ticket_booking_system.service.PassengerService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final PassengerService passengerService;
    private final JwtService jwtService;

    public AuthController(
            PassengerService passengerService,
            JwtService jwtService
    ) {
        this.passengerService = passengerService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<PassengerResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        Passenger passenger =
                passengerService.registerPassenger(request);

        PassengerResponse response = new PassengerResponse(
                passenger.getId(),
                passenger.getFirstName(),
                passenger.getLastName(),
                passenger.getEmail(),
                passenger.getPhone()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        // Verify passenger credentials
        Passenger passenger = passengerService.authenticate(request);

        // Generate JWT
        String token = jwtService.generateToken(passenger.getEmail());

        // Prepare response
        AuthResponse response = new AuthResponse(
                token,
                "Bearer"
        );

        return ResponseEntity.ok(response);
    }
}
