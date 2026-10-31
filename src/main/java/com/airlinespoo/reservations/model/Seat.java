package com.airlinespoo.reservations.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;

/** Silla de un vuelo: fila + número. Objeto inmutable. */
public final class Seat implements Comparable<Seat> {

    public static final int MAX_ROW_LENGTH = 2;
    public static final int MIN_SEAT_NUMBER = 1;
    public static final int MAX_SEAT_NUMBER = 99;

    private final String rowNumber;
    private final int seatNumber;

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

    public String getRowNumber() {
        return rowNumber;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    @Override
    public int compareTo(Seat other) {
        int byRow = rowNumber.compareTo(other.rowNumber);
        return byRow != 0 ? byRow : Integer.compare(seatNumber, other.seatNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seat other)) return false;
        return seatNumber == other.seatNumber && rowNumber.equals(other.rowNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowNumber, seatNumber);
    }

    @Override
    public String toString() {
        return rowNumber + seatNumber;
    }
}
