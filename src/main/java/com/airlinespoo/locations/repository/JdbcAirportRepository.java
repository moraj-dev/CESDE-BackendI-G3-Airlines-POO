package com.airlinespoo.locations.repository;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcAirportRepository extends AirportRepository {

    private static final String SELECT_BASE =
        "SELECT a.AirportId, a.name AS AirportName, c.CityId, c.name AS CityName "
            + "FROM dbo.tblAirports a "
            + "JOIN dbo.tblCities c ON a.CityId = c.CityId ";
    private static final String SELECT_ALL =
        SELECT_BASE + "ORDER BY a.name";
    private static final String SELECT_BY_ID =
        SELECT_BASE + "WHERE a.AirportId = ?";
    private static final String SELECT_BY_CITY =
        SELECT_BASE + "WHERE a.CityId = ? ORDER BY a.name";
    private static final String SELECT_BY_NAME =
        SELECT_BASE + "WHERE LOWER(a.name) LIKE ? ORDER BY a.name";
    private static final String INSERT =
        "INSERT INTO dbo.tblAirports (AirportId, name, CityId) VALUES (?, ?, ?)";
    private static final String UPDATE =
        "UPDATE dbo.tblAirports SET name = ?, CityId = ? WHERE AirportId = ?";
    private static final String DELETE =
        "DELETE FROM dbo.tblAirports WHERE AirportId = ?";
    private static final String COUNT =
        "SELECT COUNT(*) FROM dbo.tblAirports";

    private final DatabaseConnection database;

    public JdbcAirportRepository(DatabaseConnection database) {
        this.database = Validator.requireNotNull(database, "conexión a la base de datos");
    }

    @Override
    public Airport create(Airport airport) {
        Validator.requireNotNull(airport, "aeropuerto");
        try (Connection con = database.open();
             PreparedStatement ps = con.prepareStatement(INSERT)) {
            ps.setString(1, airport.getId().toString());
            ps.setString(2, airport.getName());
            ps.setInt(3, airport.getCity().getId());
            ps.executeUpdate();
            return airport;
        } catch (SQLException e) {
            if (isDuplicateKey(e)) {
                throw new DuplicateException("Ya existe un aeropuerto con el id " + airport.getId() + ".");
            }
            throw new AirlinesException("Error al registrar el aeropuerto en SQL Server.", e);
        }
    }

    @Override
    public Optional<Airport> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        try (Connection con = database.open();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(toAirport(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error al consultar el aeropuerto por id.", e);
        }
    }

    @Override
    public List<Airport> findAll() {
        List<Airport> airports = new ArrayList<>();
        try (Connection con = database.open();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                airports.add(toAirport(rs));
            }
            return airports;
        } catch (SQLException e) {
            throw new AirlinesException("Error al listar los aeropuertos.", e);
        }
    }

    @Override
    public Airport update(Airport airport) {
        Validator.requireNotNull(airport, "aeropuerto");
        try (Connection con = database.open();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {
            ps.setString(1, airport.getName());
            ps.setInt(2, airport.getCity().getId());
            ps.setString(3, airport.getId().toString());
            if (ps.executeUpdate() == 0) {
                throw new NotFoundException("Aeropuerto no encontrado.");
            }
            return airport;
        } catch (SQLException e) {
            throw new AirlinesException("Error al actualizar el aeropuerto.", e);
        }
    }

    @Override
    public boolean deleteById(UUID id) {
        if (id == null) {
            return false;
        }
        try (Connection con = database.open();
             PreparedStatement ps = con.prepareStatement(DELETE)) {
            ps.setString(1, id.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AirlinesException("Error al eliminar el aeropuerto.", e);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        return findById(id).isPresent();
    }

    @Override
    public long count() {
        try (Connection con = database.open();
             PreparedStatement ps = con.prepareStatement(COUNT);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new AirlinesException("Error al contar los aeropuertos.", e);
        }
    }

    @Override
    public List<Airport> findByCity(int cityId) {
        List<Airport> airports = new ArrayList<>();
        try (Connection con = database.open();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_CITY)) {
            ps.setInt(1, cityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    airports.add(toAirport(rs));
                }
            }
            return airports;
        } catch (SQLException e) {
            throw new AirlinesException("Error al buscar aeropuertos por ciudad.", e);
        }
    }

    @Override
    public List<Airport> findByName(String name) {
        List<Airport> airports = new ArrayList<>();
        if (name == null || name.isBlank()) {
            return airports;
        }
        String pattern = "%" + name.trim().toLowerCase() + "%";
        try (Connection con = database.open();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_NAME)) {
            ps.setString(1, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    airports.add(toAirport(rs));
                }
            }
            return airports;
        } catch (SQLException e) {
            throw new AirlinesException("Error al buscar aeropuertos por nombre.", e);
        }
    }

    private Airport toAirport(ResultSet rs) throws SQLException {
        City city = new City(rs.getInt("CityId"), rs.getString("CityName"));
        UUID id = UUID.fromString(rs.getString("AirportId").trim());
        return new Airport(id, rs.getString("AirportName"), city);
    }

    private boolean isDuplicateKey(SQLException e) {
        int code = e.getErrorCode();
        return code == 2627 || code == 2601;
    }
}
