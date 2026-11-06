package com.airlinespoo.reservations.ui;

import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.reservations.model.Seat;
import com.airlinespoo.reservations.service.PassengerRequest;
import com.airlinespoo.reservations.service.ReservationService;

import java.util.List;

/** Carga 4 reservas de ejemplo. Requiere clientes y vuelos ya cargados. */
public class ReservationSampleData {

    private final ReservationService service;
    private final CustomerService customerService;
    private final FlightService flightService;

    public ReservationSampleData(ReservationService service, CustomerService customerService,
                                 FlightService flightService) {
        this.service = service;
        this.customerService = customerService;
        this.flightService = flightService;
    }

    public void load() {
        List<Customer> customers = customerService.listCustomers();
        List<Flight> flights = flightService.listFlights();
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

    private void createSample(Customer customer, Flight flight, List<PassengerRequest> passengers) {
        service.createReservation(customer.getId(), flight.getId(), passengers);
    }
}
