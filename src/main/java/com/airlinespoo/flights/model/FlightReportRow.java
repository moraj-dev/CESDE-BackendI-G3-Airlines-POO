package com.airlinespoo.flights.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Una fila del reporte "vuelos por aerolínea y mes" (lo que devolvía uspVuelosPorAerolinea).
 *
 * <p>Es un {@code record}: una clase que solo agrupa datos de lectura, sin lógica de negocio.
 * Java genera por sí solo el constructor, los métodos de lectura, equals, hashCode y toString.</p>
 *
 * @param airlineName      nombre de la aerolínea
 * @param flightName       código del vuelo
 * @param departureCity    ciudad de salida
 * @param departureAirport aeropuerto de salida
 * @param departureDate    fecha de salida
 * @param departureTime    hora de salida
 * @param arrivalCity      ciudad de llegada
 * @param arrivalAirport   aeropuerto de llegada
 * @param arrivalDate      fecha de llegada
 * @param arrivalTime      hora de llegada
 */
public record FlightReportRow(
    String airlineName,
    String flightName,
    String departureCity,
    String departureAirport,
    LocalDate departureDate,
    LocalTime departureTime,
    String arrivalCity,
    String arrivalAirport,
    LocalDate arrivalDate,
    LocalTime arrivalTime) {

    /**
     * Construye la fila a partir de un vuelo.
     *
     * @param flight vuelo del que se toman los datos
     * @return la fila del reporte
     */
    public static FlightReportRow from(Flight flight) {
        FlightSchedule schedule = flight.getSchedule();
        return new FlightReportRow(
            flight.getAirline().getName(),
            flight.getFlightName(),
            flight.getDeparture().getCity().getName(),
            flight.getDeparture().getName(),
            schedule.getDepartureDate(),
            schedule.getDepartureTime(),
            flight.getArrival().getCity().getName(),
            flight.getArrival().getName(),
            schedule.getArrivalDate(),
            schedule.getArrivalTime());
    }
}
