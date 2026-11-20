
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

/**
 * Contiene las reglas de negocio relacionadas con las reservas y sus pasajeros.
 * Se encarga de coordinar los servicios de clientes, vuelos y el repositorio
 * de reservas. No realiza operaciones de entrada o salida por consola.
 */
public class ReservationService {

    /** Repositorio encargado de almacenar y consultar las reservas. */
    private final ReservationRepository repository;

    /** Servicio utilizado para consultar y validar los clientes. */
    private final CustomerService customerService;

    /** Servicio utilizado para consultar y validar los vuelos. */
    private final FlightService flightService;

    /** Generador de identificadores únicos para las reservas. */
    private final IdGenerator reservationIds;

    /** Generador de identificadores únicos para los detalles de reserva. */
    private final IdGenerator detailIds;

    /**
     * Construye el servicio de reservas e inicializa sus dependencias.
     *
     * @param repository repositorio de reservas.
     * @param customerService servicio de clientes.
     * @param flightService servicio de vuelos.
     * @param reservationIds generador de identificadores de reservas.
     * @param detailIds generador de identificadores de detalles.
     */
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

    /**
     * Crea una reserva para un cliente y un vuelo, incluyendo sus pasajeros.
     * Verifica que exista al menos un pasajero y que las sillas estén libres.
     *
     * @param customerDocument documento del cliente que realiza la reserva.
     * @param flightId identificador del vuelo seleccionado.
     * @param passengers lista de pasajeros que se incluirán en la reserva.
     * @return reserva creada y almacenada en el repositorio.
     */
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

    /**
     * Agrega un pasajero a una reserva existente.
     * Comprueba que la silla solicitada no esté ocupada en el vuelo.
     *
     * @param reservationId identificador de la reserva.
     * @param passenger información del pasajero que se agregará.
     * @return reserva actualizada con el nuevo pasajero.
     */
    public Reservation addPassenger(int reservationId, PassengerRequest passenger) {
        Reservation reservation = getReservation(reservationId);
        ensureSeatFree(occupiedSeats(reservation.getFlight().getId(), reservationId),
            passenger.seat());
        reservation.addPassenger(newDetail(passenger));
        repository.update(reservation);
        return reservation;
    }

    // ---------- Leer ----------

    /**
     * Busca una reserva por su identificador.
     *
     * @param reservationId identificador de la reserva.
     * @return reserva encontrada.
     * @throws NotFoundException si la reserva no existe.
     */
    public Reservation getReservation(int reservationId) {
        return repository.findById(reservationId)
            .orElseThrow(() -> new NotFoundException("reserva", reservationId));
    }

    /**
     * Consulta todas las reservas y las organiza por identificador.
     *
     * @return lista de reservas ordenadas por su identificador.
     */
    public List<Reservation> listReservations() {
        return repository.findAll().stream()
            .sorted(Comparator.comparing(Reservation::getId))
            .toList();
    }

    /**
     * Consulta los pasajeros asociados a una reserva.
     *
     * @param reservationId identificador de la reserva.
     * @return lista de detalles de los pasajeros.
     */
    public List<ReservationDetail> listPassengers(int reservationId) {
        return getReservation(reservationId).getDetails();
    }

    /**
     * Consulta las reservas realizadas por un cliente.
     * Ordena los resultados según la fecha de salida de los vuelos.
     *
     * @param customerDocument documento del cliente.
     * @return lista de reservas del cliente.
     */
    public List<Reservation> listReservationsByCustomer(String customerDocument) {
        customerService.getCustomer(customerDocument);
        return repository.findByCustomer(customerDocument).stream()
            .sorted(Comparator.comparing(r -> r.getFlight().getSchedule().getDeparture()))
            .toList();
    }

    /**
     * Consulta las reservas asociadas a un vuelo.
     *
     * @param flightId identificador del vuelo.
     * @return lista de reservas del vuelo.
     */
    public List<Reservation> listReservationsByFlight(int flightId) {
        flightService.getFlight(flightId);
        return repository.findByFlight(flightId);
    }

    // ---------- Actualizar ----------

    /**
     * Cambia el vuelo asociado a una reserva.
     * Verifica que el nuevo vuelo no haya salido y que las sillas de los
     * pasajeros estén disponibles en ese vuelo.
     *
     * @param reservationId identificador de la reserva.
     * @param newFlightId identificador del nuevo vuelo.
     * @return reserva actualizada con el nuevo vuelo.
     */
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

    /**
     * Cambia la silla asignada a un pasajero de una reserva.
     *
     * @param reservationId identificador de la reserva.
     * @param detailId identificador del detalle del pasajero.
     * @param newSeat nueva silla que se desea asignar.
     * @return reserva actualizada.
     */
    public Reservation changeSeat(int reservationId, int detailId, Seat newSeat) {
        Reservation reservation = getReservation(reservationId);
        ReservationDetail current = findDetailOrFail(reservation, detailId);
        ensureSeatFree(occupiedSeats(reservation.getFlight().getId(), reservationId), newSeat);
        reservation.replaceDetail(current.withSeat(newSeat));
        repository.update(reservation);
        return reservation;
    }

    /**
     * Actualiza el nombre y el documento de un pasajero.
     *
     * @param reservationId identificador de la reserva.
     * @param detailId identificador del detalle del pasajero.
     * @param fullName nuevo nombre completo del pasajero.
     * @param document nuevo documento del pasajero.
     * @return reserva actualizada.
     */
    public Reservation updatePassenger(int reservationId, int detailId,
                                       String fullName, String document) {
        Reservation reservation = getReservation(reservationId);
        ReservationDetail current = findDetailOrFail(reservation, detailId);
        reservation.replaceDetail(current.withPassenger(fullName, document));
        repository.update(reservation);
        return reservation;
    }

    // ---------- Eliminar ----------

    /**
     * Elimina un pasajero de una reserva existente.
     *
     * @param reservationId identificador de la reserva.
     * @param detailId identificador del pasajero que se eliminará.
     * @return reserva actualizada después de eliminar el pasajero.
     */
    public Reservation removePassenger(int reservationId, int detailId) {
        Reservation reservation = getReservation(reservationId);
        findDetailOrFail(reservation, detailId);
        reservation.removePassenger(detailId);
        repository.update(reservation);
        return reservation;
    }

    /**
     * Cancela una reserva y la elimina del repositorio.
     *
     * @param reservationId identificador de la reserva que se cancelará.
     */
    public void cancelReservation(int reservationId) {
        getReservation(reservationId);
        repository.deleteById(reservationId);
    }

    // ---------- Complementarios ----------

    /**
     * Verifica si un cliente tiene reservas registradas.
     *
     * @param customerDocument documento del cliente.
     * @return true si el cliente tiene reservas; false en caso contrario.
     */
    public boolean customerHasReservations(String customerDocument) {
        return repository.existsByCustomer(customerDocument);
    }

    /**
     * Verifica si un vuelo tiene reservas registradas.
     *
     * @param flightId identificador del vuelo.
     * @return true si el vuelo tiene reservas; false en caso contrario.
     */
    public boolean flightHasReservations(int flightId) {
        return repository.existsByFlight(flightId);
    }

    /**
     * Consulta las sillas ocupadas en un vuelo y las ordena.
     *
     * @param flightId identificador del vuelo.
     * @return lista ordenada de sillas ocupadas.
     */
    public List<Seat> listOccupiedSeats(int flightId) {
        flightService.getFlight(flightId);
        return occupiedSeats(flightId, null).stream().sorted().toList();
    }

    /**
     * Comprueba si una silla está ocupada en un vuelo.
     *
     * @param flightId identificador del vuelo.
     * @param seat silla que se desea consultar.
     * @return true si la silla está ocupada; false en caso contrario.
     */
    public boolean isSeatOccupied(int flightId, Seat seat) {
        return occupiedSeats(flightId, null).contains(seat);
    }

    /**
     * Calcula la cantidad total de pasajeros reservados para un vuelo.
     *
     * @param flightId identificador del vuelo.
     * @return cantidad total de pasajeros.
     */
    public int countPassengersByFlight(int flightId) {
        flightService.getFlight(flightId);
        return repository.findByFlight(flightId).stream()
            .mapToInt(r -> r.getDetails().size())
            .sum();
    }

    /**
     * Genera el manifiesto de pasajeros de un vuelo.
     * Incluye el nombre, documento y silla de cada pasajero.
     *
     * @param flightId identificador del vuelo.
     * @return lista de pasajeros ordenada por silla.
     */
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

    /**
     * Busca un vuelo y verifica que todavía no haya salido.
     *
     * @param flightId identificador del vuelo.
     * @return vuelo encontrado que aún no ha salido.
     * @throws BusinessRuleException si el vuelo ya salió.
     */
    private Flight getFutureFlight(int flightId) {
        Flight flight = flightService.getFlight(flightId);
        if (flight.getSchedule().hasDeparted(LocalDateTime.now())) {
            throw new BusinessRuleException("El vuelo " + flight.getFlightName() + " ya salió.");
        }
        return flight;
    }

    /**
     * Obtiene las sillas ocupadas en un vuelo.
     * Permite ignorar una reserva específica cuando se está modificando.
     *
     * @param flightId identificador del vuelo.
     * @param ignoredReservationId identificador de la reserva que se debe ignorar,
     *                              o null si no se debe ignorar ninguna.
     * @return conjunto de sillas ocupadas.
     */
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

    /**
     * Verifica que una silla esté disponible.
     *
     * @param occupied conjunto de sillas que ya están ocupadas.
     * @param seat silla que se desea asignar.
     * @throws ValidationException si la silla es null.
     * @throws BusinessRuleException si la silla ya está ocupada.
     */
    private void ensureSeatFree(Set<Seat> occupied, Seat seat) {
        if (seat == null) {
            throw new ValidationException("La silla del pasajero es obligatoria.");
        }
        if (occupied.contains(seat)) {
            throw new BusinessRuleException("La silla " + seat + " ya está ocupada en ese vuelo.");
        }
    }

    /**
     * Construye un nuevo detalle de reserva a partir de la información recibida.
     *
     * @param passenger datos del pasajero.
     * @return nuevo detalle de reserva con un identificador generado.
     */
    private ReservationDetail newDetail(PassengerRequest passenger) {
        return new ReservationDetail(detailIds.next(), passenger.fullName(),
            passenger.document(), passenger.seat());
    }

    /**
     * Busca un pasajero dentro de una reserva.
     *
     * @param reservation reserva donde se realizará la búsqueda.
     * @param detailId identificador del detalle del pasajero.
     * @return detalle del pasajero encontrado.
     * @throws NotFoundException si el pasajero no pertenece a la reserva.
     */
    private ReservationDetail findDetailOrFail(Reservation reservation, int detailId) {
        return reservation.findDetail(detailId)
            .orElseThrow(() -> new NotFoundException("pasajero", detailId));
    }
}
