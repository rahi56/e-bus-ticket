
package com.example.ticket_booking_system.service;

import com.example.ticket_booking_system.dto.PassengerRequest;
import com.example.ticket_booking_system.dto.RegisterRequest;
import com.example.ticket_booking_system.entity.Passenger;
import com.example.ticket_booking_system.repository.PassengerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.ticket_booking_system.dto.LoginRequest;
import java.util.List;

@Service
public class PassengerService {

    private final PassengerRepository passengerRepository;
    private final PasswordEncoder passwordEncoder;

    public PassengerService(
            PassengerRepository passengerRepository,
            PasswordEncoder passwordEncoder) {

        this.passengerRepository = passengerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Get all passengers
    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll();
    }

    // Get passenger by ID
    public Passenger getPassengerById(Long id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Passenger not found with id: " + id
                ));
    }

    // Update passenger
    public Passenger updatePassenger(Long id, PassengerRequest request) {
        Passenger passenger = getPassengerById(id);

        passenger.setFirstName(request.getFirstName());
        passenger.setLastName(request.getLastName());
        passenger.setEmail(request.getEmail());
        passenger.setPhone(request.getPhone());

        return passengerRepository.save(passenger);
    }


    // Delete passenger
    public void deletePassenger(Long id) {
        Passenger passenger = getPassengerById(id);
        passengerRepository.delete(passenger);
    }
    public Passenger registerPassenger(RegisterRequest request) {

        if (passengerRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        Passenger passenger = new Passenger();

        passenger.setFirstName(request.getFirstName());
        passenger.setLastName(request.getLastName());
        passenger.setEmail(request.getEmail());
        passenger.setPhone(request.getPhone());

        passenger.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        return passengerRepository.save(passenger);

    }
    public Passenger authenticate(LoginRequest request) {

        Passenger passenger = passengerRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                passenger.getPassword()
        );

        if (!passwordMatches) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        return passenger;
    }
}
