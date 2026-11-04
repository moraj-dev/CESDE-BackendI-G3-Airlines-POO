package com.airlinespoo.flights.model;

import com.airlinespoo.airlines.model.Airlines;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.Airport;

import java.util.Objects;

/**
 * Vuelo entre dos aeropuertos operado por una aerolínea.
 */
public class Flight implements Identifiable<Integer>, Comparable<Flight> {

    private static final int MAX_FLIGHT_NAME_LENGTH = 8;
    private static final String FLIGHT_NAME_PATTERN = "[A-Z0-9]+";

    private final int id;
    private String flightName;
    private Airport departure;
    private Airport arrival;
    private Airlines airline;
    private FlightSchedule schedule;

    /**
     * @throws ValidationException si algún dato no cumple las reglas del vuelo
     */
    public Flight(int id, String flightName, Airport departure, Airport arrival,
                  Airlines airline, FlightSchedule schedule) {
        this.id = (int) Validator.requirePositive(id, "id del vuelo");
        this.flightName = validFlightName(flightName);
        validateRoute(departure, arrival);
        this.departure = departure;
        this.arrival = arrival;
        this.airline = Validator.requireNotNull(airline, "aerolínea");
        this.schedule = Validator.requireNotNull(schedule, "horario");
    }

    private static String validFlightName(String value) {
        String name = Validator.requireText(value, "código del vuelo", MAX_FLIGHT_NAME_LENGTH)
            .toUpperCase();
        if (!name.matches(FLIGHT_NAME_PATTERN)) {
            throw new ValidationException(
                "El campo 'código del vuelo' solo admite letras y dígitos.");
        }
        return name;
    }

    private static void validateRoute(Airport departure, Airport arrival) {
        Validator.requireNotNull(departure, "aeropuerto de salida");
        Validator.requireNotNull(arrival, "aeropuerto de llegada");
        if (departure.equals(arrival)) {
            throw new ValidationException(
                "El aeropuerto de salida y el de llegada deben ser distintos.");
        }
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getFlightName() {
        return flightName;
    }

    public Airport getDeparture() {
        return departure;
    }

    public Airport getArrival() {
        return arrival;
    }

    public Airlines getAirline() {
        return airline;
    }

    public FlightSchedule getSchedule() {
        return schedule;
    }

    public void setFlightName(String flightName) {
        this.flightName = validFlightName(flightName);
    }

    public void setAirline(Airlines airline) {
        this.airline = Validator.requireNotNull(airline, "aerolínea");
    }

    /**
     * Cambia la ruta validando que los aeropuertos sean distintos.
     */
    public void changeRoute(Airport departure, Airport arrival) {
        validateRoute(departure, arrival);
        this.departure = departure;
        this.arrival = arrival;
    }

    /**
     * Reprograma el vuelo; el nuevo horario ya viene validado.
     */
    public void reschedule(FlightSchedule schedule) {
        this.schedule = Validator.requireNotNull(schedule, "horario");
    }

    @Override
    public int compareTo(Flight other) {
        int byDeparture = schedule.getDeparture().compareTo(other.schedule.getDeparture());
        return byDeparture != 0 ? byDeparture : Integer.compare(id, other.id);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Flight that && id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Vuelo %s [%s → %s] %s".formatted(
            flightName, departure.getName(), arrival.getName(), schedule);
    }
}
