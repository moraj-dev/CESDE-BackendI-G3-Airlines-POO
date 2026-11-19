package com.airlinespoo.reservations.repository;

import com.airlinespoo.core.repository.Repository;
import com.airlinespoo.reservations.model.Reservation;

import java.util.List;

/**
 * Contrato del repositorio de reservas: las operaciones comunes del kit base
 * más las búsquedas propias de este módulo.
 * Lo cumplen {@link InMemoryReservationRepository} (memoria) y
 * {@link JdbcReservationRepository} (SQL Server).
 */
public interface ReservationRepository extends Repository<Reservation, Integer> {

    /**
     * Lista las reservas de un cliente.
     * Equivale a: SELECT ... WHERE CustomerId = ?
     *
     * @param customerId documento del cliente
     * @return reservas de ese cliente
     */
    List<Reservation> findByCustomer(String customerId);

    /**
     * Lista las reservas de un vuelo.
     * Equivale a: SELECT ... WHERE FlightId = ?
     *
     * @param flightId id del vuelo
     * @return reservas de ese vuelo
     */
    List<Reservation> findByFlight(int flightId);

    /**
     * Indica si el cliente tiene al menos una reserva.
     * Equivale a: SELECT 1 ... WHERE CustomerId = ?
     *
     * @param customerId documento del cliente
     * @return true si tiene reservas
     */
    boolean existsByCustomer(String customerId);

    /**
     * Indica si el vuelo tiene al menos una reserva.
     * Equivale a: SELECT 1 ... WHERE FlightId = ?
     *
     * @param flightId id del vuelo
     * @return true si tiene reservas
     */
    boolean existsByFlight(int flightId);
}
