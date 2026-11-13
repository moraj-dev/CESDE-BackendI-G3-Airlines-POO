package com.airlinespoo.flights.ui;

import com.airlinespoo.airlines.model.Airlines;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.flights.model.FlightReportRow;
import com.airlinespoo.flights.model.FlightSchedule;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.service.LocationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Menú de consola para programar y consultar vuelos.
 */
public class FlightMenu implements Menu {

    private static final int FIRST_OPTION = 1;
    private static final int LAST_OPTION = 9;
    private static final int CREATE_FLIGHT_OPTION = 1;
    private static final int FIND_FLIGHT_OPTION = 2;
    private static final int LIST_FLIGHTS_OPTION = 3;
    private static final int UPDATE_FLIGHT_OPTION = 4;
    private static final int CANCEL_FLIGHT_OPTION = 5;
    private static final int LIST_BY_AIRLINE_OPTION = 6;
    private static final int LIST_BY_DATE_RANGE_OPTION = 7;
    private static final int MONTHLY_REPORT_OPTION = 8;
    private static final int RETURN_OPTION = 9;
    private static final int INDEX_OFFSET = 1;
    private static final int MIN_REPORT_YEAR = 2000;
    private static final int MAX_REPORT_YEAR = 2100;
    private static final int MIN_MONTH = 1;
    private static final int MAX_MONTH = 12;

    private final FlightService flightService;
    private final LocationService locationService;
    private final AirlineService airlineService;
    private final ConsoleInput consoleInput;

    /**
     * Crea el menú de vuelos con los servicios y la entrada de consola requeridos.
     *
     * @param flightService servicio de vuelos
     * @param locationService servicio de aeropuertos
     * @param airlineService servicio de aerolíneas
     * @param consoleInput lector de consola compartido por la aplicación
     */
    public FlightMenu(FlightService flightService, LocationService locationService,
                      AirlineService airlineService, ConsoleInput consoleInput) {
        this.flightService = flightService;
        this.locationService = locationService;
        this.airlineService = airlineService;
        this.consoleInput = consoleInput;
    }

    /**
     * Devuelve el nombre de este menú.
     *
     * @return título del menú
     */
    @Override
    public String title() {
        return "Vuelos";
    }

    /**
     * Muestra las opciones y permanece en el menú hasta que se elija volver.
     */
    @Override
    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int option = consoleInput.readInt("Selecciona una opción", FIRST_OPTION, LAST_OPTION);
            if (option == RETURN_OPTION) {
                running = false;
            } else {
                runOption(option);
                consoleInput.pause();
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== Gestión de vuelos ===");
        System.out.println("%d. Programar vuelo".formatted(CREATE_FLIGHT_OPTION));
        System.out.println("%d. Consultar por código".formatted(FIND_FLIGHT_OPTION));
        System.out.println("%d. Listar todos".formatted(LIST_FLIGHTS_OPTION));
        System.out.println("%d. Reprogramar/actualizar".formatted(UPDATE_FLIGHT_OPTION));
        System.out.println("%d. Cancelar vuelo".formatted(CANCEL_FLIGHT_OPTION));
        System.out.println("%d. Listar por aerolínea".formatted(LIST_BY_AIRLINE_OPTION));
        System.out.println("%d. Listar por rango de fechas".formatted(LIST_BY_DATE_RANGE_OPTION));
        System.out.println("%d. Reporte mensual".formatted(MONTHLY_REPORT_OPTION));
        System.out.println("%d. Volver".formatted(RETURN_OPTION));
    }

    private void runOption(int option) {
        try {
            switch (option) {
                case CREATE_FLIGHT_OPTION -> createFlight();
                case FIND_FLIGHT_OPTION -> findFlight();
                case LIST_FLIGHTS_OPTION -> listFlights();
                case UPDATE_FLIGHT_OPTION -> updateFlight();
                case CANCEL_FLIGHT_OPTION -> cancelFlight();
                case LIST_BY_AIRLINE_OPTION -> listFlightsByAirline();
                case LIST_BY_DATE_RANGE_OPTION -> listFlightsBetweenDates();
                case MONTHLY_REPORT_OPTION -> printMonthlyReport();
                default -> throw new IllegalStateException("Opción de vuelo no reconocida.");
            }
        } catch (AirlinesException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void createFlight() {
        FlightData data = requestFlightData();
        Flight flight = flightService.createFlight(
            data.flightName(), data.departureId(), data.arrivalId(), data.airlineId(), data.schedule());
        System.out.println("Vuelo programado correctamente:");
        printFlight(flight);
    }

    private void findFlight() {
        String flightName = consoleInput.readRequiredText("Código del vuelo");
        printFlight(flightService.getFlightByName(flightName));
    }

    private void listFlights() {
        printFlights(flightService.listFlights(), "Sin vuelos registrados.");
    }

    private void updateFlight() {
        String flightName = consoleInput.readRequiredText("Código del vuelo que deseas actualizar");
        Flight existingFlight = flightService.getFlightByName(flightName);
        FlightData data = requestFlightData();
        Flight updatedFlight = flightService.updateFlight(
            existingFlight.getId(), data.flightName(), data.departureId(), data.arrivalId(),
            data.airlineId(), data.schedule());
        System.out.println("Vuelo actualizado correctamente:");
        printFlight(updatedFlight);
    }

    private void cancelFlight() {
        String flightName = consoleInput.readRequiredText("Código del vuelo que deseas cancelar");
        Flight flight = flightService.getFlightByName(flightName);
        if (consoleInput.confirm("¿Confirmas la cancelación del vuelo %s?".formatted(
                flight.getFlightName()))) {
            flightService.deleteFlight(flight.getId());
            System.out.println("Vuelo cancelado correctamente.");
        } else {
            System.out.println("Cancelación no realizada.");
        }
    }

    private void listFlightsByAirline() {
        int airlineId = requestAirlineId();
        printFlights(flightService.listFlightsByAirline(airlineId), "Sin vuelos para esa aerolínea.");
    }

    private void listFlightsBetweenDates() {
        LocalDate from = consoleInput.readDate("Fecha inicial");
        LocalDate to = consoleInput.readDate("Fecha final");
        printFlights(flightService.listFlightsBetween(from, to), "Sin vuelos en ese periodo.");
    }

    private void printMonthlyReport() {
        int year = consoleInput.readInt("Año", MIN_REPORT_YEAR, MAX_REPORT_YEAR);
        int month = consoleInput.readInt("Mes", MIN_MONTH, MAX_MONTH);
        Map<Airlines, List<FlightReportRow>> report =
            flightService.listFlightsByAirlineInMonth(year, month);
        if (report.isEmpty()) {
            System.out.println("Sin vuelos en ese periodo.");
            return;
        }
        printReportGroups(report);
    }

    private FlightData requestFlightData() {
        String flightName = consoleInput.readRequiredText("Código del vuelo");
        int airlineId = requestAirlineId();
        UUID departureId = requestAirportId("Aeropuerto de salida");
        UUID arrivalId = requestAirportId("Aeropuerto de llegada");
        FlightSchedule schedule = requestSchedule();
        return new FlightData(flightName, departureId, arrivalId, airlineId, schedule);
    }

    private int requestAirlineId() {
        List<Airlines> airlines = airlineService.findAll();
        if (airlines.isEmpty()) {
            throw new ValidationException("No hay aerolíneas registradas.");
        }
        System.out.println("Aerolíneas disponibles:");
        for (int index = 0; index < airlines.size(); index++) {
            System.out.println("%d. %s".formatted(index + INDEX_OFFSET, airlines.get(index).getName()));
        }
        int selectedIndex = consoleInput.readInt(
            "Selecciona la aerolínea", FIRST_OPTION, airlines.size()) - INDEX_OFFSET;
        return airlines.get(selectedIndex).getId();
    }

    private UUID requestAirportId(String prompt) {
        List<Airport> airports = locationService.listAirports();
        if (airports.isEmpty()) {
            throw new ValidationException("No hay aeropuertos registrados.");
        }
        System.out.println("Aeropuertos disponibles:");
        for (int index = 0; index < airports.size(); index++) {
            Airport airport = airports.get(index);
            System.out.println("%d. %s - %s".formatted(
                index + INDEX_OFFSET, airport.getName(), airport.getCity().getName()));
        }
        int selectedIndex = consoleInput.readInt(
            prompt, FIRST_OPTION, airports.size()) - INDEX_OFFSET;
        return airports.get(selectedIndex).getId();
    }

    private FlightSchedule requestSchedule() {
        LocalDate departureDate = consoleInput.readDate("Fecha de salida");
        LocalTime departureTime = consoleInput.readTime("Hora de salida");
        LocalDate arrivalDate = consoleInput.readDate("Fecha de llegada");
        LocalTime arrivalTime = consoleInput.readTime("Hora de llegada");
        return new FlightSchedule(departureDate, departureTime, arrivalDate, arrivalTime);
    }

    private void printFlights(List<Flight> flights, String emptyMessage) {
        if (flights.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (Flight flight : flights) {
            printFlight(flight);
        }
    }

    private void printFlight(Flight flight) {
        FlightSchedule schedule = flight.getSchedule();
        System.out.println("%s | %s (%s) → %s (%s) | %s → %s | Duración: %s".formatted(
            flight.getFlightName(),
            flight.getDeparture().getName(),
            flight.getDeparture().getCity().getName(),
            flight.getArrival().getName(),
            flight.getArrival().getCity().getName(),
            schedule.getDeparture(),
            schedule.getArrival(),
            schedule.formatDuration()));
        System.out.println("Aerolínea: %s".formatted(flight.getAirline().getName()));
    }

    private void printReportGroups(Map<Airlines, List<FlightReportRow>> report) {
        for (Map.Entry<Airlines, List<FlightReportRow>> group : report.entrySet()) {
            System.out.println("Aerolínea: %s".formatted(group.getKey().getName()));
            for (FlightReportRow row : group.getValue()) {
                FlightSchedule schedule = new FlightSchedule(
                    row.departureDate(), row.departureTime(), row.arrivalDate(), row.arrivalTime());
                System.out.println("%s | %s (%s) → %s (%s) | %s → %s | Duración: %s".formatted(
                    row.flightName(), row.departureAirport(), row.departureCity(),
                    row.arrivalAirport(), row.arrivalCity(),
                    schedule.getDeparture(), schedule.getArrival(), schedule.formatDuration()));
            }
        }
    }

    private record FlightData(String flightName, UUID departureId, UUID arrivalId,
                              int airlineId, FlightSchedule schedule) {
    }
}
