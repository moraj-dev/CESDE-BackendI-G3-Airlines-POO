
package com.airlinespoo.reservations.ui;

import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.reservations.model.Seat;
import com.airlinespoo.reservations.service.PassengerRequest;
import com.airlinespoo.reservations.service.ReservationService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Proporciona datos de prueba para el módulo de reservas.
 *
 * Carga cuatro reservas de ejemplo utilizando clientes y vuelos
 * previamente registrados en sus respectivos módulos.
 */
public class ReservationSampleData {

    /** Servicio encargado de crear las reservas de prueba. */
    private final ReservationService service;

    /** Servicio utilizado para consultar los clientes registrados. */
    private final CustomerService customerService;

    /** Servicio utilizado para consultar los vuelos disponibles. */
    private final FlightService flightService;

    /**
     * Construye el componente encargado de cargar las reservas de ejemplo.
     *
     * @param service servicio de reservas.
     * @param customerService servicio de clientes.
     * @param flightService servicio de vuelos.
     */
    public ReservationSampleData(ReservationService service, CustomerService customerService,
                                 FlightService flightService) {
        this.service = service;
        this.customerService = customerService;
        this.flightService = flightService;
    }

    /**
     * Consulta los clientes y vuelos registrados y crea cuatro reservas
     * de ejemplo con diferentes cantidades de pasajeros.
     *
     * Solo utiliza vuelos que todavía no han salido.
     * Si no existen clientes o vuelos disponibles, finaliza sin crear reservas.
     */
    public void load() {
        List<Customer> customers = customerService.listCustomers();
        List<Flight> flights = flightService.listFlights().stream()
            .filter(f -> !f.getSchedule().hasDeparted(LocalDateTime.now()))
            .toList();
        if (customers.isEmpty() || flights.isEmpty()) {
            return;
        }
        createSample(customers.get(0), flights.get(0), List.of(
            new PassengerRequest("Ana Gómez", "AG1001", new Seat("A", 1))));
        createSample(customers.get(customers.size() > 1 ? 1 : 0), flights.get(0), List.of(
            new PassengerRequest("Luis Pérez", "LP2002", new Seat("A", 2)),
            new PassengerRequest("Marta Pérez", "MP2003", new Seat("A", 3))));
        createSample(customers.get(customers.size() > 2 ? 2 : 0),
            flights.get(flights.size() > 1 ? 1 : 0), List.of(
                new PassengerRequest("Carlos Ruiz", "CR3001", new Seat("B", 1)),
                new PassengerRequest("Elena Ruiz", "ER3002", new Seat("B", 2)),
                new PassengerRequest("Sofía Ruiz", "SR3003", new Seat("B", 3))));
        createSample(customers.get(0), flights.get(flights.size() > 2 ? 2 : 0), List.of(
            new PassengerRequest("Ana Gómez", "AG1001", new Seat("C", 10))));
    }

    /**
     * Crea una reserva de ejemplo para el cliente y vuelo recibidos.
     *
     * @param customer cliente al que se asociará la reserva.
     * @param flight vuelo al que pertenecerá la reserva.
     * @param passengers lista de pasajeros que tendrá la reserva.
     */
    private void createSample(Customer customer, Flight flight, List<PassengerRequest> passengers) {
        service.createReservation(customer.getId(), flight.getId(), passengers);
    }
}
