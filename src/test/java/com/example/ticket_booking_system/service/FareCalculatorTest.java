package com.example.ticket_booking_system.service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FareCalculatorTest {
    @Test
    void shouldCalculateFareCorrectly() {
        FareCalculator calculator = new FareCalculator();
        double actualFare = calculator.calculateFare(500, 3);
        double expectedFare = 1500.0;
        assertEquals(expectedFare, actualFare);
    }
}
