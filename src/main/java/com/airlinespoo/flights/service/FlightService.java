package com.airlinespoo.flights.service;

import com.airlinespoo.airlines.model.Airlines;
import com.airlinespoo.airlines.repository.AirlineRepository;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.flights.model.FlightSchedule;
import com.airlinespoo.flights.repository.FlightRepository;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.service.LocationService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Reglas de negocio de vuelos. No imprime ni lee de consola.
 *
 * <p>Las dependencias llegan por el constructor. Los aeropuertos se consultan a través de
 * {@link LocationService}. Las aerolíneas se consultan por ahora con {@link AirlineRepository},
 * porque el Módulo 3 aún no entrega su servicio; cuando exista, se reemplaza el método
 * privado {@code findAirline}.</p>
 */
public class FlightService {

    private final FlightRepository flightRepository;
    private final LocationService locationService;
    private final AirlineRepository airlineRepository;
    private final IdGenerator flightIds;

    public FlightService(FlightRepository flightRepository, LocationService locationService,
                         AirlineRepository airlineRepository, IdGenerator flightIds) {
        this.flightRepository = flightRepository;
        this.locationService = locationService;
        this.airlineRepository = airlineRepository;
        this.flightIds = flightIds;
    }

    /** Programa un vuelo nuevo (INSERT). */
    public Flight createFlight(String flightName, UUID departureId, UUID arrivalId,
                               int airlineId, FlightSchedule schedule) {
        requireUniqueName(flightName, null);
        Flight flight = new Flight(flightIds.peekNext(), flightName,
            locationService.getAirport(departureId), locationService.getAirport(arrivalId),
            findAirline(airlineId), schedule);
        flightRepository.create(flight);
        flightIds.next();
        return flight;
    }

    /** Consulta un vuelo por id; lanza "no encontrado" si no existe. */
    public Flight getFlight(int id) {
        return flightRepository.findById(id).orElseThrow(() -> new NotFoundException("vuelo", id));
    }

    /** Consulta un vuelo por código; lanza "no encontrado" si no existe. */
    public Flight getFlightByName(String flightName) {
        return flightRepository.findByFlightName(flightName)
            .orElseThrow(() -> new NotFoundException(
                "No existe un vuelo con el código '%s'.".formatted(flightName)));
    }

    /** Lista todos los vuelos ordenados por fecha y hora de salida. */
    public List<Flight> listFlights() {
        return flightRepository.findAll().stream().sorted().toList();
    }

    /**
     * Reprograma o cambia ruta/aerolínea (UPDATE). Crea un vuelo nuevo con el mismo id,
     * así todas las reglas del constructor se aplican y nada queda a medias.
     */
    public Flight updateFlight(int id, String flightName, UUID departureId, UUID arrivalId,
                               int airlineId, FlightSchedule schedule) {
        getFlight(id);
        requireUniqueName(flightName, id);
        Flight updated = new Flight(id, flightName,
            locationService.getAirport(departureId), locationService.getAirport(arrivalId),
            findAirline(airlineId), schedule);
        return flightRepository.update(updated);
    }

    /** Cancela un vuelo (DELETE). La regla "no eliminar con reservas" llega en la integración. */
    public void deleteFlight(int id) {
        getFlight(id);
        flightRepository.deleteById(id);
    }

    /** Lista los vuelos de una aerolínea ordenados por salida. */
    public List<Flight> listFlightsByAirline(int airlineId) {
        Airlines airline = findAirline(airlineId);
        return flightRepository.findByAirline(airline).stream().sorted().toList();
    }

    /** Lista los vuelos que salen entre dos fechas (inclusive), ordenados por salida. */
    public List<Flight> listFlightsBetween(LocalDate from, LocalDate to) {
        Validator.requireNotNull(from, "fecha inicial");
        Validator.requireNotNull(to, "fecha final");
        if (from.isAfter(to)) {
            throw new ValidationException("La fecha inicial no puede ser posterior a la final.");
        }
        return flightRepository.findByDepartureDateBetween(from, to).stream().sorted().toList();
    }

    public long countFlightsByAirline(int airlineId) {
        return flightRepository.countByAirline(findAirline(airlineId));
    }

    /** Lo usará Aerolíneas en la fase de integración. */
    public boolean airlineHasFlights(int airlineId) {
        return flightRepository.existsByAirline(findAirline(airlineId));
    }

    /** Lo usará Ubicaciones en la fase de integración. */
    public boolean airportHasFlights(UUID airportId) {
        Airport airport = locationService.getAirport(airportId);
        return flightRepository.existsByAirport(airport);
    }

    private Airlines findAirline(int airlineId) {
        return airlineRepository.findById(airlineId)
            .orElseThrow(() -> new NotFoundException("aerolínea", airlineId));
    }

    /** El código no puede repetirse, salvo en el propio vuelo que se actualiza. */
    private void requireUniqueName(String flightName, Integer ownId) {
        flightRepository.findByFlightName(flightName).ifPresent(existing -> {
            if (ownId == null || existing.getId() != ownId) {
                throw new DuplicateException(
                    "Ya existe un vuelo con el código '%s'.".formatted(existing.getFlightName()));
            }
        });
    }
}
