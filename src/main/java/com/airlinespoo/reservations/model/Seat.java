
package com.airlinespoo.reservations.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;

/**
 * Representa una silla de un vuelo mediante una fila y un número.
 *
 * Es un objeto inmutable: sus valores se establecen al construirlo
 * y no pueden modificarse posteriormente.
 */
public final class Seat implements Comparable<Seat> {

    /** Longitud máxima permitida para identificar la fila. */
    public static final int MAX_ROW_LENGTH = 2;

    /** Número mínimo permitido para identificar una silla. */
    public static final int MIN_SEAT_NUMBER = 1;

    /** Número máximo permitido para identificar una silla. */
    public static final int MAX_SEAT_NUMBER = 99;

    /** Identificador de la fila donde se encuentra la silla. */
    private final String rowNumber;

    /** Número de la silla dentro de la fila. */
    private final int seatNumber;

    /**
     * Construye una silla validando su fila y número.
     *
     * La fila se convierte a mayúsculas y solo admite letras y dígitos.
     *
     * @param rowNumber identificador de la fila.
     * @param seatNumber número de la silla.
     * @throws ValidationException si alguno de los valores no cumple
     *         las reglas de validación.
     */
    public Seat(String rowNumber, int seatNumber) {
        String row = Validator.requireText(rowNumber, "fila de la silla", MAX_ROW_LENGTH)
            .toUpperCase();
        if (!row.matches("[A-Z0-9]+")) {
            throw new ValidationException(
                "El campo 'fila de la silla' solo admite letras y dígitos.");
        }
        Validator.requireInRange(seatNumber, MIN_SEAT_NUMBER, MAX_SEAT_NUMBER, "número de silla");
        this.rowNumber = row;
        this.seatNumber = seatNumber;
    }

    /**
     * Obtiene el identificador de la fila.
     *
     * @return fila de la silla.
     */
    public String getRowNumber() {
        return rowNumber;
    }

    /**
     * Obtiene el número de la silla.
     *
     * @return número de la silla.
     */
    public int getSeatNumber() {
        return seatNumber;
    }

    /**
     * Compara esta silla con otra.
     *
     * Primero compara las filas y, si son iguales, compara los números.
     *
     * @param other silla con la que se realizará la comparación.
     * @return valor negativo, cero o positivo según el orden de comparación.
     */
    @Override
    public int compareTo(Seat other) {
        int byRow = rowNumber.compareTo(other.rowNumber);
        return byRow != 0 ? byRow : Integer.compare(seatNumber, other.seatNumber);
    }

    /**
     * Comprueba si dos sillas tienen la misma fila y el mismo número.
     *
     * @param o objeto que se desea comparar.
     * @return true si ambos objetos representan la misma silla.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seat other)) return false;
        return seatNumber == other.seatNumber && rowNumber.equals(other.rowNumber);
    }

    /**
     * Genera el código hash de la silla utilizando su fila y número.
     *
     * @return código hash de la silla.
     */
    @Override
    public int hashCode() {
        return Objects.hash(rowNumber, seatNumber);
    }

    /**
     * Representa la silla mediante la unión de su fila y número.
     *
     * @return código de la silla, por ejemplo A1 o B12.
     */
    @Override
    public String toString() {
        return rowNumber + seatNumber;
    }
}
