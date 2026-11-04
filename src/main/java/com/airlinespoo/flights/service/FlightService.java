package com.airlinespoo.flights.service;

import com.airlinespoo.airlines.model.Airlines;
import com.airlinespoo.airlines.repository.AirlineRepository;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.flights.model.FlightReportRow;
import com.airlinespoo.flights.model.FlightSchedule;
import com.airlinespoo.flights.repository.FlightRepository;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.service.LocationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private static final int MIN_REPORT_YEAR = 2000;
    private static final int MAX_REPORT_YEAR = 2100;

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

    /** Lista los vuelos de una ruta (aeropuerto de salida y de llegada), ordenados por salida. */
    public List<Flight> listFlightsByRoute(UUID departureId, UUID arrivalId) {
        Airport departure = locationService.getAirport(departureId);
        Airport arrival = locationService.getAirport(arrivalId);
        return flightRepository.findByRoute(departure, arrival).stream().sorted().toList();
    }

    /**
     * Reporte mensual (antes uspVuelosPorAerolinea): los vuelos cuya salida cae en el mes,
     * agrupados por aerolínea. Las aerolíneas salen en orden alfabético y los vuelos de cada
     * una por fecha y hora de salida. Si no hay vuelos, devuelve un mapa vacío.
     *
     * @param year  año del reporte
     * @param month mes del reporte (1 a 12)
     * @return mapa aerolínea → filas del reporte, en el orden descrito
     * @throws ValidationException si el año o el mes están fuera de rango
     */
    public Map<Airlines, List<FlightReportRow>> listFlightsByAirlineInMonth(int year, int month) {
        Validator.requireInRange(year, MIN_REPORT_YEAR, MAX_REPORT_YEAR, "año");
        Validator.requireInRange(month, 1, 12, "mes");
        LocalDate first = LocalDate.of(year, month, 1);
        LocalDate last = first.withDayOfMonth(first.lengthOfMonth());

        List<Flight> flights = new ArrayList<>(flightRepository.findByDepartureDateBetween(first, last));
        flights.sort(Comparator
                .comparing((Flight flight) -> flight.getAirline().getName().toLowerCase())
                .thenComparing(Comparator.naturalOrder()));

        Map<Airlines, List<FlightReportRow>> report = new LinkedHashMap<>();
        for (Flight flight : flights) {
            report.computeIfAbsent(flight.getAirline(), airline -> new ArrayList<>())
                    .add(FlightReportRow.from(flight));
        }
        return report;
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
