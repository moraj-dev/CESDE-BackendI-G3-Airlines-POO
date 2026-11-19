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
 * Implementación EN MEMORIA del repositorio de vuelos. Guarda los vuelos en un mapa.
 * Hereda el CRUD del kit base y agrega las búsquedas propias de {@link FlightRepository}.
 */
public class InMemoryFlightRepository extends InMemoryRepository<Flight, Integer>
        implements FlightRepository {

    @Override
    protected String entityName() {
        return "vuelo";
    }

    @Override
    public Optional<Flight> findByFlightName(String flightName) {
        String wanted = Validator.normalize(flightName);
        return values().stream()
            .filter(flight -> flight.getFlightName().equalsIgnoreCase(wanted))
            .findFirst();
    }

    @Override
    public boolean existsByFlightName(String flightName) {
        return findByFlightName(flightName).isPresent();
    }

    @Override
    public List<Flight> findByAirline(Airlines airline) {
        return values().stream()
            .filter(flight -> flight.getAirline().equals(airline))
            .toList();
    }

    @Override
    public List<Flight> findByDepartureDateBetween(LocalDate from, LocalDate to) {
        return values().stream()
            .filter(flight -> {
                LocalDate date = flight.getSchedule().getDepartureDate();
                return !date.isBefore(from) && !date.isAfter(to);
            })
            .toList();
    }

    @Override
    public List<Flight> findByRoute(Airport departure, Airport arrival) {
        return values().stream()
            .filter(flight -> flight.getDeparture().equals(departure)
                    && flight.getArrival().equals(arrival))
            .toList();
    }

    @Override
    public long countByAirline(Airlines airline) {
        return findByAirline(airline).size();
    }

    @Override
    public boolean existsByAirline(Airlines airline) {
        return countByAirline(airline) > 0;
    }

    @Override
    public boolean existsByAirport(Airport airport) {
        return values().stream().anyMatch(flight ->
            flight.getDeparture().equals(airport) || flight.getArrival().equals(airport));
    }
}