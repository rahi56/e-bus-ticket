
package com.example.ticket_booking_system.service;
import com.example.ticket_booking_system.dto.BusRequest;
import com.example.ticket_booking_system.entity.Bus;
import com.example.ticket_booking_system.repository.BusRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BusService {

    private final BusRepository busRepository;

    public BusService(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

    public Bus saveBus(Bus bus) {
        return busRepository.save(bus);
    }

    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }
    public Bus getBusById(Long id) {
        return busRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Bus not found with id: " + id
                        ));
    }
    public Bus createBus(BusRequest request) {
        Bus bus = new Bus();
        bus.setBusNumber(request.getBusNumber());
        bus.setTotalSeats(request.getTotalSeats());

        return busRepository.save(bus);
    }
    public Bus updateBus(Long id, BusRequest request) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Bus not found with id: " + id
                        ));

        bus.setBusNumber(request.getBusNumber());
        bus.setTotalSeats(request.getTotalSeats());

        return busRepository.save(bus);
    }
    public void deleteBus(Long id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Bus not found with id: " + id
                        ));

        busRepository.delete(bus);
    }
}
