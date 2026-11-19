package com.airlinespoo.flights.ui;

import com.airlinespoo.airlines.model.Airlines;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.flights.model.FlightSchedule;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.service.LocationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Carga vuelos de ejemplo usando aeropuertos y aerolíneas existentes.
 */
public class FlightSampleData {

    private static final int FIRST_POSITION = 0;
    private static final int SECOND_POSITION = 1;
    private static final int REQUIRED_AIRPORT_COUNT = 2;
    private static final int REQUIRED_AIRLINE_COUNT = 2;

    private static final LocalDate DECEMBER_5 = LocalDate.of(2026, 12, 5);
    private static final LocalDate DECEMBER_12 = LocalDate.of(2026, 12, 12);
    private static final LocalDate DECEMBER_19 = LocalDate.of(2026, 12, 19);
    private static final LocalDate DECEMBER_26 = LocalDate.of(2026, 12, 26);
    private static final LocalDate DECEMBER_27 = LocalDate.of(2026, 12, 27);
    private static final LocalDate JANUARY_3 = LocalDate.of(2027, 1, 3);
    private static final LocalDate JANUARY_10 = LocalDate.of(2027, 1, 10);
    private static final LocalDate JANUARY_17 = LocalDate.of(2027, 1, 17);
    private static final LocalDate JANUARY_24 = LocalDate.of(2027, 1, 24);

    private static final LocalTime MORNING_DEPARTURE = LocalTime.of(8, 0);
    private static final LocalTime LATE_MORNING_DEPARTURE = LocalTime.of(11, 15);
    private static final LocalTime AFTERNOON_DEPARTURE = LocalTime.of(15, 30);
    private static final LocalTime NIGHT_DEPARTURE = LocalTime.of(23, 30);
    private static final LocalTime EARLY_ARRIVAL = LocalTime.of(1, 10);
    private static final LocalTime MORNING_ARRIVAL = LocalTime.of(10, 0);
    private static final LocalTime AFTERNOON_ARRIVAL = LocalTime.of(17, 0);
    private static final LocalTime EVENING_ARRIVAL = LocalTime.of(21, 0);

    private final FlightService flightService;
    private final LocationService locationService;
    private final AirlineService airlineService;

    /**
     * Crea el cargador con los servicios necesarios para consultar y registrar vuelos.
     *
     * @param flightService servicio de vuelos
     * @param locationService servicio de ubicaciones
     * @param airlineService servicio de aerolíneas
     */
    public FlightSampleData(FlightService flightService, LocationService locationService,
                            AirlineService airlineService) {
        this.flightService = flightService;
        this.locationService = locationService;
        this.airlineService = airlineService;
    }

    /**
     * Registra ocho vuelos futuros repartidos entre diciembre de 2026 y enero de 2027.
     *
     * <p>Si ya hay vuelos guardados (por ejemplo, en la base de datos de una ejecución
     * anterior), no hace nada, para no intentar repetir los códigos de vuelo.</p>
     *
     * @throws BusinessRuleException si no hay suficientes aeropuertos o aerolíneas cargados
     */
    public void load() {
        if (!flightService.listFlights().isEmpty()) {
            return;
        }
        List<Airport> airports = locationService.listAirports();
        List<Airlines> airlines = airlineService.findAll();
        if (airports.size() < REQUIRED_AIRPORT_COUNT) {
            throw new BusinessRuleException("Se necesitan al menos dos aeropuertos cargados.");
        }
        if (airlines.size() < REQUIRED_AIRLINE_COUNT) {
            throw new BusinessRuleException("Se necesitan al menos dos aerolíneas cargadas.");
        }

        Airport firstAirport = airports.get(FIRST_POSITION);
        Airport secondAirport = airports.get(SECOND_POSITION);
        Airlines firstAirline = airlines.get(FIRST_POSITION);
        Airlines secondAirline = airlines.get(SECOND_POSITION);

        registerFlight("AF101", firstAirport, secondAirport, firstAirline,
            DECEMBER_5, MORNING_DEPARTURE, DECEMBER_5, MORNING_ARRIVAL);
        registerFlight("AF102", secondAirport, firstAirport, secondAirline,
            DECEMBER_12, LATE_MORNING_DEPARTURE, DECEMBER_12, AFTERNOON_ARRIVAL);
        registerFlight("AF103", firstAirport, secondAirport, firstAirline,
            DECEMBER_19, AFTERNOON_DEPARTURE, DECEMBER_19, EVENING_ARRIVAL);
        registerFlight("AF104", secondAirport, firstAirport, secondAirline,
            DECEMBER_26, NIGHT_DEPARTURE, DECEMBER_27, EARLY_ARRIVAL);
        registerFlight("AF201", firstAirport, secondAirport, firstAirline,
            JANUARY_3, MORNING_DEPARTURE, JANUARY_3, MORNING_ARRIVAL);
        registerFlight("AF202", secondAirport, firstAirport, secondAirline,
            JANUARY_10, LATE_MORNING_DEPARTURE, JANUARY_10, AFTERNOON_ARRIVAL);
        registerFlight("AF203", firstAirport, secondAirport, firstAirline,
            JANUARY_17, AFTERNOON_DEPARTURE, JANUARY_17, EVENING_ARRIVAL);
        registerFlight("AF204", secondAirport, firstAirport, secondAirline,
            JANUARY_24, MORNING_DEPARTURE, JANUARY_24, AFTERNOON_ARRIVAL);
    }

    private void registerFlight(String flightName, Airport departure, Airport arrival,
                                Airlines airline, LocalDate departureDate, LocalTime departureTime,
                                LocalDate arrivalDate, LocalTime arrivalTime) {
        FlightSchedule schedule = new FlightSchedule(
            departureDate, departureTime, arrivalDate, arrivalTime);
        flightService.createFlight(
            flightName, departure.getId(), arrival.getId(), airline.getId(), schedule);
    }
}
