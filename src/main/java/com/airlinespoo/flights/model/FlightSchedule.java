package com.airlinespoo.flights.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Horario de un vuelo: fecha y hora de salida y de llegada.
 * Es inmutable: para reprogramar se crea un horario nuevo.
 */
    public final class FlightSchedule {

    private final LocalDate departureDate;
    private final LocalTime departureTime;
    private final LocalDate arrivalDate;
    private final LocalTime arrivalTime;

    /**
     * @param departureDate fecha de salida
     * @param departureTime hora de salida
     * @param arrivalDate   fecha de llegada
     * @param arrivalTime   hora de llegada
     * @throws ValidationException si falta algún dato o la llegada no es posterior a la salida
     */
    public FlightSchedule(LocalDate departureDate, LocalTime departureTime,
                          LocalDate arrivalDate, LocalTime arrivalTime) {
        this.departureDate = Validator.requireNotNull(departureDate, "fecha de salida");
        this.departureTime = Validator.requireNotNull(departureTime, "hora de salida");
        this.arrivalDate = Validator.requireNotNull(arrivalDate, "fecha de llegada");
        this.arrivalTime = Validator.requireNotNull(arrivalTime, "hora de llegada");
        requireArrivalAfterDeparture();
    }

    private void requireArrivalAfterDeparture() {
        if (!getArrival().isAfter(getDeparture())) {
            throw new ValidationException("La llegada no puede ser anterior ni igual a la salida.");
        }
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    /** @return salida con fecha y hora juntas */
    public LocalDateTime getDeparture() {
        return LocalDateTime.of(departureDate, departureTime);
    }

    /** @return llegada con fecha y hora juntas */
    public LocalDateTime getArrival() {
        return LocalDateTime.of(arrivalDate, arrivalTime);
    }

    /** @return duración total del vuelo */
    public Duration getDuration() {
        return Duration.between(getDeparture(), getArrival());
    }

    /**
     * @param now momento de referencia
     * @return {@code true} si el vuelo ya salió respecto a ese momento
     */
    public boolean hasDeparted(LocalDateTime now) {
        Validator.requireNotNull(now, "momento actual");
        return !getDeparture().isAfter(now);
    }

    /** @return duración en formato "1 h 40 min" */
    public String formatDuration() {
        Duration duration = getDuration();
        return "%d h %d min".formatted(duration.toHours(), duration.toMinutesPart());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FlightSchedule that)) {
            return false;
        }
        return getDeparture().equals(that.getDeparture()) && getArrival().equals(that.getArrival());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeparture(), getArrival());
    }

    @Override
    public String toString() {
        return "%s → %s".formatted(getDeparture(), getArrival());
    }
}
