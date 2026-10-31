package com.airlinespoo.reservations.service;

import com.airlinespoo.reservations.model.Seat;

/** Fila del manifiesto de un vuelo: nombre, documento y silla. */
public record ManifestEntry(String passengerFullName, String passengerDocument, Seat seat) {
}
