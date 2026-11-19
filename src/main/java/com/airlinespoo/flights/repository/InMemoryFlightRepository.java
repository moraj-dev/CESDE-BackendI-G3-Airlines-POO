package com.airlinespoo.flights.repository;

import com.airlinespoo.airlines.model.Airlines;
import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.locations.model.Airport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementación EN MEMORIA del repositorio de vuelos. Guarda los vuelos en un mapa.
 * Hereda el CRUD del kit base y agrega las búsquedas propias de {@link FlightRepository}.
 *
 * <p>Asigna el id de cada vuelo nuevo con un {@link IdGenerator}, igual que la columna
 * IDENTITY de tblFlights lo hace en la base de datos.</p>
 */
public class InMemoryFlightRepository extends InMemoryRepository<Flight, Integer>
        implements FlightRepository {

    private final IdGenerator ids;

    /**
     * @param ids generador de ids de vuelos (en el proyecto: arranque 1, paso 1)
     */
    public InMemoryFlightRepository(IdGenerator ids) {
        this.ids = Validator.requireNotNull(ids, "generador de ids de vuelos");
    }

    @Override
    protected String entityName() {
        return "vuelo";
    }

    /**
     * Guarda un vuelo nuevo (INSERT) y le asigna el siguiente id disponible.
     *
     * @param flight vuelo sin guardar, con id {@link Flight#UNSAVED_ID}
     * @return un vuelo nuevo con los mismos datos y el id real; quien llama debe usar este
     */
    @Override
    public Flight create(Flight flight) {
        Validator.requireNotNull(flight, entityName());
        Flight saved = new Flight(ids.next(), flight.getFlightName(), flight.getDeparture(),
            flight.getArrival(), flight.getAirline(), flight.getSchedule());
        return super.create(saved);
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