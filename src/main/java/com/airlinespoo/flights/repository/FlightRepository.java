package com.airlinespoo.flights.repository;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.repository.Repository;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.locations.model.Airport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contrato del repositorio de vuelos: las operaciones comunes del kit base
 * más las búsquedas propias de este módulo sobre la tabla tblFlights.
 *
 * <p>Lo cumple {@link InMemoryFlightRepository} (memoria). Solo guarda y busca;
 * las reglas de negocio viven en el servicio.</p>
 */
public interface FlightRepository extends Repository<Flight, Integer> {

    /**
     * Busca un vuelo por su código, sin distinguir mayúsculas.
     * Equivale a: SELECT ... WHERE FlightName = ?
     *
     * @param flightName código del vuelo
     * @return el vuelo, o vacío si no existe
     */
    Optional<Flight> findByFlightName(String flightName);

    /**
     * Indica si ya existe un vuelo con ese código.
     * Equivale a: SELECT 1 ... WHERE FlightName = ?
     *
     * @param flightName código del vuelo
     * @return true si existe
     */
    boolean existsByFlightName(String flightName);

    /**
     * Lista los vuelos de una aerolínea.
     * Equivale a: SELECT ... WHERE AirlineId = ?
     *
     * @param airline aerolínea buscada
     * @return vuelos de esa aerolínea
     */
    List<Flight> findByAirline(Airline airline);

    /**
     * Lista los vuelos que salen entre dos fechas, con los extremos incluidos.
     * Equivale a: SELECT ... WHERE DateDeparture BETWEEN ? AND ?
     *
     * @param from fecha inicial
     * @param to   fecha final
     * @return vuelos que salen en ese rango
     */
    List<Flight> findByDepartureDateBetween(LocalDate from, LocalDate to);

    /**
     * Lista los vuelos de una ruta.
     * Equivale a: SELECT ... WHERE Departure = ? AND Arrival = ?
     *
     * @param departure aeropuerto de salida
     * @param arrival   aeropuerto de llegada
     * @return vuelos de esa ruta
     */
    List<Flight> findByRoute(Airport departure, Airport arrival);

    /**
     * Cuenta los vuelos de una aerolínea.
     * Equivale a: SELECT COUNT(*) ... WHERE AirlineId = ?
     *
     * @param airline aerolínea buscada
     * @return cantidad de vuelos
     */
    long countByAirline(Airline airline);

    /**
     * Indica si una aerolínea tiene al menos un vuelo.
     * Equivale a: SELECT 1 ... WHERE AirlineId = ?
     *
     * @param airline aerolínea a revisar
     * @return true si tiene vuelos
     */
    boolean existsByAirline(Airline airline);

    /**
     * Indica si un aeropuerto se usa como salida o llegada de algún vuelo.
     * Equivale a: SELECT 1 ... WHERE Departure = ? OR Arrival = ?
     *
     * @param airport aeropuerto a revisar
     * @return true si algún vuelo lo usa
     */
    boolean existsByAirport(Airport airport);
}