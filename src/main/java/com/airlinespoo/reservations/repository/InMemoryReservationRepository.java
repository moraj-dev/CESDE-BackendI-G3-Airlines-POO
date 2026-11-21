
package com.airlinespoo.reservations.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.reservations.model.Reservation;

import java.util.List;

/**
 * Implementación en memoria del repositorio de reservas.
 *
 * Almacena las reservas durante la ejecución del programa.
 * Los detalles de los pasajeros se encuentran dentro de cada reserva.
 */
public class InMemoryReservationRepository extends InMemoryRepository<Reservation, Integer>
    implements ReservationRepository {

    /**
     * Retorna el nombre de la entidad administrada por el repositorio.
     *
     * @return nombre de la entidad: reserva.
     */
    @Override
    protected String entityName() {
        return "reserva";
    }

    /**
     * Busca todas las reservas asociadas a un cliente.
     *
     * @param customerId documento o identificador del cliente.
     * @return lista de reservas pertenecientes al cliente.
     */
    @Override
    public List<Reservation> findByCustomer(String customerId) {
        return values().stream()
            .filter(r -> r.getCustomer().getId().equals(customerId))
            .toList();
    }

    /**
     * Busca todas las reservas asociadas a un vuelo.
     *
     * @param flightId identificador del vuelo.
     * @return lista de reservas correspondientes al vuelo.
     */
    @Override
    public List<Reservation> findByFlight(int flightId) {
        return values().stream()
            .filter(r -> r.getFlight().getId() == flightId)
            .toList();
    }

    /**
     * Verifica si un cliente tiene al menos una reserva registrada.
     *
     * @param customerId documento o identificador del cliente.
     * @return true si existe una reserva para el cliente; false en caso contrario.
     */
    @Override
    public boolean existsByCustomer(String customerId) {
        return values().stream()
            .anyMatch(r -> r.getCustomer().getId().equals(customerId));
    }

    /**
     * Verifica si un vuelo tiene al menos una reserva registrada.
     *
     * @param flightId identificador del vuelo.
     * @return true si existe una reserva para el vuelo; false en caso contrario.
     */
    @Override
    public boolean existsByFlight(int flightId) {
        return values().stream()
            .anyMatch(r -> r.getFlight().getId() == flightId);
    }
}
