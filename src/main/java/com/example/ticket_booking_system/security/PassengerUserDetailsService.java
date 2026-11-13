
package com.example.ticket_booking_system.security;

import com.example.ticket_booking_system.entity.Passenger;
import com.example.ticket_booking_system.repository.PassengerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PassengerUserDetailsService implements UserDetailsService {

    private final PassengerRepository passengerRepository;

    public PassengerUserDetailsService(
            PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Passenger passenger = passengerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Passenger not found: " + email
                ));

        return new PassengerUserDetails(passenger);
    }
}
