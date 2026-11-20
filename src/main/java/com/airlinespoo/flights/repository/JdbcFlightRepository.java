package com.airlinespoo.flights.repository;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.flights.model.FlightSchedule;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del repositorio de vuelos con JDBC sobre la tabla dbo.tblFlights.
 *
 * <p>Para armar cada {@link com.airlinespoo.flights.model.Flight} con sus aeropuertos
 * (cada uno con su ciudad) y su aerolínea (objetos, no ids sueltos), las consultas hacen
 * JOIN con tblAirports (dos veces: salida y llegada), tblCities (dos veces) y tblAirlines.</p>
 *
 * <p>El id del vuelo lo genera SQL Server (columna IDENTITY); se lee después del INSERT.</p>
 */
public class JdbcFlightRepository implements FlightRepository {

    private static final String ENTITY = "vuelo";

    // Cantidad de columnas que llena setFlightColumns (de FlightName a AirlineId).
    // En el UPDATE, el FlightId del WHERE va en la posición siguiente.
    private static final int FLIGHT_COLUMN_COUNT = 8;

    // Consulta base: vuelo + aeropuerto de salida y su ciudad + aeropuerto de llegada y su
    // ciudad + aerolínea. Los alias (AS) evitan que dos columnas "name" se confundan.
    private static final String SELECT_BASE =
            "SELECT f.FlightId, f.FlightName, "
            + "f.DateDeparture, f.HourDeparture, f.DateArrival, f.HourArrival, "
            + "dep.AirportId AS DepAirportId, dep.name AS DepAirportName, "
            + "depCity.CityId AS DepCityId, depCity.name AS DepCityName, "
            + "arr.AirportId AS ArrAirportId, arr.name AS ArrAirportName, "
            + "arrCity.CityId AS ArrCityId, arrCity.name AS ArrCityName, "
            + "al.airlineId AS AirlineId, al.name AS AirlineName "
            + "FROM dbo.tblFlights f "
            + "JOIN dbo.tblAirports dep ON f.Departure = dep.AirportId "
            + "JOIN dbo.tblCities depCity ON dep.CityId = depCity.CityId "
            + "JOIN dbo.tblAirports arr ON f.Arrival = arr.AirportId "
            + "JOIN dbo.tblCities arrCity ON arr.CityId = arrCity.CityId "
            + "JOIN dbo.tblAirlines al ON f.AirlineId = al.airlineId ";

    private static final String SELECT_ALL =
            SELECT_BASE + "ORDER BY f.DateDeparture, f.HourDeparture";
    private static final String SELECT_BY_ID = SELECT_BASE + "WHERE f.FlightId = ?";
    private static final String SELECT_BY_FLIGHT_NAME =
            SELECT_BASE + "WHERE UPPER(f.FlightName) = UPPER(?)";
    private static final String SELECT_BY_AIRLINE = SELECT_BASE + "WHERE f.AirlineId = ?";
    private static final String SELECT_BY_DEPARTURE_DATES =
            SELECT_BASE + "WHERE f.DateDeparture BETWEEN ? AND ?";
    private static final String SELECT_BY_ROUTE =
            SELECT_BASE + "WHERE f.Departure = ? AND f.Arrival = ?";

    // FlightId no va en el INSERT: lo genera la columna IDENTITY
    private static final String INSERT =
            "INSERT INTO dbo.tblFlights (FlightName, Departure, Arrival, DateDeparture, "
            + "HourDeparture, DateArrival, HourArrival, AirlineId) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE dbo.tblFlights SET FlightName = ?, Departure = ?, Arrival = ?, "
            + "DateDeparture = ?, HourDeparture = ?, DateArrival = ?, HourArrival = ?, "
            + "AirlineId = ? WHERE FlightId = ?";
    private static final String DELETE = "DELETE FROM dbo.tblFlights WHERE FlightId = ?";

    private static final String COUNT = "SELECT COUNT(*) FROM dbo.tblFlights";
    private static final String COUNT_BY_AIRLINE =
            "SELECT COUNT(*) FROM dbo.tblFlights WHERE AirlineId = ?";
    private static final String EXISTS_BY_FLIGHT_NAME =
            "SELECT 1 FROM dbo.tblFlights WHERE UPPER(FlightName) = UPPER(?)";
    private static final String EXISTS_BY_AIRLINE =
            "SELECT 1 FROM dbo.tblFlights WHERE AirlineId = ?";
    private static final String EXISTS_BY_AIRPORT =
            "SELECT 1 FROM dbo.tblFlights WHERE Departure = ? OR Arrival = ?";

    private final DatabaseConnection database;

    /**
     * Recibe la conexión por el constructor (inyección de dependencias).
     *
     * @param database fábrica de conexiones a SQL Server
     */
    public JdbcFlightRepository(DatabaseConnection database) {
        this.database = Validator.requireNotNull(database, "conexión a la base de datos");
    }

    // ---------- Operaciones comunes del contrato ----------

    /**
     * INSERT en tblFlights. SQL Server genera el FlightId (IDENTITY) y aquí se lee.
     *
     * @param flight vuelo sin guardar, con id {@link Flight#UNSAVED_ID}
     * @return un vuelo nuevo con los mismos datos y el id real; quien llama debe usar este
     */
    @Override
    public Flight create(Flight flight) {
        Validator.requireNotNull(flight, ENTITY);
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            setFlightColumns(sql, flight);
            sql.executeUpdate();
            try (ResultSet keys = sql.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new AirlinesException(
                        "La base de datos no devolvió el id del vuelo nuevo.");
                }
                int newId = keys.getInt(1);
                return new Flight(newId, flight.getFlightName(), flight.getDeparture(),
                    flight.getArrival(), flight.getAirline(), flight.getSchedule());
            }
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    /** SELECT ... WHERE FlightId = ? */
    @Override
    public Optional<Flight> findById(Integer id) {
        return queryOne(SELECT_BY_ID, id);
    }

    /** SELECT de todos los vuelos, ordenados por fecha y hora de salida. */
    @Override
    public List<Flight> findAll() {
        return queryList(SELECT_ALL);
    }

    /**
     * UPDATE tblFlights SET ... WHERE FlightId = ? (cambia todo menos el id).
     *
     * @throws NotFoundException si no existe un vuelo con ese id
     */
    @Override
    public Flight update(Flight flight) {
        Validator.requireNotNull(flight, ENTITY);
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(UPDATE)) {
            setFlightColumns(sql, flight);
            sql.setInt(FLIGHT_COLUMN_COUNT + 1, flight.getId());
            if (sql.executeUpdate() == 0) {
                throw new NotFoundException(ENTITY, flight.getId());
            }
            return flight;
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    /** DELETE ... WHERE FlightId = ? */
    @Override
    public boolean deleteById(Integer id) {
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(DELETE)) {
            sql.setInt(1, id);
            return sql.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    /** Usa findById: si encuentra algo, existe. */
    @Override
    public boolean existsById(Integer id) {
        return findById(id).isPresent();
    }

    /** SELECT COUNT(*) */
    @Override
    public long count() {
        return queryCount(COUNT);
    }

    // ---------- Búsquedas propias del módulo ----------

    /** SELECT ... WHERE FlightName = ? (sin distinguir mayúsculas) */
    @Override
    public Optional<Flight> findByFlightName(String flightName) {
        if (flightName == null) {
            return Optional.empty();
        }
        return queryOne(SELECT_BY_FLIGHT_NAME, Validator.normalize(flightName));
    }

    /** SELECT 1 ... WHERE FlightName = ? (sin distinguir mayúsculas) */
    @Override
    public boolean existsByFlightName(String flightName) {
        if (flightName == null) {
            return false;
        }
        return queryExists(EXISTS_BY_FLIGHT_NAME, Validator.normalize(flightName));
    }

    /** SELECT ... WHERE AirlineId = ? */
    @Override
    public List<Flight> findByAirline(Airline airline) {
        return queryList(SELECT_BY_AIRLINE, airline.getId());
    }

    /** SELECT ... WHERE DateDeparture BETWEEN ? AND ? (extremos incluidos) */
    @Override
    public List<Flight> findByDepartureDateBetween(LocalDate from, LocalDate to) {
        return queryList(SELECT_BY_DEPARTURE_DATES, from, to);
    }

    /** SELECT ... WHERE Departure = ? AND Arrival = ? */
    @Override
    public List<Flight> findByRoute(Airport departure, Airport arrival) {
        return queryList(SELECT_BY_ROUTE,
            departure.getId().toString(), arrival.getId().toString());
    }

    /** SELECT COUNT(*) ... WHERE AirlineId = ? */
    @Override
    public long countByAirline(Airline airline) {
        return queryCount(COUNT_BY_AIRLINE, airline.getId());
    }

    /** SELECT 1 ... WHERE AirlineId = ? */
    @Override
    public boolean existsByAirline(Airline airline) {
        return queryExists(EXISTS_BY_AIRLINE, airline.getId());
    }

    /** SELECT 1 ... WHERE Departure = ? OR Arrival = ? (el mismo id en los dos ?) */
    @Override
    public boolean existsByAirport(Airport airport) {
        String airportId = airport.getId().toString();
        return queryExists(EXISTS_BY_AIRPORT, airportId, airportId);
    }

    // ---------- Métodos de apoyo (para no repetir código) ----------

    // Ejecuta un SELECT y convierte cada fila en un Flight
    private List<Flight> queryList(String query, Object... params) {
        List<Flight> flights = new ArrayList<>();
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(query)) {
            setParams(sql, params);
            try (ResultSet row = sql.executeQuery()) {
                while (row.next()) {
                    flights.add(toFlight(row));
                }
            }
            return flights;
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    // Igual que queryList, pero devuelve solo el primer vuelo (o vacío si no hay)
    private Optional<Flight> queryOne(String query, Object... params) {
        List<Flight> found = queryList(query, params);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(found.get(0));
    }

    // Ejecuta un SELECT 1 y responde si devolvió al menos una fila
    private boolean queryExists(String query, Object... params) {
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(query)) {
            setParams(sql, params);
            try (ResultSet row = sql.executeQuery()) {
                return row.next();
            }
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    // Ejecuta un SELECT COUNT(*) y devuelve el número
    private long queryCount(String query, Object... params) {
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(query)) {
            setParams(sql, params);
            try (ResultSet row = sql.executeQuery()) {
                row.next();
                return row.getLong(1);
            }
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    // Llena los ? de la consulta en orden (setObject sirve para texto, números y fechas)
    private void setParams(PreparedStatement sql, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            sql.setObject(i + 1, params[i]);
        }
    }

    // Llena las 8 columnas del vuelo, en el mismo orden del INSERT y del UPDATE.
    // Los UUID de los aeropuertos se envían como texto: SQL Server los convierte a UNIQUEIDENTIFIER.
    private void setFlightColumns(PreparedStatement sql, Flight flight) throws SQLException {
        FlightSchedule schedule = flight.getSchedule();
        sql.setString(1, flight.getFlightName());
        sql.setString(2, flight.getDeparture().getId().toString());
        sql.setString(3, flight.getArrival().getId().toString());
        sql.setObject(4, schedule.getDepartureDate());
        sql.setObject(5, schedule.getDepartureTime());
        sql.setObject(6, schedule.getArrivalDate());
        sql.setObject(7, schedule.getArrivalTime());
        sql.setInt(8, flight.getAirline().getId());
    }

    // Convierte la fila actual en un Flight con sus aeropuertos, ciudades, aerolínea y horario
    private Flight toFlight(ResultSet row) throws SQLException {
        City departureCity = new City(row.getInt("DepCityId"), row.getString("DepCityName"));
        Airport departure = new Airport(UUID.fromString(row.getString("DepAirportId")),
            row.getString("DepAirportName"), departureCity);

        City arrivalCity = new City(row.getInt("ArrCityId"), row.getString("ArrCityName"));
        Airport arrival = new Airport(UUID.fromString(row.getString("ArrAirportId")),
            row.getString("ArrAirportName"), arrivalCity);

        Airline airline = new Airline(row.getInt("AirlineId"), row.getString("AirlineName"));

        FlightSchedule schedule = new FlightSchedule(
            row.getObject("DateDeparture", LocalDate.class),
            row.getObject("HourDeparture", LocalTime.class),
            row.getObject("DateArrival", LocalDate.class),
            row.getObject("HourArrival", LocalTime.class));

        return new Flight(row.getInt("FlightId"), row.getString("FlightName"),
            departure, arrival, airline, schedule);
    }
}