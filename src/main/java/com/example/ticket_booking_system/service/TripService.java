package com.example.ticket_booking_system.service;

import com.example.ticket_booking_system.dto.TripRequest;
import com.example.ticket_booking_system.entity.Trip;
import com.example.ticket_booking_system.repository.BusRepository;
import com.example.ticket_booking_system.repository.TripRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final BusRepository busRepository;
    public TripService(TripRepository tripRepository,
                       BusRepository busRepository) {
        this.tripRepository = tripRepository;
        this.busRepository = busRepository;
    }
    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }
    public Trip createTrip(TripRequest request) {

        if (!busRepository.existsById(request.getBusId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Bus not found with id: " + request.getBusId()
            );
        }

        Trip trip = new Trip();

        trip.setBusId(request.getBusId());
        trip.setOrigin(request.getOrigin());
        trip.setDestination(request.getDestination());

        return tripRepository.save(trip);
    }

    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trip not found with id: " + id
                ));
    }

    public Trip updateTrip(Long id, TripRequest request) {
        Trip trip = getTripById(id);

        if (!busRepository.existsById(request.getBusId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Bus not found with id: " + request.getBusId()
            );
        }

        trip.setBusId(request.getBusId());
        trip.setOrigin(request.getOrigin());
        trip.setDestination(request.getDestination());

        return tripRepository.save(trip);
    }

    public void deleteTrip(Long id) {
        Trip trip = getTripById(id);
        tripRepository.delete(trip);
    }
}