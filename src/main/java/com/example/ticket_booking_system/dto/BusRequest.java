
package com.example.ticket_booking_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BusRequest {

    @NotBlank(message = "Bus number is required")
    private String busNumber;

    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "A bus must have at least one seat")
    private Integer totalSeats;

    public BusRequest() {
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }
}
