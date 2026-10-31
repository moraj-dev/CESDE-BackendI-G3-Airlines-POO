package com.airlinespoo.reservations.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;

/** Un pasajero con su silla dentro de una reserva. Inmutable. */
public final class ReservationDetail {

    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_DOCUMENT_LENGTH = 20;

    private final int id;
    private final String passengerFullName;
    private final String passengerDocument;
    private final Seat seat;

    public ReservationDetail(int id, String passengerFullName, String passengerDocument, Seat seat) {
        Validator.requirePositive(id, "id del detalle");
        this.id = id;
        this.passengerFullName =
            Validator.requireText(passengerFullName, "nombre del pasajero", MAX_NAME_LENGTH);
        this.passengerDocument = normalizeDocument(passengerDocument);
        this.seat = Validator.requireNotNull(seat, "silla del pasajero");
    }

    /** Devuelve un detalle nuevo, mismo id y pasajero, con otra silla. */
    public ReservationDetail withSeat(Seat newSeat) {
        return new ReservationDetail(id, passengerFullName, passengerDocument, newSeat);
    }

    /** Devuelve un detalle nuevo, mismo id y silla, con otros datos de pasajero. */
    public ReservationDetail withPassenger(String newFullName, String newDocument) {
        return new ReservationDetail(id, newFullName, newDocument, seat);
    }

    /** Quita espacios, pasa a mayúsculas y exige solo letras y dígitos. */
    private static String normalizeDocument(String document) {
        String normalized = Validator.requireText(document, "documento del pasajero")
            .replace(" ", "").toUpperCase();
        Validator.requireMaxLength(normalized, MAX_DOCUMENT_LENGTH, "documento del pasajero");
        if (!normalized.matches("[A-Z0-9]+")) {
            throw new ValidationException(
                "El campo 'documento del pasajero' solo admite letras y dígitos.");
        }
        return normalized;
    }

    public int getId() {
        return id;
    }

    public String getPassengerFullName() {
        return passengerFullName;
    }

    public String getPassengerDocument() {
        return passengerDocument;
    }

    public Seat getSeat() {
        return seat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationDetail other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return passengerFullName + " (" + passengerDocument + ") - silla " + seat;
    }
}
