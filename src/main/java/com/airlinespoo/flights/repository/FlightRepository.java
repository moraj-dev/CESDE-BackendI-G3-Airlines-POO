package com.airlinespoo.flights.repository;

import com.airlinespoo.airlines.model.Airlines;
import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.locations.model.Airport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio en memoria de vuelos. Solo guarda y busca; las reglas viven en el servicio.
 */
public class FlightRepository extends InMemoryRepository<Flight, Integer> {

    @Override
    protected String entityName() {
        return "vuelo";
    }

    /** SELECT ... WHERE FlightName = ? (sin distinguir mayúsculas). */
    public Optional<Flight> findByFlightName(String flightName) {
        String wanted = Validator.normalize(flightName);
        return values().stream()
            .filter(flight -> flight.getFlightName().equalsIgnoreCase(wanted))
            .findFirst();
    }

    /** SELECT 1 ... WHERE FlightName = ? */
    public boolean existsByFlightName(String flightName) {
        return findByFlightName(flightName).isPresent();
    }

    /** SELECT ... WHERE AirlineId = ? */
    public List<Flight> findByAirline(Airlines airline) {
        return values().stream()
            .filter(flight -> flight.getAirline().equals(airline))
            .toList();
    }

    /** SELECT ... WHERE DateDeparture BETWEEN ? AND ? (extremos incluidos). */
    public List<Flight> findByDepartureDateBetween(LocalDate from, LocalDate to) {
        return values().stream()
            .filter(flight -> {
                LocalDate date = flight.getSchedule().getDepartureDate();
                return !date.isBefore(from) && !date.isAfter(to);
            })
            .toList();
    }

    /** SELECT COUNT(*) ... WHERE AirlineId = ? */
    public long countByAirline(Airlines airline) {
        return findByAirline(airline).size();
    }

    /** SELECT 1 ... WHERE AirlineId = ? */
    public boolean existsByAirline(Airlines airline) {
        return countByAirline(airline) > 0;
    }

    /** SELECT 1 ... WHERE Departure = ? OR Arrival = ? */
    public boolean existsByAirport(Airport airport) {
        return values().stream().anyMatch(flight ->
            flight.getDeparture().equals(airport) || flight.getArrival().equals(airport));
    }
}
