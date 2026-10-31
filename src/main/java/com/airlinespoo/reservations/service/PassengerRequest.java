package com.airlinespoo.reservations.service;

import com.airlinespoo.reservations.model.Seat;

/** Datos de un pasajero tal como los captura la interfaz (aún sin id). */
public record PassengerRequest(String fullName, String document, Seat seat) {
}
