package com.airlinespoo.reservations.service;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.reservations.model.Reservation;
import com.airlinespoo.reservations.model.ReservationDetail;
import com.airlinespoo.reservations.model.Seat;
import com.airlinespoo.reservations.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Reglas de negocio de reservas y pasajeros. No imprime ni lee de consola. */
public class ReservationService {

    private final ReservationRepository repository;
    private final CustomerService customerService;
    private final FlightService flightService;
    private final IdGenerator reservationIds;
    private final IdGenerator detailIds;

    public ReservationService(ReservationRepository repository,
                              CustomerService customerService,
                              FlightService flightService,
                              IdGenerator reservationIds,
                              IdGenerator detailIds) {
        this.repository = repository;
        this.customerService = customerService;
        this.flightService = flightService;
        this.reservationIds = reservationIds;
        this.detailIds = detailIds;
    }

    // ---------- Crear ----------

    /** INSERT en tblReservations + un INSERT por pasajero en tblDetailsReservation. */
    public Reservation createReservation(String customerDocument, int flightId,
                                         List<PassengerRequest> passengers) {
        if (passengers == null || passengers.isEmpty()) {
            throw new ValidationException("La reserva debe tener al menos un pasajero.");
        }
        Customer customer = customerService.getCustomer(customerDocument);
        Flight flight = getFutureFlight(flightId);
        Set<Seat> occupied = occupiedSeats(flightId, null);

        Reservation reservation = null;
        for (PassengerRequest passenger : passengers) {
            ensureSeatFree(occupied, passenger.seat());
            ReservationDetail detail = newDetail(passenger);
            if (reservation == null) {
                reservation = new Reservation(reservationIds.next(), customer, flight, detail);
            } else {
                reservation.addPassenger(detail);
            }
        }
        repository.create(reservation);
        return reservation;
    }

    /** INSERT en tblDetailsReservation. */
    public Reservation addPassenger(int reservationId, PassengerRequest passenger) {
        Reservation reservation = getReservation(reservationId);
        ensureSeatFree(occupiedSeats(reservation.getFlight().getId(), reservationId),
            passenger.seat());
        reservation.addPassenger(newDetail(passenger));
        repository.update(reservation);
        return reservation;
    }

    // ---------- Leer ----------

    public Reservation getReservation(int reservationId) {
        return repository.findById(reservationId)
            .orElseThrow(() -> new NotFoundException("reserva", reservationId));
    }

    public List<Reservation> listReservations() {
        return repository.findAll().stream()
            .sorted(Comparator.comparing(Reservation::getId))
            .toList();
    }

    public List<ReservationDetail> listPassengers(int reservationId) {
        return getReservation(reservationId).getDetails();
    }

    public List<Reservation> listReservationsByCustomer(String customerDocument) {
        customerService.getCustomer(customerDocument);
        return repository.findByCustomer(customerDocument).stream()
            // CORREGIDO: la fecha de salida vive en FlightSchedule
            .sorted(Comparator.comparing(r -> r.getFlight().getSchedule().getDeparture()))
            .toList();
    }

    public List<Reservation> listReservationsByFlight(int flightId) {
        flightService.getFlight(flightId);
        return repository.findByFlight(flightId);
    }

    // ---------- Actualizar ----------

    /** UPDATE tblReservations SET FlightId. Las sillas deben estar libres en el nuevo vuelo. */
    public Reservation changeReservationFlight(int reservationId, int newFlightId) {
        Reservation reservation = getReservation(reservationId);
        Flight newFlight = getFutureFlight(newFlightId);
        Set<Seat> occupied = occupiedSeats(newFlightId, reservationId);
        for (ReservationDetail detail : reservation.getDetails()) {
            ensureSeatFree(occupied, detail.getSeat());
        }
        reservation.setFlight(newFlight);
        repository.update(reservation);
        return reservation;
    }

    /** UPDATE tblDetailsReservation SET RowNumber, SeatNumer. */
    public Reservation changeSeat(int reservationId, int detailId, Seat newSeat) {
        Reservation reservation = getReservation(reservationId);
        ReservationDetail current = findDetailOrFail(reservation, detailId);
        ensureSeatFree(occupiedSeats(reservation.getFlight().getId(), reservationId), newSeat);
        reservation.replaceDetail(current.withSeat(newSeat));
        repository.update(reservation);
        return reservation;
    }

    /** UPDATE tblDetailsReservation SET PassengerFullname, PassagerDocument. */
    public Reservation updatePassenger(int reservationId, int detailId,
                                       String fullName, String document) {
        Reservation reservation = getReservation(reservationId);
        ReservationDetail current = findDetailOrFail(reservation, detailId);
        reservation.replaceDetail(current.withPassenger(fullName, document));
        repository.update(reservation);
        return reservation;
    }

    // ---------- Eliminar ----------

    /** DELETE en tblDetailsReservation. */
    public Reservation removePassenger(int reservationId, int detailId) {
        Reservation reservation = getReservation(reservationId);
        findDetailOrFail(reservation, detailId);
        reservation.removePassenger(detailId);
        repository.update(reservation);
        return reservation;
    }

    /** DELETE de detalles y luego de la reserva. */
    public void cancelReservation(int reservationId) {
        getReservation(reservationId);
        repository.deleteById(reservationId);
    }

    // ---------- Complementarios ----------

    public boolean customerHasReservations(String customerDocument) {
        return repository.existsByCustomer(customerDocument);
    }

    public boolean flightHasReservations(int flightId) {
        return repository.existsByFlight(flightId);
    }

    public List<Seat> listOccupiedSeats(int flightId) {
        flightService.getFlight(flightId);
        return occupiedSeats(flightId, null).stream().sorted().toList();
    }

    public boolean isSeatOccupied(int flightId, Seat seat) {
        return occupiedSeats(flightId, null).contains(seat);
    }

    public int countPassengersByFlight(int flightId) {
        flightService.getFlight(flightId);
        return repository.findByFlight(flightId).stream()
            .mapToInt(r -> r.getDetails().size())
            .sum();
    }

    public List<ManifestEntry> getFlightManifest(int flightId) {
        flightService.getFlight(flightId);
        return repository.findByFlight(flightId).stream()
            .flatMap(r -> r.getDetails().stream())
            .map(d -> new ManifestEntry(d.getPassengerFullName(),
                d.getPassengerDocument(), d.getSeat()))
            .sorted(Comparator.comparing(ManifestEntry::seat))
            .toList();
    }

    // ---------- Métodos privados ----------

    private Flight getFutureFlight(int flightId) {
        Flight flight = flightService.getFlight(flightId);
        // CORREGIDO: hasDeparted vive en FlightSchedule
        if (flight.getSchedule().hasDeparted(LocalDateTime.now())) {
            throw new BusinessRuleException("El vuelo " + flight.getFlightName() + " ya salió.");
        }
        return flight;
    }

    /** Sillas ocupadas en un vuelo, ignorando opcionalmente una reserva. */
    private Set<Seat> occupiedSeats(int flightId, Integer ignoredReservationId) {
        Set<Seat> occupied = new HashSet<>();
        for (Reservation reservation : repository.findByFlight(flightId)) {
            if (ignoredReservationId != null && ignoredReservationId.equals(reservation.getId())) {
                continue;
            }
            for (ReservationDetail detail : reservation.getDetails()) {
                occupied.add(detail.getSeat());
            }
        }
        return occupied;
    }

    private void ensureSeatFree(Set<Seat> occupied, Seat seat) {
        if (seat == null) {
            throw new ValidationException("La silla del pasajero es obligatoria.");
        }
        if (occupied.contains(seat)) {
            throw new BusinessRuleException("La silla " + seat + " ya está ocupada en ese vuelo.");
        }
    }

    private ReservationDetail newDetail(PassengerRequest passenger) {
        return new ReservationDetail(detailIds.next(), passenger.fullName(),
            passenger.document(), passenger.seat());
    }

    private ReservationDetail findDetailOrFail(Reservation reservation, int detailId) {
        return reservation.findDetail(detailId)
            .orElseThrow(() -> new NotFoundException("pasajero", detailId));
    }
}
