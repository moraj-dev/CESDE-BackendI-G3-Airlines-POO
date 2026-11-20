package com.airlinespoo.flights.model;

import com.airlinespoo.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightScheduleTest {

    @Test
    void acceptsArrivalOnTheNextDayAndCalculatesDuration() {
        FlightSchedule schedule = new FlightSchedule(
                LocalDate.of(2026, 10, 25), LocalTime.of(23, 30),
                LocalDate.of(2026, 10, 26), LocalTime.of(1, 10));

        assertEquals(Duration.ofMinutes(100), schedule.getDuration());
        assertEquals("1 h 40 min", schedule.formatDuration());
    }

    @Test
    void rejectsMissingScheduleValues() {
        assertThrows(ValidationException.class, () -> new FlightSchedule(
                null, LocalTime.NOON, LocalDate.now(), LocalTime.NOON));
        assertThrows(ValidationException.class, () -> new FlightSchedule(
                LocalDate.now(), LocalTime.NOON, LocalDate.now(), null));
    }

    @Test
    void rejectsArrivalAtOrBeforeDeparture() {
        LocalDate date = LocalDate.of(2026, 10, 25);
        assertThrows(ValidationException.class, () -> new FlightSchedule(
                date, LocalTime.NOON, date, LocalTime.NOON));
        assertThrows(ValidationException.class, () -> new FlightSchedule(
                date, LocalTime.NOON, date, LocalTime.of(11, 59)));
    }

    @Test
    void hasDepartedIncludesTheExactDepartureTime() {
        FlightSchedule schedule = new FlightSchedule(
                LocalDate.of(2026, 10, 25), LocalTime.NOON,
                LocalDate.of(2026, 10, 25), LocalTime.of(13, 0));

        assertFalse(schedule.hasDeparted(LocalDateTime.of(2026, 10, 25, 11, 59)));
        assertTrue(schedule.hasDeparted(LocalDateTime.of(2026, 10, 25, 12, 0)));
        assertThrows(ValidationException.class, () -> schedule.hasDeparted(null));
    }

    @Test
    void equalityUsesDepartureAndArrivalInstants() {
        FlightSchedule first = new FlightSchedule(
                LocalDate.of(2026, 10, 25), LocalTime.NOON,
                LocalDate.of(2026, 10, 25), LocalTime.of(13, 0));
        FlightSchedule same = new FlightSchedule(
                LocalDate.of(2026, 10, 25), LocalTime.NOON,
                LocalDate.of(2026, 10, 25), LocalTime.of(13, 0));
        FlightSchedule different = new FlightSchedule(
                LocalDate.of(2026, 10, 25), LocalTime.NOON,
                LocalDate.of(2026, 10, 25), LocalTime.of(13, 1));

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, different);
    }
}
