package com.example.ticket_booking_system.controller;

import com.example.ticket_booking_system.dto.BusRequest;
import com.example.ticket_booking_system.entity.Bus;
import com.example.ticket_booking_system.service.BusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
public class BusController {
    private final BusService busService;
    public BusController(BusService busService) {
        this.busService = busService;
    }
    @GetMapping("/api/buses")
    public List<Bus> getAllBuses()
    {
        return busService.getAllBuses();
    }
    @GetMapping("/api/buses/{id}")
    public Bus getBusById(@PathVariable Long id) {
        return busService.getBusById(id);
    }

    @PostMapping("/api/buses")
    @ResponseStatus(HttpStatus.CREATED)
    public Bus createBus(@Valid @RequestBody BusRequest busRequest) {
        return busService.createBus(busRequest);
    }

    @PutMapping("/api/buses/{id}")
    public Bus updateBus(
            @PathVariable Long id,
            @Valid @RequestBody BusRequest request) {
        return busService.updateBus(id, request);
    }

    @DeleteMapping("/api/buses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBus(@PathVariable Long id) {
        busService.deleteBus(id);
    }
}
