package com.airlinespoo.locations.repository;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.City;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCityRepository extends CityRepository {

    private static final String SELECT_ALL =
        "SELECT CityId, name FROM dbo.tblCities ORDER BY name";
    private static final String SELECT_BY_ID =
        "SELECT CityId, name FROM dbo.tblCities WHERE CityId = ?";
    private static final String SELECT_BY_NAME =
        "SELECT CityId, name FROM dbo.tblCities WHERE LOWER(name) LIKE ? ORDER BY name";
    private static final String INSERT =
        "INSERT INTO dbo.tblCities (name) VALUES (?)";
    private static final String UPDATE =
        "UPDATE dbo.tblCities SET name = ? WHERE CityId = ?";
    private static final String DELETE =
        "DELETE FROM dbo.tblCities WHERE CityId = ?";
    private static final String COUNT =
        "SELECT COUNT(*) FROM dbo.tblCities";

    private final DatabaseConnection database;

    public JdbcCityRepository(DatabaseConnection database) {
        this.database = Validator.requireNotNull(database, "conexiÃ³n a la base de datos");
    }

    @Override
    public City create(City city) {
        Validator.requireNotNull(city, "ciudad");
        try (Connection con = database.open();
             PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, city.getName());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    return new City(generatedId, city.getName());
                }
            }
            return city;
        } catch (SQLException e) {
            if (isDuplicateKey(e)) {
                throw new DuplicateException("Ya existe una ciudad con ese dato.");
            }
            throw new AirlinesException("Error al registrar la ciudad en SQL Server.", e);
        }
    }

    @Override
    public Optional<City> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        try (Connection con = database.open();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(toCity(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error al consultar la ciudad por id.", e);
        }
    }

    @Override
    public List<City> findAll() {
        List<City> cities = new ArrayList<>();
        try (Connection con = database.open();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cities.add(toCity(rs));
            }
            return cities;
        } catch (SQLException e) {
            throw new AirlinesException("Error al listar las ciudades.", e);
        }
    }

    @Override
    public City update(City city) {
        Validator.requireNotNull(city, "ciudad");
        try (Connection con = database.open();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {
            ps.setString(1, city.getName());
            ps.setInt(2, city.getId());
            if (ps.executeUpdate() == 0) {
                throw new NotFoundException("Ciudad no encontrada.");
            }
            return city;
        } catch (SQLException e) {
            throw new AirlinesException("Error al actualizar la ciudad.", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        if (id == null) {
            return false;
        }
        try (Connection con = database.open();
             PreparedStatement ps = con.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AirlinesException("Error al eliminar la ciudad.", e);
        }
    }

    @Override
    public boolean existsById(Integer id) {
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
            throw new AirlinesException("Error al contar las ciudades.", e);
        }
    }

    @Override
    public List<City> findByName(String name) {
        List<City> cities = new ArrayList<>();
        if (name == null || name.isBlank()) {
            return cities;
        }
        String pattern = "%" + name.trim().toLowerCase() + "%";
        try (Connection con = database.open();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_NAME)) {
            ps.setString(1, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cities.add(toCity(rs));
                }
            }
            return cities;
        } catch (SQLException e) {
            throw new AirlinesException("Error al buscar ciudades por nombre.", e);
        }
    }

    private City toCity(ResultSet rs) throws SQLException {
        return new City(rs.getInt("CityId"), rs.getString("name"));
    }

    private boolean isDuplicateKey(SQLException e) {
        int code = e.getErrorCode();
        return code == 2627 || code == 2601;
    }
}
