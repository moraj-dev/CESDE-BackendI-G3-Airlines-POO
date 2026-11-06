package com.airlinespoo.reservations.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.reservations.model.Reservation;

import java.util.List;

/** Repositorio en memoria de reservas (los detalles viajan dentro de la reserva). */
public class ReservationRepository extends InMemoryRepository<Reservation, Integer> {

    @Override
    protected String entityName() {
        return "reserva";
    }

    /** SELECT ... WHERE CustomerId = ? */
    public List<Reservation> findByCustomer(String customerId) {
        return values().stream()
            .filter(r -> r.getCustomer().getId().equals(customerId))
            .toList();
    }

    /** SELECT ... WHERE FlightId = ? */
    public List<Reservation> findByFlight(int flightId) {
        return values().stream()
            .filter(r -> r.getFlight().getId() == flightId)
            .toList();
    }

    /** SELECT 1 ... WHERE CustomerId = ? */
    public boolean existsByCustomer(String customerId) {
        return values().stream()
            .anyMatch(r -> r.getCustomer().getId().equals(customerId));
    }

    /** SELECT 1 ... WHERE FlightId = ? */
    public boolean existsByFlight(int flightId) {
        return values().stream()
            .anyMatch(r -> r.getFlight().getId() == flightId);
    }
}
