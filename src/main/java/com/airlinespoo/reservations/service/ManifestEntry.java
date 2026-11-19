
package com.airlinespoo.reservations.service;

import com.airlinespoo.reservations.model.Seat;

/**
 * Representa una fila del manifiesto de pasajeros de un vuelo.
 *
 * Contiene la información básica de cada pasajero que tiene una reserva:
 * su nombre completo, documento de identidad y silla asignada.
 *
 * @param passengerFullName nombre completo del pasajero.
 * @param passengerDocument documento de identidad del pasajero.
 * @param seat silla asignada al pasajero en el vuelo.
 */
public record ManifestEntry(String passengerFullName, String passengerDocument, Seat seat) {
}
