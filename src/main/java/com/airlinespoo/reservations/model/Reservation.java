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

/** Reserva de un cliente para un vuelo. Posee sus detalles (composición). */
public class Reservation implements Identifiable<Integer> {

    public static final int MAX_PASSENGERS = 9;

    private final int id;
    private final Customer customer;
    private Flight flight;
    private final List<ReservationDetail> details = new ArrayList<>();

    /** Una reserva nace con su primer pasajero: nunca queda vacía. */
    public Reservation(int id, Customer customer, Flight flight, ReservationDetail firstPassenger) {
        Validator.requirePositive(id, "id de la reserva");
        this.id = id;
        this.customer = Validator.requireNotNull(customer, "cliente de la reserva");
        setFlight(flight);
        details.add(Validator.requireNotNull(firstPassenger, "primer pasajero"));
    }

    public void addPassenger(ReservationDetail detail) {
        Validator.requireNotNull(detail, "pasajero");
        if (details.size() >= MAX_PASSENGERS) {
            throw new ValidationException(
                "Una reserva no puede tener más de " + MAX_PASSENGERS + " pasajeros.");
        }
        checkConflicts(detail, null);
        details.add(detail);
    }

    public void removePassenger(int detailId) {
        int index = indexOf(detailId);
        if (details.size() == 1) {
            throw new ValidationException("No se puede quitar el único pasajero de la reserva.");
        }
        details.remove(index);
    }

    /** Reemplaza el detalle que tenga el mismo id (cambio de silla o de datos). */
    public void replaceDetail(ReservationDetail updated) {
        Validator.requireNotNull(updated, "pasajero");
        int index = indexOf(updated.getId());
        checkConflicts(updated, updated.getId());
        details.set(index, updated);
    }

    public Optional<ReservationDetail> findDetail(int detailId) {
        return details.stream().filter(d -> d.getId() == detailId).findFirst();
    }

    private int indexOf(int detailId) {
        for (int i = 0; i < details.size(); i++) {
            if (details.get(i).getId() == detailId) {
                return i;
            }
        }
        throw new ValidationException("El pasajero " + detailId + " no pertenece a esta reserva.");
    }

    /** Verifica documento y silla repetidos dentro de esta reserva, ignorando un detalle. */
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

    public void setFlight(Flight flight) {
        this.flight = Validator.requireNotNull(flight, "vuelo de la reserva");
    }

    @Override
    public Integer getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Flight getFlight() {
        return flight;
    }

    /** Copia de solo lectura: modificarla desde afuera no afecta la reserva. */
    public List<ReservationDetail> getDetails() {
        return List.copyOf(details);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reserva #" + id + " [cliente " + customer.getId()
            + ", vuelo " + flight.getFlightName()
            + ", pasajeros " + details.size() + "]";
    }
}
