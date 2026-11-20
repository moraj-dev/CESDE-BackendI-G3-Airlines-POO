
package com.airlinespoo.reservations.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.flights.model.Flight;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Representa una reserva realizada por un cliente para un vuelo.
 *
 * Una reserva contiene uno o varios pasajeros y sus respectivas sillas.
 * Mantiene la relación con el cliente y el vuelo mediante composición
 * con los detalles de los pasajeros.
 */
public class Reservation implements Identifiable<Integer> {

    /** Cantidad máxima de pasajeros permitida en una reserva. */
    public static final int MAX_PASSENGERS = 9;

    /** Identificador único de la reserva. */
    private final int id;

    /** Cliente responsable de realizar la reserva. */
    private final Customer customer;

    /** Vuelo asociado a la reserva. */
    private Flight flight;

    /** Lista de detalles correspondientes a los pasajeros de la reserva. */
    private final List<ReservationDetail> details = new ArrayList<>();

    /**
     * Construye una reserva con su identificador, cliente, vuelo
     * y primer pasajero.
     *
     * Una reserva siempre debe iniciar con al menos un pasajero.
     *
     * @param id identificador de la reserva.
     * @param customer cliente que realiza la reserva.
     * @param flight vuelo seleccionado.
     * @param firstPassenger primer detalle del pasajero.
     */
    public Reservation(int id, Customer customer, Flight flight, ReservationDetail firstPassenger) {
        Validator.requirePositive(id, "id de la reserva");
        this.id = id;
        this.customer = Validator.requireNotNull(customer, "cliente de la reserva");
        setFlight(flight);
        details.add(Validator.requireNotNull(firstPassenger, "primer pasajero"));
    }

    /**
     * Agrega un pasajero a la reserva.
     *
     * Comprueba que no se supere el límite de pasajeros y que no existan
     * documentos o sillas repetidas dentro de la misma reserva.
     *
     * @param detail detalle del pasajero que se desea agregar.
     */
    public void addPassenger(ReservationDetail detail) {
        Validator.requireNotNull(detail, "pasajero");
        if (details.size() >= MAX_PASSENGERS) {
            throw new ValidationException(
                "Una reserva no puede tener más de " + MAX_PASSENGERS + " pasajeros.");
        }
        checkConflicts(detail, null);
        details.add(detail);
    }

    /**
     * Elimina un pasajero de la reserva mediante el identificador de su detalle.
     *
     * No permite eliminar al único pasajero, porque toda reserva
     * debe conservar al menos uno.
     *
     * @param detailId identificador del detalle del pasajero que se eliminará.
     */
    public void removePassenger(int detailId) {
        int index = indexOf(detailId);
        if (details.size() == 1) {
            throw new ValidationException("No se puede quitar el único pasajero de la reserva.");
        }
        details.remove(index);
    }

    /**
     * Reemplaza los datos de un pasajero existente.
     *
     * Comprueba que el nuevo detalle no genere conflictos de documento
     * o silla con los demás pasajeros de la reserva.
     *
     * @param updated nuevo detalle que reemplazará al existente.
     */
    public void replaceDetail(ReservationDetail updated) {
        Validator.requireNotNull(updated, "pasajero");
        int index = indexOf(updated.getId());
        checkConflicts(updated, updated.getId());
        details.set(index, updated);
    }

    /**
     * Busca un pasajero dentro de la reserva mediante el identificador del detalle.
     *
     * @param detailId identificador del detalle que se desea encontrar.
     * @return Optional con el detalle encontrado, o vacío si no existe.
     */
    public Optional<ReservationDetail> findDetail(int detailId) {
        return details.stream().filter(d -> d.getId() == detailId).findFirst();
    }

    /**
     * Obtiene la posición de un pasajero dentro de la lista de detalles.
     *
     * @param detailId identificador del detalle que se desea localizar.
     * @return posición del detalle dentro de la lista.
     * @throws ValidationException si el pasajero no pertenece a la reserva.
     */
    private int indexOf(int detailId) {
        for (int i = 0; i < details.size(); i++) {
            if (details.get(i).getId() == detailId) {
                return i;
            }
        }
        throw new ValidationException("El pasajero " + detailId + " no pertenece a esta reserva.");
    }

    /**
     * Comprueba que el pasajero no tenga un documento ni una silla
     * repetidos dentro de la misma reserva.
     *
     * Permite ignorar un detalle específico cuando se está actualizando.
     *
     * @param candidate detalle que se desea agregar o establecer.
     * @param ignoredDetailId identificador del detalle que se debe ignorar,
     *                        o null si no se debe ignorar ninguno.
     */
    private void checkConflicts(ReservationDetail candidate, Integer ignoredDetailId) {
        for (ReservationDetail existing : details) {
            if (ignoredDetailId != null && existing.getId() == ignoredDetailId) {
                continue;
            }
            if (existing.getPassengerDocument().equals(candidate.getPassengerDocument())) {
                throw new ValidationException("El pasajero con documento "
                    + candidate.getPassengerDocument() + " ya está en la reserva.");
            }
            if (existing.getSeat().equals(candidate.getSeat())) {
                throw new ValidationException(
                    "La silla " + candidate.getSeat() + " ya está asignada en esta reserva.");
            }
        }
    }

    /**
     * Asigna el vuelo asociado a la reserva.
     *
     * @param flight vuelo que se asociará a la reserva.
     */
    public void setFlight(Flight flight) {
        this.flight = Validator.requireNotNull(flight, "vuelo de la reserva");
    }

    /**
     * Obtiene el identificador único de la reserva.
     *
     * @return identificador de la reserva.
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Obtiene el cliente responsable de la reserva.
     *
     * @return cliente asociado a la reserva.
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Obtiene el vuelo asociado a la reserva.
     *
     * @return vuelo de la reserva.
     */
    public Flight getFlight() {
        return flight;
    }

    /**
     * Obtiene los detalles de los pasajeros de la reserva.
     *
     * Retorna una copia de solo lectura para evitar modificaciones
     * externas sobre la lista interna.
     *
     * @return lista inmodificable de detalles de pasajeros.
     */
    public List<ReservationDetail> getDetails() {
        return List.copyOf(details);
    }

    /**
     * Compara dos reservas utilizando su identificador.
     *
     * @param o objeto que se desea comparar.
     * @return true si ambos objetos representan la misma reserva.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation other)) return false;
        return id == other.id;
    }

    /**
     * Genera el código hash de la reserva a partir de su identificador.
     *
     * @return código hash de la reserva.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Representa la reserva mostrando su identificador, cliente,
     * vuelo y cantidad de pasajeros.
     *
     * @return descripción de la reserva.
     */
    @Override
    public String toString() {
        return "Reserva #" + id + " [cliente " + customer.getId()
            + ", vuelo " + flight.getFlightName()
            + ", pasajeros " + details.size() + "]";
    }
}
