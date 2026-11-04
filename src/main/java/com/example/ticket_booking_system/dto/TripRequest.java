package com.example.ticket_booking_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TripRequest {
    @NotNull(message = "Bus ID is required")
    @Positive(message = "Bus ID must be positive ")
    private Long busId;
    @NotBlank(message = "Origin required")
    private String origin;

    public Long getBusId() {
        return busId;
    }

    public void setBusId(Long busId) {
        this.busId = busId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
    @NotBlank(message = "Destination is required")
    private String destination;
   public TripRequest()
   {

   }
}
