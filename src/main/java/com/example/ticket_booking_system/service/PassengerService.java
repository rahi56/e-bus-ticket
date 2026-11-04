
        package com.example.ticket_booking_system.service;

import com.example.ticket_booking_system.dto.PassengerRequest;
import com.example.ticket_booking_system.entity.Passenger;
import com.example.ticket_booking_system.repository.PassengerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PassengerService {

    private final PassengerRepository passengerRepository;

    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    // Create passenger
    public Passenger createPassenger(PassengerRequest request) {
        Passenger passenger = new Passenger();

        passenger.setFirstName(request.getFirstName());
        passenger.setLastName(request.getLastName());
        passenger.setEmail(request.getEmail());
        passenger.setPhone(request.getPhone());

        return passengerRepository.save(passenger);
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
}
