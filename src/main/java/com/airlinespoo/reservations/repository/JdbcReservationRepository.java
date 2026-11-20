
package com.airlinespoo.reservations.repository;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.customers.repository.CustomerRepository;
import com.airlinespoo.flights.repository.FlightRepository;
import com.airlinespoo.reservations.model.Reservation;
import com.airlinespoo.reservations.model.ReservationDetail;
import com.airlinespoo.reservations.model.Seat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de reservas.
 *
 * <p>Almacena las reservas en dbo.tblReservations y sus pasajeros
 * en dbo.tblDetailsReservation.</p>
 *
 * <p>Las operaciones de escritura utilizan transacciones para mantener
 * la consistencia entre la reserva y sus detalles.</p>
 *
 * @author Equipo Airlines
 * @version 1.0
 */
public class JdbcReservationRepository implements ReservationRepository {

    private static final String ENTITY = "reserva";

    private static final String INSERT_RESERVATION =
        "INSERT INTO dbo.tblReservations "
            + "(ReservationId, CustomerId, FlightId) VALUES (?, ?, ?)";

    private static final String INSERT_DETAIL =
        "INSERT INTO dbo.tblDetailsReservation "
            + "(DetailId, ReservationId, PassangerFullname, "
            + "PassagerDocument, SeatNumer, RowNumber) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_RESERVATION =
        "SELECT ReservationId, CustomerId, FlightId "
            + "FROM dbo.tblReservations WHERE ReservationId = ?";

    private static final String SELECT_ALL =
        "SELECT ReservationId FROM dbo.tblReservations ORDER BY ReservationId";

    private static final String SELECT_BY_CUSTOMER =
        "SELECT ReservationId FROM dbo.tblReservations "
            + "WHERE CustomerId = ? ORDER BY ReservationId";

    private static final String SELECT_BY_FLIGHT =
        "SELECT ReservationId FROM dbo.tblReservations "
            + "WHERE FlightId = ? ORDER BY ReservationId";

    private static final String SELECT_DETAILS =
        "SELECT DetailId, PassangerFullname, PassagerDocument, "
            + "SeatNumer, RowNumber "
            + "FROM dbo.tblDetailsReservation "
            + "WHERE ReservationId = ? ORDER BY DetailId";

    private static final String UPDATE_RESERVATION =
        "UPDATE dbo.tblReservations SET CustomerId = ?, FlightId = ? "
            + "WHERE ReservationId = ?";

    private static final String DELETE_DETAILS =
        "DELETE FROM dbo.tblDetailsReservation WHERE ReservationId = ?";

    private static final String DELETE_RESERVATION =
        "DELETE FROM dbo.tblReservations WHERE ReservationId = ?";

    private static final String EXISTS_BY_CUSTOMER =
        "SELECT 1 FROM dbo.tblReservations WHERE CustomerId = ?";

    private static final String EXISTS_BY_FLIGHT =
        "SELECT 1 FROM dbo.tblReservations WHERE FlightId = ?";

    private static final String COUNT =
        "SELECT COUNT(*) FROM dbo.tblReservations";

    private final SqlConnectionProvider database;
    private final CustomerRepository customerRepository;
    private final FlightRepository flightRepository;

    /**
     * Recibe las dependencias necesarias para acceder a la base de datos
     * y reconstruir las entidades relacionadas.
     *
     * @param database proveedor de conexiones JDBC
     * @param customerRepository repositorio de clientes
     * @param flightRepository repositorio de vuelos
     */
    public JdbcReservationRepository(
        SqlConnectionProvider database,
        CustomerRepository customerRepository,
        FlightRepository flightRepository) {

        if (database == null || customerRepository == null || flightRepository == null) {
            throw new IllegalArgumentException(
                "Las dependencias del repositorio no pueden ser nulas.");
        }

        this.database = database;
        this.customerRepository = customerRepository;
        this.flightRepository = flightRepository;
    }

    /**
     * Guarda una reserva y todos sus pasajeros en una misma transacción.
     *
     * @param reservation reserva que se desea guardar
     * @return reserva almacenada
     */
    @Override
    public Reservation create(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula.");
        }

        try (Connection con = database.open()) {
            con.setAutoCommit(false);

            try {
                try (PreparedStatement sql = con.prepareStatement(INSERT_RESERVATION)) {
                    sql.setInt(1, reservation.getId());
                    sql.setString(2, reservation.getCustomer().getId());
                    sql.setInt(3, reservation.getFlight().getId());
                    sql.executeUpdate();
                }

                insertDetails(con, reservation);
                con.commit();

                return reservation;

            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(con);
                throw e;
            }

        } catch (SQLException e) {
            throw ReservationDatabaseErrors.translate(e, ENTITY);
        }
    }

    /**
     * Busca una reserva por su identificador.
     *
     * @param id identificador de la reserva
     * @return reserva encontrada o vacío si no existe
     */
    @Override
    public Optional<Reservation> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }

        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(SELECT_RESERVATION)) {

            sql.setInt(1, id);

            try (ResultSet row = sql.executeQuery()) {
                if (!row.next()) {
                    return Optional.empty();
                }

                String customerId = row.getString("CustomerId");
                int flightId = row.getInt("FlightId");

                return Optional.of(
                    buildReservation(con, id, customerId, flightId)
                );
            }

        } catch (SQLException e) {
            throw ReservationDatabaseErrors.translate(e, ENTITY);
        }
    }

    /**
     * Recupera todas las reservas registradas.
     *
     * @return lista de reservas
     */
    @Override
    public List<Reservation> findAll() {
        return findByIds(SELECT_ALL);
    }

    /**
     * Actualiza una reserva y reemplaza sus detalles de pasajeros.
     *
     * @param reservation reserva con los datos actualizados
     * @return reserva actualizada
     * @throws NotFoundException si la reserva no existe
     */
    @Override
    public Reservation update(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula.");
        }

        try (Connection con = database.open()) {
            con.setAutoCommit(false);

            try {
                int affected;

                try (PreparedStatement sql = con.prepareStatement(UPDATE_RESERVATION)) {
                    sql.setString(1, reservation.getCustomer().getId());
                    sql.setInt(2, reservation.getFlight().getId());
                    sql.setInt(3, reservation.getId());
                    affected = sql.executeUpdate();
                }

                if (affected == 0) {
                    throw new NotFoundException(ENTITY, reservation.getId());
                }

                try (PreparedStatement sql = con.prepareStatement(DELETE_DETAILS)) {
                    sql.setInt(1, reservation.getId());
                    sql.executeUpdate();
                }

                insertDetails(con, reservation);
                con.commit();

                return reservation;

            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(con);
                throw e;
            }

        } catch (SQLException e) {
            throw ReservationDatabaseErrors.translate(e, ENTITY);
        }
    }

    /**
     * Elimina una reserva y sus pasajeros.
     *
     * @param id identificador de la reserva
     * @return true si se eliminó; false si no existía
     */
    @Override
    public boolean deleteById(Integer id) {
        if (id == null) {
            return false;
        }

        try (Connection con = database.open()) {
            con.setAutoCommit(false);

            try {
                try (PreparedStatement sql = con.prepareStatement(DELETE_DETAILS)) {
                    sql.setInt(1, id);
                    sql.executeUpdate();
                }

                int affected;

                try (PreparedStatement sql = con.prepareStatement(DELETE_RESERVATION)) {
                    sql.setInt(1, id);
                    affected = sql.executeUpdate();
                }

                con.commit();
                return affected > 0;

            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(con);
                throw e;
            }

        } catch (SQLException e) {
            throw ReservationDatabaseErrors.translate(e, ENTITY);
        }
    }

    /**
     * Comprueba si existe una reserva con el identificador indicado.
     *
     * @param id identificador de la reserva
     * @return true si existe
     */
    @Override
    public boolean existsById(Integer id) {
        return findById(id).isPresent();
    }

    /**
     * Cuenta las reservas registradas.
     *
     * @return cantidad de reservas
     */
    @Override
    public long count() {
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(COUNT);
             ResultSet row = sql.executeQuery()) {

            row.next();
            return row.getLong(1);

        } catch (SQLException e) {
            throw ReservationDatabaseErrors.translate(e, ENTITY);
        }
    }

    /**
     * Busca las reservas asociadas a un cliente.
     *
     * @param customerId identificador del cliente
     * @return reservas del cliente
     */
    @Override
    public List<Reservation> findByCustomer(String customerId) {
        if (customerId == null) {
            return List.of();
        }

        return findByIds(SELECT_BY_CUSTOMER, customerId);
    }

    /**
     * Busca las reservas asociadas a un vuelo.
     *
     * @param flightId identificador del vuelo
     * @return reservas del vuelo
     */
    @Override
    public List<Reservation> findByFlight(int flightId) {
        return findByIds(SELECT_BY_FLIGHT, flightId);
    }

    /**
     * Comprueba si un cliente tiene reservas.
     *
     * @param customerId identificador del cliente
     * @return true si tiene reservas
     */
    @Override
    public boolean existsByCustomer(String customerId) {
        return queryExists(EXISTS_BY_CUSTOMER, customerId);
    }

    /**
     * Comprueba si un vuelo tiene reservas.
     *
     * @param flightId identificador del vuelo
     * @return true si tiene reservas
     */
    @Override
    public boolean existsByFlight(int flightId) {
        return queryExists(EXISTS_BY_FLIGHT, flightId);
    }

    /**
     * Inserta todos los pasajeros pertenecientes a una reserva.
     *
     * @param con conexión dentro de la transacción activa
     * @param reservation reserva cuyos pasajeros se insertarán
     * @throws SQLException si ocurre un error de persistencia
     */
    private void insertDetails(Connection con, Reservation reservation)
        throws SQLException {

        try (PreparedStatement sql = con.prepareStatement(INSERT_DETAIL)) {
            for (ReservationDetail detail : reservation.getDetails()) {
                sql.setInt(1, detail.getId());
                sql.setInt(2, reservation.getId());
                sql.setString(3, detail.getPassengerFullName());
                sql.setString(4, detail.getPassengerDocument());
                sql.setInt(5, detail.getSeat().getSeatNumber());
                sql.setString(6, detail.getSeat().getRowNumber());
                sql.addBatch();
            }

            sql.executeBatch();
        }
    }

    /**
     * Construye una reserva con sus objetos relacionados y pasajeros.
     *
     * @param con conexión JDBC
     * @param id identificador de la reserva
     * @param customerId identificador del cliente
     * @param flightId identificador del vuelo
     * @return reserva reconstruida
     * @throws SQLException si ocurre un error de consulta
     */
    private Reservation buildReservation(
        Connection con, int id, String customerId, int flightId)
        throws SQLException {

        var customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new AirlinesException(
                "No se encontró el cliente asociado a la reserva " + id + "."));

        var flight = flightRepository.findById(flightId)
            .orElseThrow(() -> new AirlinesException(
                "No se encontró el vuelo asociado a la reserva " + id + "."));

        List<ReservationDetail> details = new ArrayList<>();

        try (PreparedStatement sql = con.prepareStatement(SELECT_DETAILS)) {
            sql.setInt(1, id);

            try (ResultSet row = sql.executeQuery()) {
                while (row.next()) {
                    details.add(new ReservationDetail(
                        row.getInt("DetailId"),
                        row.getString("PassangerFullname"),
                        row.getString("PassagerDocument"),
                        new Seat(
                            row.getString("RowNumber"),
                            row.getInt("SeatNumer")
                        )
                    ));
                }
            }
        }

        if (details.isEmpty()) {
            throw new AirlinesException(
                "La reserva " + id + " no tiene pasajeros registrados.");
        }

        Reservation reservation = new Reservation(
            id, customer, flight, details.get(0));

        for (int i = 1; i < details.size(); i++) {
            reservation.addPassenger(details.get(i));
        }

        return reservation;
    }

    /**
     * Ejecuta una consulta de identificadores y recupera las reservas.
     *
     * @param query consulta SQL
     * @param params parámetros de la consulta
     * @return reservas encontradas
     */
    private List<Reservation> findByIds(String query, Object... params) {
        List<Integer> ids = new ArrayList<>();

        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(query)) {

            setParams(sql, params);

            try (ResultSet row = sql.executeQuery()) {
                while (row.next()) {
                    ids.add(row.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw ReservationDatabaseErrors.translate(e, ENTITY);
        }

        List<Reservation> reservations = new ArrayList<>();

        for (Integer id : ids) {
            findById(id).ifPresent(reservations::add);
        }

        return reservations;
    }

    /**
     * Comprueba si una consulta devuelve al menos un registro.
     *
     * @param query consulta SQL
     * @param params parámetros de la consulta
     * @return true si existe al menos un registro
     */
    private boolean queryExists(String query, Object... params) {
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(query)) {

            setParams(sql, params);

            try (ResultSet row = sql.executeQuery()) {
                return row.next();
            }

        } catch (SQLException e) {
            throw ReservationDatabaseErrors.translate(e, ENTITY);
        }
    }

    /**
     * Asigna los parámetros de una consulta preparada.
     *
     * @param sql sentencia preparada
     * @param params valores que se asignarán
     * @throws SQLException si ocurre un error al asignar parámetros
     */
    private void setParams(PreparedStatement sql, Object... params)
        throws SQLException {

        for (int i = 0; i < params.length; i++) {
            sql.setObject(i + 1, params[i]);
        }
    }

    /**
     * Intenta revertir una transacción sin ocultar el error original.
     *
     * @param con conexión cuya transacción se revertirá
     */
    private void rollbackQuietly(Connection con) {
        try {
            con.rollback();
        } catch (SQLException ignored) {
            // Se conserva la excepción original.
        }
    }
}
