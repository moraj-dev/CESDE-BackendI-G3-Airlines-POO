
package com.airlinespoo.reservations.service;

import com.airlinespoo.reservations.model.Seat;

/**
 * Representa los datos de un pasajero antes de crear su detalle de reserva.
 *
 * Es un record que agrupa el nombre, el documento y la silla seleccionada.
 * Todavía no contiene un identificador de detalle; este se genera al crear
 * la reserva.
 *
 * @param fullName nombre completo del pasajero.
 * @param document documento de identidad del pasajero.
 * @param seat silla seleccionada para el pasajero.
 */
public record PassengerRequest(String fullName, String document, Seat seat) {
}
