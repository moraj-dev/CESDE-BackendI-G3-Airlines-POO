
package com.airlinespoo.reservations.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;

/**
 * Representa los datos de un pasajero y la silla asignada dentro de una reserva.
 *
 * Es una clase inmutable: una vez creado el detalle, sus atributos no pueden
 * modificarse directamente. Para actualizar sus datos se generan nuevos objetos.
 */
public final class ReservationDetail {

    /** Longitud máxima permitida para el nombre completo del pasajero. */
    public static final int MAX_NAME_LENGTH = 50;

    /** Longitud máxima permitida para el documento de identidad. */
    public static final int MAX_DOCUMENT_LENGTH = 20;

    /** Identificador único del detalle de reserva. */
    private final int id;

    /** Nombre completo del pasajero. */
    private final String passengerFullName;

    /** Documento de identidad normalizado del pasajero. */
    private final String passengerDocument;

    /** Silla asignada al pasajero dentro del vuelo. */
    private final Seat seat;

    /**
     * Construye un detalle de reserva con los datos del pasajero y su silla.
     *
     * Valida el identificador, el nombre, el documento y la silla recibida.
     *
     * @param id identificador del detalle.
     * @param passengerFullName nombre completo del pasajero.
     * @param passengerDocument documento de identidad del pasajero.
     * @param seat silla asignada al pasajero.
     */
    public ReservationDetail(int id, String passengerFullName, String passengerDocument, Seat seat) {
        Validator.requirePositive(id, "id del detalle");
        this.id = id;
        this.passengerFullName =
            Validator.requireText(passengerFullName, "nombre del pasajero", MAX_NAME_LENGTH);
        this.passengerDocument = normalizeDocument(passengerDocument);
        this.seat = Validator.requireNotNull(seat, "silla del pasajero");
    }

    /**
     * Crea un nuevo detalle conservando el identificador y los datos del pasajero,
     * pero asignándole una silla diferente.
     *
     * @param newSeat nueva silla del pasajero.
     * @return nuevo detalle de reserva con la silla actualizada.
     */
    public ReservationDetail withSeat(Seat newSeat) {
        return new ReservationDetail(id, passengerFullName, passengerDocument, newSeat);
    }

    /**
     * Crea un nuevo detalle conservando el identificador y la silla,
     * pero actualizando el nombre y el documento del pasajero.
     *
     * @param newFullName nuevo nombre completo del pasajero.
     * @param newDocument nuevo documento de identidad del pasajero.
     * @return nuevo detalle de reserva con los datos actualizados.
     */
    public ReservationDetail withPassenger(String newFullName, String newDocument) {
        return new ReservationDetail(id, newFullName, newDocument, seat);
    }

    /**
     * Normaliza y valida el documento de identidad del pasajero.
     *
     * Elimina los espacios, convierte los caracteres a mayúsculas
     * y permite únicamente letras y números.
     *
     * @param document documento recibido para su validación.
     * @return documento normalizado.
     * @throws ValidationException si el documento está vacío, supera
     *         la longitud permitida o contiene caracteres no admitidos.
     */
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

    /**
     * Obtiene el identificador del detalle.
     *
     * @return identificador del detalle de reserva.
     */
    public int getId() {
        return id;
    }

    /**
     * Obtiene el nombre completo del pasajero.
     *
     * @return nombre completo del pasajero.
     */
    public String getPassengerFullName() {
        return passengerFullName;
    }

    /**
     * Obtiene el documento normalizado del pasajero.
     *
     * @return documento de identidad.
     */
    public String getPassengerDocument() {
        return passengerDocument;
    }

    /**
     * Obtiene la silla asignada al pasajero.
     *
     * @return silla correspondiente al pasajero.
     */
    public Seat getSeat() {
        return seat;
    }

    /**
     * Compara dos detalles de reserva mediante su identificador.
     *
     * @param o objeto que se desea comparar.
     * @return true si ambos detalles tienen el mismo identificador.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationDetail other)) return false;
        return id == other.id;
    }

    /**
     * Genera el código hash utilizando el identificador del detalle.
     *
     * @return código hash del detalle de reserva.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Representa el detalle mostrando el nombre, documento y silla del pasajero.
     *
     * @return descripción del pasajero y su silla asignada.
     */
    @Override
    public String toString() {
        return passengerFullName + " (" + passengerDocument + ") - silla " + seat;
    }
}
