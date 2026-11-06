package com.airlinespoo.reservations.ui;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.reservations.model.Reservation;
import com.airlinespoo.reservations.model.ReservationDetail;
import com.airlinespoo.reservations.model.Seat;
import com.airlinespoo.reservations.service.ManifestEntry;
import com.airlinespoo.reservations.service.PassengerRequest;
import com.airlinespoo.reservations.service.ReservationService;

import java.util.ArrayList;
import java.util.List;

/** Menú de consola del módulo de reservas. */
public class ReservationMenu implements Menu {

    private static final int EXIT = 0;

    private final ReservationService service;
    private final CustomerService customerService;
    private final FlightService flightService;
    private final ConsoleInput input;

    public ReservationMenu(ReservationService service, CustomerService customerService,
                           FlightService flightService, ConsoleInput input) {
        this.service = service;
        this.customerService = customerService;
        this.flightService = flightService;
        this.input = input;
    }

    @Override
    public String title() {
        return "Reservas y pasajeros";
    }

    @Override
    public void run() {
        int option;
        do {
            printOptions();
            option = input.readInt("Elige una opción", EXIT, 8);
            try {
                execute(option);
            } catch (AirlinesException e) {
                System.out.println("No se pudo completar la operación: " + e.getMessage());
            }
        } while (option != EXIT);
    }

    private void printOptions() {
        System.out.println("\n=== " + title() + " ===");
        System.out.println("1. Crear reserva");
        System.out.println("2. Agregar pasajero");
        System.out.println("3. Quitar pasajero");
        System.out.println("4. Cambiar silla");
        System.out.println("5. Cancelar reserva");
        System.out.println("6. Consultar reservas de un cliente");
        System.out.println("7. Ver sillas ocupadas de un vuelo");
        System.out.println("8. Ver manifiesto de un vuelo");
        System.out.println("0. Volver");
    }

    private void execute(int option) {
        switch (option) {
            case 1 -> createReservation();
            case 2 -> addPassenger();
            case 3 -> removePassenger();
            case 4 -> changeSeat();
            case 5 -> cancelReservation();
            case 6 -> listByCustomer();
            case 7 -> showOccupiedSeats();
            case 8 -> showManifest();
            default -> { /* 0: volver */ }
        }
    }

    private void createReservation() {
        Customer customer = customerService.getCustomer(
            input.readRequiredText("Documento del cliente"));
        System.out.println("Cliente: " + customer.getFullName());

        Flight flight = flightService.getFlightByName(
            input.readRequiredText("Código del vuelo").toUpperCase());
        System.out.println("Vuelo " + flight.getFlightName() + ": "
            + flight.getDeparture().getName() + " -> " + flight.getArrival().getName());

        if (!input.confirm("¿Continuar con este cliente y vuelo?")) {
            System.out.println("Operación cancelada.");
            return;
        }
        List<PassengerRequest> passengers = new ArrayList<>();
        do {
            passengers.add(readPassenger());
        } while (passengers.size() < Reservation.MAX_PASSENGERS
            && input.confirm("¿Agregar otro pasajero?"));

        Reservation reservation = service.createReservation(
            customer.getId(), flight.getId(), passengers);
        System.out.println("Reserva creada: " + reservation);
    }

    private void addPassenger() {
        int reservationId = input.readInt("Id de la reserva", 1, Integer.MAX_VALUE);
        Reservation reservation = service.addPassenger(reservationId, readPassenger());
        System.out.println("Pasajero agregado. " + reservation);
    }

    private void removePassenger() {
        int reservationId = input.readInt("Id de la reserva", 1, Integer.MAX_VALUE);
        printPassengers(service.listPassengers(reservationId));
        int detailId = input.readInt("Id del pasajero a quitar", 1, Integer.MAX_VALUE);
        service.removePassenger(reservationId, detailId);
        System.out.println("Pasajero eliminado de la reserva.");
    }

    private void changeSeat() {
        int reservationId = input.readInt("Id de la reserva", 1, Integer.MAX_VALUE);
        printPassengers(service.listPassengers(reservationId));
        int detailId = input.readInt("Id del pasajero", 1, Integer.MAX_VALUE);
        service.changeSeat(reservationId, detailId, readSeat());
        System.out.println("Silla actualizada.");
    }

    private void cancelReservation() {
        int reservationId = input.readInt("Id de la reserva", 1, Integer.MAX_VALUE);
        if (input.confirm("¿Seguro que deseas cancelar la reserva " + reservationId + "?")) {
            service.cancelReservation(reservationId);
            System.out.println("Reserva cancelada. Sus sillas quedaron libres.");
        }
    }

    private void listByCustomer() {
        String document = input.readRequiredText("Documento del cliente");
        List<Reservation> reservations = service.listReservationsByCustomer(document);
        if (reservations.isEmpty()) {
            System.out.println("El cliente no tiene reservas.");
            return;
        }
        reservations.forEach(System.out::println);
    }

    private void showOccupiedSeats() {
        Flight flight = flightService.getFlightByName(
            input.readRequiredText("Código del vuelo").toUpperCase());
        List<Seat> seats = service.listOccupiedSeats(flight.getId());
        System.out.println(seats.isEmpty() ? "No hay sillas ocupadas." : "Ocupadas: " + seats);
    }

    private void showManifest() {
        Flight flight = flightService.getFlightByName(
            input.readRequiredText("Código del vuelo").toUpperCase());
        List<ManifestEntry> manifest = service.getFlightManifest(flight.getId());
        if (manifest.isEmpty()) {
            System.out.println("El vuelo no tiene pasajeros.");
            return;
        }
        System.out.printf("%-6s %-30s %-20s%n", "SILLA", "PASAJERO", "DOCUMENTO");
        for (ManifestEntry entry : manifest) {
            System.out.printf("%-6s %-30s %-20s%n", entry.seat(),
                entry.passengerFullName(), entry.passengerDocument());
        }
        System.out.println("Total: " + service.countPassengersByFlight(flight.getId())
            + " pasajeros");
    }

    private PassengerRequest readPassenger() {
        String name = input.readRequiredText("Nombre completo del pasajero");
        String document = input.readRequiredText("Documento del pasajero");
        return new PassengerRequest(name, document, readSeat());
    }

    private Seat readSeat() {
        String row = input.readRequiredText("Fila de la silla (ej. B)");
        int number = input.readInt("Número de silla", Seat.MIN_SEAT_NUMBER, Seat.MAX_SEAT_NUMBER);
        return new Seat(row, number);
    }

    private void printPassengers(List<ReservationDetail> details) {
        details.forEach(d -> System.out.println("  [" + d.getId() + "] " + d));
    }
}
